package com.example.presentation

import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.core.content.ContextCompat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.WatchlistItemEntity
import com.example.domain.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CryptoViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val adsEnabled by viewModel.adsEnabled.collectAsState()
    val adsMargin by viewModel.adsMargin.collectAsState()
    val language by viewModel.language.collectAsState()
    val btcAlertsEnabled by viewModel.btcAlertsEnabled.collectAsState()

    val startupPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (btcAlertsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    startupPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(CryptoDarkBg)) {
                // Main Header
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.TrendingUp,
                                contentDescription = "Logo",
                                tint = CryptoGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = Translations.getString("app_title", language),
                                color = CryptoGold,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 20.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CryptoDarkBg,
                        titleContentColor = CryptoTextPrimary
                    )
                )

                // Real-time Price Ticker Row
                PriceTickerRow(viewModel = viewModel)
                
                HorizontalDivider(color = CryptoSurfaceVariant, thickness = 1.dp)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CryptoSurface,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text(Translations.getString("tab_news", language), fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) AppIcons.FeedFilled else AppIcons.FeedOutlined,
                            contentDescription = "News"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CryptoDarkBg,
                        selectedTextColor = CryptoGold,
                        indicatorColor = CryptoGold,
                        unselectedIconColor = CryptoTextSecondary,
                        unselectedTextColor = CryptoTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_news")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text(Translations.getString("tab_calendar", language), fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) AppIcons.CalendarMonthFilled else AppIcons.CalendarMonthOutlined,
                            contentDescription = "Calendar"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CryptoDarkBg,
                        selectedTextColor = CryptoGold,
                        indicatorColor = CryptoGold,
                        unselectedIconColor = CryptoTextSecondary,
                        unselectedTextColor = CryptoTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_calendar")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text(Translations.getString("tab_watchlist", language), fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Star else AppIcons.StarOutline,
                            contentDescription = "Watchlist"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CryptoDarkBg,
                        selectedTextColor = CryptoGold,
                        indicatorColor = CryptoGold,
                        unselectedIconColor = CryptoTextSecondary,
                        unselectedTextColor = CryptoTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_watchlist")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    label = { Text(Translations.getString("tab_settings", language), fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CryptoDarkBg,
                        selectedTextColor = CryptoGold,
                        indicatorColor = CryptoGold,
                        unselectedIconColor = CryptoTextSecondary,
                        unselectedTextColor = CryptoTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        containerColor = CryptoDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> NewsTab(viewModel = viewModel)
                    1 -> CalendarTab(viewModel = viewModel)
                    2 -> WatchlistTab(viewModel = viewModel)
                    3 -> SettingsTab(viewModel = viewModel)
                }

                // Show Event detail dialog when selected
                selectedEvent?.let { event ->
                    EventDetailDialog(
                        event = event,
                        viewModel = viewModel,
                        onDismiss = { viewModel.selectEvent(null) }
                    )
                }
            }

            if (adsEnabled) {
                AdBanner(viewModel = viewModel, marginDp = adsMargin)
            }
        }
    }
}

