package com.example.domain.model

enum class ImpactLevel { HIGH, MEDIUM, LOW }

sealed class MarketEvent {
    abstract val id: String
    abstract val title: String
    abstract val timestamp: Long
    abstract val impact: ImpactLevel
    abstract val sourceUrl: String
}

data class AirdropEvent(
    override val id: String,
    override val title: String,
    override val timestamp: Long,
    override val impact: ImpactLevel,
    override val sourceUrl: String,
    val projectName: String,
    val claimDeadline: Long?,
    val estimatedValue: String?
) : MarketEvent()

data class FedMeetingEvent(
    override val id: String,
    override val title: String,
    override val timestamp: Long,
    override val impact: ImpactLevel,
    override val sourceUrl: String,
    val meetingType: String, // FOMC, Press Conference, Minutes Release
    val expectedRateChange: String?
) : MarketEvent()

data class EconomicDataEvent(
    override val id: String,
    override val title: String,
    override val timestamp: Long,
    override val impact: ImpactLevel,
    override val sourceUrl: String,
    val indicator: String, // CPI, NFP, GDP...
    val country: String,
    val forecastValue: String?,
    val previousValue: String?,
    val actualValue: String?
) : MarketEvent()
