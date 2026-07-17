package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AirdropEvent
import com.example.domain.model.EconomicDataEvent
import com.example.domain.model.FedMeetingEvent
import com.example.domain.model.ImpactLevel
import com.example.domain.model.MarketEvent
import com.example.domain.model.NewsArticle

@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val sourceName: String,
    val sourceUrl: String,
    val imageUrl: String?,
    val categories: String,
    val relatedCoins: String
) {
    fun toDomain(): NewsArticle {
        return NewsArticle(
            id = id,
            title = title,
            body = body,
            timestamp = timestamp,
            sourceName = sourceName,
            sourceUrl = sourceUrl,
            imageUrl = imageUrl,
            categories = categories,
            relatedCoins = relatedCoins
        )
    }

    companion object {
        fun fromDomain(article: NewsArticle): NewsArticleEntity {
            return NewsArticleEntity(
                id = article.id,
                title = article.title,
                body = article.body,
                timestamp = article.timestamp,
                sourceName = article.sourceName,
                sourceUrl = article.sourceUrl,
                imageUrl = article.imageUrl,
                categories = article.categories,
                relatedCoins = article.relatedCoins
            )
        }
    }
}

@Entity(tableName = "market_events")
data class EventEntity(
    @PrimaryKey val id: String,
    val type: String, // "AIRDROP", "FED_MEETING", "ECONOMIC_DATA"
    val title: String,
    val timestamp: Long,
    val impact: String, // "HIGH", "MEDIUM", "LOW"
    val sourceUrl: String,
    
    // Airdrop specific
    val projectName: String? = null,
    val claimDeadline: Long? = null,
    val estimatedValue: String? = null,
    
    // Fed meeting specific
    val meetingType: String? = null,
    val expectedRateChange: String? = null,
    
    // Economic data specific
    val indicator: String? = null,
    val country: String? = null,
    val forecastValue: String? = null,
    val previousValue: String? = null,
    val actualValue: String? = null
) {
    fun toDomain(): MarketEvent {
        val impactLevel = try {
            ImpactLevel.valueOf(impact)
        } catch (e: Exception) {
            ImpactLevel.LOW
        }
        return when (type) {
            "AIRDROP" -> AirdropEvent(
                id = id,
                title = title,
                timestamp = timestamp,
                impact = impactLevel,
                sourceUrl = sourceUrl,
                projectName = projectName ?: "",
                claimDeadline = claimDeadline,
                estimatedValue = estimatedValue
            )
            "FED_MEETING" -> FedMeetingEvent(
                id = id,
                title = title,
                timestamp = timestamp,
                impact = impactLevel,
                sourceUrl = sourceUrl,
                meetingType = meetingType ?: "",
                expectedRateChange = expectedRateChange
            )
            else -> EconomicDataEvent(
                id = id,
                title = title,
                timestamp = timestamp,
                impact = impactLevel,
                sourceUrl = sourceUrl,
                indicator = indicator ?: "",
                country = country ?: "",
                forecastValue = forecastValue,
                previousValue = previousValue,
                actualValue = actualValue
            )
        }
    }

    companion object {
        fun fromDomain(event: MarketEvent): EventEntity {
            return when (event) {
                is AirdropEvent -> EventEntity(
                    id = event.id,
                    type = "AIRDROP",
                    title = event.title,
                    timestamp = event.timestamp,
                    impact = event.impact.name,
                    sourceUrl = event.sourceUrl,
                    projectName = event.projectName,
                    claimDeadline = event.claimDeadline,
                    estimatedValue = event.estimatedValue
                )
                is FedMeetingEvent -> EventEntity(
                    id = event.id,
                    type = "FED_MEETING",
                    title = event.title,
                    timestamp = event.timestamp,
                    impact = event.impact.name,
                    sourceUrl = event.sourceUrl,
                    meetingType = event.meetingType,
                    expectedRateChange = event.expectedRateChange
                )
                is EconomicDataEvent -> EventEntity(
                    id = event.id,
                    type = "ECONOMIC_DATA",
                    title = event.title,
                    timestamp = event.timestamp,
                    impact = event.impact.name,
                    sourceUrl = event.sourceUrl,
                    indicator = event.indicator,
                    country = event.country,
                    forecastValue = event.forecastValue,
                    previousValue = event.previousValue,
                    actualValue = event.actualValue
                )
            }
        }
    }
}

@Entity(tableName = "watchlist_items")
data class WatchlistItemEntity(
    @PrimaryKey val id: String, // Can be eventId or coinSymbol (e.g. "BTC")
    val title: String,
    val type: String // "COIN" or "EVENT"
)
