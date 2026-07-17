package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class NewsResponseDto(
    @Json(name = "Data") val data: List<NewsItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class NewsItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "published_on") val publishedOn: Long,
    @Json(name = "imageurl") val imageUrl: String?,
    @Json(name = "title") val title: String,
    @Json(name = "url") val url: String,
    @Json(name = "source") val source: String,
    @Json(name = "body") val body: String,
    @Json(name = "categories") val categories: String? = null
)

interface CryptoCompareService {
    @GET("data/v2/news/")
    suspend fun getLatestNews(
        @Query("lang") lang: String = "EN",
        @Query("api_key") apiKey: String? = null
    ): NewsResponseDto
}

interface CoinGeckoService {
    @GET("api/v3/simple/price")
    suspend fun getSimplePrices(
        @Query("ids") ids: String = "bitcoin,ethereum,solana,binancecoin,cardano",
        @Query("vs_currencies") vsCurrencies: String = "usd",
        @Query("include_24hr_change") include24hChange: Boolean = true
    ): Map<String, Map<String, Double>>
}
