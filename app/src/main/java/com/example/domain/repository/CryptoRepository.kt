package com.example.domain.repository

import com.example.data.local.WatchlistItemEntity
import com.example.domain.model.MarketEvent
import com.example.domain.model.NewsArticle
import kotlinx.coroutines.flow.Flow

interface CryptoRepository {
    fun getLatestNews(forceRefresh: Boolean): Flow<List<NewsArticle>>
    fun getEvents(forceRefresh: Boolean): Flow<List<MarketEvent>>
    fun getCoinPrices(): Flow<Map<String, Pair<Double, Double>>>
    fun getWatchlist(): Flow<List<WatchlistItemEntity>>
    suspend fun addToWatchlist(id: String, title: String, type: String)
    suspend fun removeFromWatchlist(id: String)
    fun isWatchlisted(id: String): Flow<Boolean>
}