@Composable
fun PriceTickerRow(viewModel: CryptoViewModel) {
    val prices by viewModel.coinPrices.collectAsState()
    val scrollState = rememberScrollState()

    val btcAlertsEnabled by viewModel.btcAlertsEnabled.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CryptoSurface)
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        prices.forEach { (coin, pricePair) ->
            val (price, change) = pricePair
            val isUp = change >= 0
            val color = if (isUp) ImpactSuccess else ImpactHigh
            val sign = if (isUp) "+" else ""
            val isBtc = coin.equals("bitcoin", ignoreCase = true)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(CryptoDarkBg, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                if (isBtc && btcAlertsEnabled) {
                    Icon(
                        imageVector = AppIcons.NotificationsActive,
                        contentDescription = "BTC Price Alert Active",
                        tint = CryptoGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = coin.replaceFirstChar { it.uppercase() },
                    color = CryptoTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$${String.format(Locale.US, "%,.2f", price)}",
                    color = CryptoGold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$sign${String.format(Locale.US, "%.2f", change)}%",
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun NewsTab(viewModel: CryptoViewModel) {
    val newsArticles by viewModel.newsArticles.collectAsState()
    val searchVal by viewModel.newsSearchQuery.collectAsState()
    val selectedCoinFilter by viewModel.newsFilterCoin.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val language by viewModel.language.collectAsState()

    val coins = listOf("All", "BTC", "ETH", "SOL", "ADA", "BNB")

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        TextField(
            value = searchVal,
            onValueChange = { viewModel.setNewsSearch(it) },
            placeholder = { Text(Translations.getString("search_placeholder", language), color = CryptoTextSecondary) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = CryptoGold) },
            trailingIcon = {
                if (searchVal.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setNewsSearch("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = CryptoGold)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CryptoSurface,
                unfocusedContainerColor = CryptoSurface,
                focusedTextColor = CryptoTextPrimary,
                unfocusedTextColor = CryptoTextPrimary,
                cursorColor = CryptoGold,
                focusedIndicatorColor = CryptoGold,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .testTag("news_search_input")
        )

        // Coin quick filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            coins.forEach { coin ->
                val isSelected = (coin == "All" && selectedCoinFilter == null) || (selectedCoinFilter == coin)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.setNewsCoinFilter(if (coin == "All") null else coin)
                    },
                    label = { 
                        val displayLabel = if (coin == "All") {
                            if (language == "vi") "Tất cả" else "All"
                        } else coin
                        Text(displayLabel, color = if (isSelected) CryptoDarkBg else CryptoTextPrimary) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CryptoGold,
                        containerColor = CryptoSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (newsArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = AppIcons.ArticleOutlined,
                        contentDescription = "Empty",
                        tint = CryptoTextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = Translations.getString("news_empty_desc", language),
                        color = CryptoTextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.refreshNewsFeed() },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoGold)
                    ) {
                        Text(Translations.getString("reload_news", language), color = CryptoDarkBg)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(newsArticles, key = { it.id }) { article ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(article.sourceUrl))
                                    context.startActivity(browserIntent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = CryptoSurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = article.sourceName.uppercase(),
                                    color = CryptoGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = formatTimestamp(article.timestamp),
                                    color = CryptoTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = article.title,
                                color = CryptoTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = article.body,
                                color = CryptoTextSecondary,
                                fontSize = 13.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (article.categories.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                ) {
                                    article.categories.split("|").take(3).forEach { cat ->
                                        if (cat.trim().isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .background(CryptoSurfaceVariant, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(cat.trim(), color = CryptoTextPrimary, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarTab(viewModel: CryptoViewModel) {
    val events by viewModel.marketEvents.collectAsState()
    val searchVal by viewModel.eventSearchQuery.collectAsState()
    val selectedType by viewModel.eventFilterType.collectAsState()
    val selectedImpact by viewModel.eventFilterImpact.collectAsState()
    val language by viewModel.language.collectAsState()
    val focusManager = LocalFocusManager.current

    val types = listOf("All", "Airdrops" to "AIRDROP", "Fed/FOMC" to "FED_MEETING", "Macro" to "ECONOMIC")

    Column(modifier = Modifier.fillMaxSize()) {
        // Calendar search bar
        TextField(
            value = searchVal,
            onValueChange = { viewModel.setEventSearch(it) },
            placeholder = { Text(Translations.getString("calendar_search_placeholder", language), color = CryptoTextSecondary) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = CryptoGold) },
            trailingIcon = {
                if (searchVal.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.setEventSearch("") },
                        modifier = Modifier.testTag("calendar_search_clear")
                    ) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = CryptoGold)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CryptoSurface,
                unfocusedContainerColor = CryptoSurface,
                focusedTextColor = CryptoTextPrimary,
                unfocusedTextColor = CryptoTextPrimary,
                cursorColor = CryptoGold,
                focusedIndicatorColor = CryptoGold,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .testTag("calendar_search_input")
        )

        // Horizontal Event Type Quick Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { item ->
                val label = if (item is Pair<*, *>) item.first as String else item as String
                val value = if (item is Pair<*, *>) item.second as String else null
                val isSelected = selectedType == value
                
                val displayLabel = when (label) {
                    "All" -> if (language == "vi") "Tất cả" else "All"
                    "Airdrops" -> Translations.getString("airdrop", language)
                    "Fed/FOMC" -> Translations.getString("fed_meeting", language)
                    "Macro" -> Translations.getString("economic_data", language)
                    else -> label
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setEventTypeFilter(value) },
                    label = { Text(displayLabel, color = if (isSelected) CryptoDarkBg else CryptoTextPrimary) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CryptoGold,
                        containerColor = CryptoSurface
                    )
                )
            }
        }

        // Impact Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${Translations.getString("impact", language)}:",
                color = CryptoTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            ImpactLevel.values().forEach { impact ->
                val isSelected = selectedImpact == impact
                val chipColor = when (impact) {
                    ImpactLevel.HIGH -> ImpactHigh
                    ImpactLevel.MEDIUM -> ImpactMedium
                    ImpactLevel.LOW -> ImpactLow
                }
                val impactDisplay = when (impact) {
                    ImpactLevel.HIGH -> Translations.getString("high", language)
                    ImpactLevel.MEDIUM -> Translations.getString("medium", language)
                    ImpactLevel.LOW -> Translations.getString("low", language)
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setEventImpactFilter(if (isSelected) null else impact) },
                    label = {
                        Text(
                            text = impactDisplay.uppercase(),
                            color = if (isSelected) CryptoDarkBg else chipColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chipColor,
                        containerColor = CryptoSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = AppIcons.EventBusyOutlined,
                        contentDescription = "Empty",
                        tint = CryptoTextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == "vi") "Không có sự kiện nào khớp bộ lọc." else "No events match the active filters.",
                        color = CryptoTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        viewModel = viewModel,
                        onClick = { viewModel.selectEvent(event) }
                    )
                }
            }
        }
    }
}

@Composable
fun WatchlistTab(viewModel: CryptoViewModel) {
    val watchlistItems by viewModel.watchlistItems.collectAsState()
    val events by viewModel.marketEvents.collectAsState()
    val language by viewModel.language.collectAsState()

    // Filter events that match the watchlist items
    val watchlistedEvents = events.filter { event ->
        watchlistItems.any { it.id == event.id }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${Translations.getString("tab_watchlist", language).uppercase()} (${watchlistedEvents.size})",
            color = CryptoGold,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        )

        if (watchlistedEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.StarOutline,
                        contentDescription = "Empty",
                        tint = CryptoTextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = Translations.getString("watchlist_empty", language),
                        color = CryptoTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = Translations.getString("watchlist_empty_desc", language),
                        color = CryptoTextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(watchlistedEvents, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        viewModel = viewModel,
                        onClick = { viewModel.selectEvent(event) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsTab(viewModel: CryptoViewModel) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val btcAlertsEnabled by viewModel.btcAlertsEnabled.collectAsState()
    val btcAlertThreshold by viewModel.btcAlertThreshold.collectAsState()
    val lastBtcPrice by viewModel.lastBtcPrice.collectAsState()
    val currentBtcPrice by viewModel.currentBtcPrice.collectAsState()
    val selectedTimezone by viewModel.selectedTimezone.collectAsState()
    val language by viewModel.language.collectAsState()
    val adsEnabled by viewModel.adsEnabled.collectAsState()
    val adsMargin by viewModel.adsMargin.collectAsState()
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(
                context,
                if (language == "vi") "Đã cấp quyền thông báo thành công!" else "Notification permission granted!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Translations.getString("system_config", language),
            color = CryptoGold,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp
        )

        // BTC Price Fluctuation Alert Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoSurface),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_btc_alerts")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CryptoGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppIcons.TrendingUp,
                                contentDescription = "BTC",
                                tint = CryptoGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = Translations.getString("btc_price_alerts", language),
                                color = CryptoTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = Translations.getString("btc_price_alerts_desc", language),
                                color = CryptoTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = btcAlertsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            viewModel.setBtcAlertsEnabled(enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CryptoDarkBg,
                            checkedTrackColor = CryptoGold
                        ),
                        modifier = Modifier.testTag("toggle_btc_alerts")
                    )
                }

                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ImpactMedium.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Translations.getString("notification_permission_desc", language),
                                color = ImpactMedium,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                colors = ButtonDefaults.buttonColors(containerColor = ImpactMedium),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = Translations.getString("notification_permission_btn", language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CryptoDarkBg
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = btcAlertsEnabled) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(color = CryptoSurfaceVariant)

                        // Live & Baseline Price Stats
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CryptoDarkBg, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Translations.getString("btc_current_price", language),
                                    color = CryptoTextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$${String.format(Locale.US, "%,.1f", currentBtcPrice)}",
                                    color = CryptoGold,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 16.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Translations.getString("btc_baseline_price", language),
                                    color = CryptoTextSecondary,
                                    fontSize = 11.sp
                                )
                                val baselineDisplay = if (lastBtcPrice > 0f) "$${String.format(Locale.US, "%,.1f", lastBtcPrice)}" else "Chưa đặt"
                                Text(
                                    text = baselineDisplay,
                                    color = CryptoTextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Threshold Selection Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = Translations.getString("btc_threshold_desc", language),
                                color = CryptoTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val thresholds = listOf(1.0f, 2.0f, 3.0f, 5.0f, 10.0f)
                                thresholds.forEach { th ->
                                    val isSelected = btcAlertThreshold == th
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setBtcAlertThreshold(th) },
                                        label = {
                                            Text(
                                                text = "±${th.toInt()}%",
                                                color = if (isSelected) CryptoDarkBg else CryptoTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CryptoGold,
                                            containerColor = CryptoSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("threshold_chip_${th.toInt()}")
                                    )
                                }
                            }
                        }

                        // Status & Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.resetBtcBaselinePrice()
                                    Toast.makeText(
                                        context,
                                        if (language == "vi") "Đã cập nhật mốc so sánh!" else "Baseline price updated!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.PinDrop,
                                    contentDescription = "Reset",
                                    tint = CryptoGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Translations.getString("btc_reset_baseline", language),
                                    color = CryptoGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.sendTestBtcAlert()
                                        Toast.makeText(
                                            context,
                                            Translations.getString("btc_test_alert_toast", language),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CryptoGold)
                            ) {
                                Icon(
                                    imageVector = AppIcons.NotificationsActive,
                                    contentDescription = "Test",
                                    tint = CryptoDarkBg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Translations.getString("btc_send_test_alert", language),
                                    color = CryptoDarkBg,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Background Worker Status Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ImpactSuccess.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(ImpactSuccess, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translations.getString("btc_fluctuation_alert_active", language),
                                color = ImpactSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Notification Toggle Row (for Calendar Events)
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Translations.getString("notifications", language),
                        color = CryptoTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = Translations.getString("notifications_desc", language),
                        color = CryptoTextSecondary,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CryptoDarkBg,
                        checkedTrackColor = CryptoGold
                    ),
                    modifier = Modifier.testTag("toggle_notifications")
                )
            }
        }

        // Language Selection Row
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = Translations.getString("language_setting", language),
                    color = CryptoTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = Translations.getString("language_setting_desc", language),
                    color = CryptoTextSecondary,
                    fontSize = 12.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("en", "vi").forEach { lang ->
                        val isSelected = language == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setLanguage(lang) },
                            label = {
                                Text(
                                    text = if (lang == "en") Translations.getString("english", language) else Translations.getString("vietnamese", language),
                                    color = if (isSelected) CryptoDarkBg else CryptoTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CryptoGold,
                                containerColor = CryptoSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f).testTag("lang_chip_$lang")
                        )
                    }
                }
            }
        }

        // Expanded Timezone Selection Row
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = Translations.getString("timezone", language),
                    color = CryptoTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = Translations.getString("timezone_desc", language),
                    color = CryptoTextSecondary,
                    fontSize = 12.sp
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val zones = listOf("Local", "UTC", "GMT+7", "GMT+8", "GMT+9", "GMT-5")
                    zones.forEach { tz ->
                        val isSelected = selectedTimezone == tz
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedTimezone(tz) },
                            label = {
                                Text(
                                    text = tz,
                                    color = if (isSelected) CryptoDarkBg else CryptoTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CryptoGold,
                                containerColor = CryptoSurfaceVariant
                            ),
                            modifier = Modifier.testTag("timezone_chip_$tz")
                        )
                    }
                }
            }
        }

        // Database & Synchronization Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Translations.getString("db_sync", language),
                    color = CryptoTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Translations.getString("db_sync_desc", language),
                    color = CryptoTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.refreshNewsFeed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(AppIcons.Sync, contentDescription = "Sync", tint = CryptoDarkBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Translations.getString("sync_button", language), color = CryptoDarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: MarketEvent,
    viewModel: CryptoViewModel,
    onClick: () -> Unit
) {
    val watchlistItems by viewModel.watchlistItems.collectAsState()
    val isWatchlisted = watchlistItems.any { it.id == event.id }
    val selectedTimezone by viewModel.selectedTimezone.collectAsState()
    val language by viewModel.language.collectAsState()

    val typeIcon = when (event) {
        is AirdropEvent -> AppIcons.CardGiftcard
        is FedMeetingEvent -> AppIcons.Balance
        is EconomicDataEvent -> AppIcons.Analytics
    }

    val iconColor = when (event) {
        is AirdropEvent -> ImpactSuccess
        is FedMeetingEvent -> Color(0xFF00BFFF) // DeepSkyBlue
        is EconomicDataEvent -> Color(0xFFFFA500) // Orange
    }

    val impactColor = when (event.impact) {
        ImpactLevel.HIGH -> ImpactHigh
        ImpactLevel.MEDIUM -> ImpactMedium
        ImpactLevel.LOW -> ImpactLow
    }

    val impactDisplay = when (event.impact) {
        ImpactLevel.HIGH -> Translations.getString("high", language)
        ImpactLevel.MEDIUM -> Translations.getString("medium", language)
        ImpactLevel.LOW -> Translations.getString("low", language)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CryptoSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = "Event Type",
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (event) {
                            is AirdropEvent -> Translations.getString("airdrop", language).uppercase()
                            is FedMeetingEvent -> Translations.getString("fed_meeting", language).uppercase()
                            is EconomicDataEvent -> Translations.getString("economic_data", language).uppercase()
                        },
                        color = CryptoTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Impact Badge
                    Box(
                        modifier = Modifier
                            .background(impactColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${impactDisplay.uppercase()} ${Translations.getString("impact", language).uppercase()}",
                            color = impactColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Watchlist Icon
                    Icon(
                        imageVector = if (isWatchlisted) Icons.Filled.Star else AppIcons.StarOutline,
                        contentDescription = "Watchlist Toggle",
                        tint = if (isWatchlisted) CryptoGold else CryptoTextSecondary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { viewModel.toggleWatchlist(event) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.title,
                color = CryptoTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${Translations.getString("time", language)}:",
                        color = CryptoTextSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = formatEventTime(event.timestamp, selectedTimezone),
                        color = CryptoTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Countdown Timer component
                CountdownTimer(targetTimeMs = event.timestamp, language = language)
            }
        }
    }
}

@Composable
fun CountdownTimer(targetTimeMs: Long, language: String, isLarge: Boolean = false) {
    var timeLeftMs by remember { mutableStateOf(targetTimeMs - System.currentTimeMillis()) }

    LaunchedEffect(key1 = targetTimeMs) {
        while (true) {
            timeLeftMs = targetTimeMs - System.currentTimeMillis()
            delay(1000)
        }
    }

    val isOver = timeLeftMs <= 0

    val text = if (isOver) {
        if (language == "vi") "Đã Diễn Ra" else "Live/Ended"
    } else {
        val totalSecs = timeLeftMs / 1000
        val days = totalSecs / (24 * 3600)
        val hours = (totalSecs % (24 * 3600)) / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60

        if (days > 0) {
            val dSuffix = Translations.getString("countdown_days", language)
            val hSuffix = Translations.getString("countdown_hours", language)
            "$days$dSuffix $hours$hSuffix"
        } else {
            String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
        }
    }

    val boxColor = if (isOver) CryptoSurfaceVariant else ImpactSuccess.copy(alpha = 0.15f)
    val textColor = if (isOver) CryptoTextSecondary else ImpactSuccess

    Box(
        modifier = Modifier
            .background(boxColor, RoundedCornerShape(if (isLarge) 8.dp else 4.dp))
            .padding(horizontal = if (isLarge) 12.dp else 8.dp, vertical = if (isLarge) 6.dp else 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isOver) {
                Icon(
                    imageVector = AppIcons.Timer,
                    contentDescription = "Timer",
                    tint = ImpactSuccess,
                    modifier = Modifier.size(if (isLarge) 16.dp else 12.dp)
                )
                Spacer(modifier = Modifier.width(if (isLarge) 6.dp else 4.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = if (isLarge) 14.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun EventDetailDialog(
    event: MarketEvent,
    viewModel: CryptoViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val watchlistItems by viewModel.watchlistItems.collectAsState()
    val isWatchlisted = watchlistItems.any { it.id == event.id }
    val selectedTimezone by viewModel.selectedTimezone.collectAsState()
    val language by viewModel.language.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CryptoSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 560.dp)
                .heightIn(max = 680.dp)
                .testTag("event_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translations.getString("event_details", language),
                        color = CryptoGold,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("event_detail_close_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = CryptoTextSecondary)
                    }
                }

                Text(
                    text = event.title,
                    color = CryptoTextPrimary,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                // Timeline / Countdown block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(Translations.getString("impact_label", language), color = CryptoTextSecondary, fontSize = 13.sp)
                        val impactColor = when (event.impact) {
                            ImpactLevel.HIGH -> ImpactHigh
                            ImpactLevel.MEDIUM -> ImpactMedium
                            ImpactLevel.LOW -> ImpactLow
                        }
                        val impactDisplay = when (event.impact) {
                            ImpactLevel.HIGH -> Translations.getString("high", language)
                            ImpactLevel.MEDIUM -> Translations.getString("medium", language)
                            ImpactLevel.LOW -> Translations.getString("low", language)
                        }
                        Text(
                            text = "${impactDisplay.uppercase()} ${Translations.getString("impact", language).uppercase()}",
                            color = impactColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    CountdownTimer(targetTimeMs = event.timestamp, language = language, isLarge = true)
                }

                HorizontalDivider(color = CryptoSurfaceVariant)

                // Specific properties based on the exact event subclass
                when (event) {
                    is AirdropEvent -> {
                        DetailItem(label = Translations.getString("project_name", language), value = event.projectName)
                        event.claimDeadline?.let {
                            DetailItem(label = Translations.getString("claim_deadline", language), value = formatEventTime(it, selectedTimezone))
                        }
                        event.estimatedValue?.let {
                            DetailItem(label = Translations.getString("estimated_value", language), value = it)
                        }
                    }
                    is FedMeetingEvent -> {
                        DetailItem(label = Translations.getString("meeting_type", language), value = event.meetingType)
                        event.expectedRateChange?.let {
                            DetailItem(label = Translations.getString("expected_rate", language), value = it)
                        }
                    }
                    is EconomicDataEvent -> {
                        DetailItem(label = Translations.getString("indicator", language), value = event.indicator)
                        DetailItem(label = Translations.getString("country", language), value = event.country)
                        DetailItem(label = Translations.getString("forecast", language), value = event.forecastValue ?: Translations.getString("tba", language))
                        DetailItem(label = Translations.getString("previous", language), value = event.previousValue ?: Translations.getString("tba", language))
                        DetailItem(label = Translations.getString("actual", language), value = event.actualValue ?: Translations.getString("not_released", language))
                    }
                }

                DetailItem(label = Translations.getString("time", language), value = formatEventTime(event.timestamp, selectedTimezone))

                HorizontalDivider(color = CryptoSurfaceVariant)

                // Actions buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.toggleWatchlist(event) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWatchlisted) CryptoSurfaceVariant else CryptoGold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("event_detail_watchlist_button")
                    ) {
                        Icon(
                            imageVector = if (isWatchlisted) Icons.Filled.Star else AppIcons.StarOutline,
                            contentDescription = "Watchlist",
                            tint = if (isWatchlisted) CryptoGold else CryptoDarkBg
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isWatchlisted) Translations.getString("cancel_remind", language) else Translations.getString("remind_me", language),
                            color = if (isWatchlisted) CryptoTextPrimary else CryptoDarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(event.sourceUrl))
                                context.startActivity(browserIntent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("event_detail_source_button")
                    ) {
                        Icon(AppIcons.Language, contentDescription = "Source", tint = CryptoGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Translations.getString("source", language),
                            color = CryptoGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, color = CryptoTextSecondary, fontSize = 13.sp)
        Text(text = value, color = CryptoTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}

fun formatEventTime(timestamp: Long, timezone: String): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val tz = when (timezone) {
        "Local" -> TimeZone.getDefault()
        "UTC" -> TimeZone.getTimeZone("UTC")
        "GMT+7" -> TimeZone.getTimeZone("GMT+7")
        "GMT+8" -> TimeZone.getTimeZone("GMT+8")
        "GMT+9" -> TimeZone.getTimeZone("GMT+9")
        "GMT-5" -> TimeZone.getTimeZone("GMT-5")
        else -> TimeZone.getTimeZone(timezone)
    }
    format.timeZone = tz
    val suffix = " ($timezone)"
    return format.format(date) + suffix
}
