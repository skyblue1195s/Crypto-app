package com.example

import com.example.data.local.EventEntity
import com.example.domain.model.AirdropEvent
import com.example.domain.model.EconomicDataEvent
import com.example.domain.model.FedMeetingEvent
import com.example.domain.model.ImpactLevel
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAirdropEventMapping() {
        val entity = EventEntity(
            id = "airdrop_1",
            type = "AIRDROP",
            title = "Berachain Claim",
            timestamp = 1718000000000L,
            impact = "HIGH",
            sourceUrl = "https://berachain.com",
            projectName = "Berachain",
            claimDeadline = 1719000000000L,
            estimatedValue = "1000 USD"
        )
        
        val domain = entity.toDomain()
        assertTrue(domain is AirdropEvent)
        val airdrop = domain as AirdropEvent
        assertEquals("airdrop_1", airdrop.id)
        assertEquals("Berachain Claim", airdrop.title)
        assertEquals(ImpactLevel.HIGH, airdrop.impact)
        assertEquals("Berachain", airdrop.projectName)
        assertEquals(1719000000000L, airdrop.claimDeadline)
        assertEquals("1000 USD", airdrop.estimatedValue)
    }

    @Test
    fun testFedMeetingEventMapping() {
        val entity = EventEntity(
            id = "fomc_1",
            type = "FED_MEETING",
            title = "FOMC Meeting Sep 2026",
            timestamp = 1718100000000L,
            impact = "MEDIUM",
            sourceUrl = "https://fed.gov",
            meetingType = "Interest Rate Announcement",
            expectedRateChange = "-25 bps"
        )
        
        val domain = entity.toDomain()
        assertTrue(domain is FedMeetingEvent)
        val fed = domain as FedMeetingEvent
        assertEquals("fomc_1", fed.id)
        assertEquals(ImpactLevel.MEDIUM, fed.impact)
        assertEquals("Interest Rate Announcement", fed.meetingType)
        assertEquals("-25 bps", fed.expectedRateChange)
    }

    @Test
    fun testEconomicDataEventMapping() {
        val entity = EventEntity(
            id = "eco_1",
            type = "ECONOMIC_DATA",
            title = "US CPI Release",
            timestamp = 1718200000000L,
            impact = "HIGH",
            sourceUrl = "https://bls.gov",
            indicator = "CPI YoY",
            country = "United States",
            forecastValue = "3.1%",
            previousValue = "3.3%",
            actualValue = "3.0%"
        )
        
        val domain = entity.toDomain()
        assertTrue(domain is EconomicDataEvent)
        val eco = domain as EconomicDataEvent
        assertEquals("eco_1", eco.id)
        assertEquals(ImpactLevel.HIGH, eco.impact)
        assertEquals("CPI YoY", eco.indicator)
        assertEquals("United States", eco.country)
        assertEquals("3.1%", eco.forecastValue)
        assertEquals("3.3%", eco.previousValue)
        assertEquals("3.0%", eco.actualValue)
    }
}
