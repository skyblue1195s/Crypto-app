package com.example.presentation

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.WatchlistItemEntity
import com.example.data.remote.CoinGeckoService
import com.example.data.remote.CryptoCompareService
import com.example.data.repository.CryptoRepositoryImpl
import com.example.domain.model.ImpactLevel
import com.example.domain.model.MarketEvent
import com.example.domain.model.NewsArticle
import com.example.domain.repository.CryptoRepository
import com.example.notification.NotificationHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CryptoViewModel(
    application: Application,
    private val repository: CryptoRepository
) : AndroidViewModel(application) {

    // Filter states
    private val _newsSearchQuery = MutableStateFlow("")
    val newsSearchQuery = _newsSearchQuery.asStateFlow()

    private val _newsFilterCoin = MutableStateFlow<String?>(null)
    val newsFilterCoin = _newsFilterCoin.asStateFlow()

    private val _eventFilterType = MutableStateFlow<String?>(null) // "AIRDROP", "FED_MEETING", "ECONOMIC"
    val eventFilterType = _eventFilterType.asStateFlow()

    private val _eventFilterImpact = MutableStateFlow<ImpactLevel?>(null)
    val eventFilterImpact = _eventFilterImpact.asStateFlow()

    // Settings States
    private val _gmtPlus7Enabled = MutableStateFlow(true)
    val gmtPlus7Enabled = _gmtPlus7Enabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    // Active detail event
    private val _selectedEvent = MutableStateFlow<MarketEvent?>(null)
    val selectedEvent = _selectedEvent.asStateFlow()

    // Fetch and combine news articles with filters
    val newsArticles: StateFlow<List<NewsArticle>> = repository.getLatestNews(forceRefresh = false)
        .combine(_newsSearchQuery) { list, query ->
            if (query.isEmpty()) list else list.filter {
                it.title.contains(query, ignoreCase = true) || it.body.contains(query, ignoreCase = true)
            }
        }
        .combine(_newsFilterCoin) { list, coin ->
            if (coin == null) list else list.filter {
                it.categories.contains(coin, ignoreCase = true) || it.title.contains(coin, ignoreCase = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Fetch and combine events with filters
    val marketEvents: StateFlow<List<MarketEvent>> = repository.getEvents(forceRefresh = false)
        .combine(_eventFilterType) { list, type ->
            if (type == null) list else list.filter {
                when (type) {
                    "AIRDROP" -> it is com.example.domain.model.AirdropEvent
                    "FED_MEETING" -> it is com.example.domain.model.FedMeetingEvent
                    "ECONOMIC" -> it is com.example.domain.model.EconomicDataEvent
                    else -> true
                }
            }
        }
        .combine(_eventFilterImpact) { list, impact ->
            if (impact == null) list else list.filter { it.impact == impact }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time prices
    val coinPrices: StateFlow<Map<String, Pair<Double, Double>>> = repository.getCoinPrices()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            mapOf(
                "bitcoin" to Pair(67250.0, 1.45),
                "ethereum" to Pair(3450.0, -0.82),
                "solana" to Pair(142.5, 4.12),
                "binancecoin" to Pair(582.0, 0.15),
                "cardano" to Pair(0.38, -1.25)
            )
        )

    // Watchlist items
    val watchlistItems: StateFlow<List<WatchlistItemEntity>> = repository.getWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshNewsFeed()
        NotificationHelper.createNotificationChannel(application)
    }

    fun refreshNewsFeed() {
        viewModelScope.launch {
            if (repository is CryptoRepositoryImpl) {
                repository.refreshNews()
            }
        }
    }

    // Set news search query
    fun setNewsSearch(query: String) {
        _newsSearchQuery.value = query
    }

    // Set news coin filter
    fun setNewsCoinFilter(coin: String?) {
        _newsFilterCoin.value = coin
    }

    // Set event type filter
    fun setEventTypeFilter(type: String?) {
        _eventFilterType.value = type
    }

    // Set event impact filter
    fun setEventImpactFilter(impact: ImpactLevel?) {
        _eventFilterImpact.value = impact
    }

    // Select an event for detail view
    fun selectEvent(event: MarketEvent?) {
        _selectedEvent.value = event
    }

    // Toggle Watchlist item
    fun toggleWatchlist(event: MarketEvent) {
        viewModelScope.launch {
            val isPresent = watchlistItems.value.any { it.id == event.id }
            if (isPresent) {
                repository.removeFromWatchlist(event.id)
                if (_notificationsEnabled.value) {
                    NotificationHelper.cancelEventAlarm(getApplication(), event.id)
                }
            } else {
                repository.addToWatchlist(event.id, event.title, "EVENT")
                if (_notificationsEnabled.value) {
                    // Schedule local notification 30 minutes before the event
                    val triggerTime = event.timestamp - 30 * 60 * 1000L
                    if (triggerTime > System.currentTimeMillis()) {
                        NotificationHelper.scheduleEventAlarm(
                            getApplication(),
                            event.id,
                            event.title,
                            triggerTime
                        )
                    }
                }
            }
        }
    }

    fun isEventWatchlisted(eventId: String): Boolean {
        return watchlistItems.value.any { it.id == eventId }
    }

    // Toggle GMT+7 / local timezone
    fun toggleGmtPlus7(enabled: Boolean) {
        _gmtPlus7Enabled.value = enabled
    }

    // Toggle Notifications
    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        if (!enabled) {
            // Cancel all scheduled alarms
            watchlistItems.value.forEach {
                NotificationHelper.cancelEventAlarm(getApplication(), it.id)
            }
        } else {
            // Re-schedule alarms for upcoming watchlisted events
            viewModelScope.launch {
                repository.getEvents(false).collect { events ->
                    events.filter { isEventWatchlisted(it.id) }.forEach { event ->
                        val triggerTime = event.timestamp - 30 * 60 * 1000L
                        if (triggerTime > System.currentTimeMillis()) {
                            NotificationHelper.scheduleEventAlarm(
                                getApplication(),
                                event.id,
                                event.title,
                                triggerTime
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            application: Application,
            repository: CryptoRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CryptoViewModel(application, repository) as T
            }
        }
    }
}
