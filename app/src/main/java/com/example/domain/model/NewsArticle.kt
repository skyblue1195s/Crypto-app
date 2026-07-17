package com.example.domain.model

data class NewsArticle(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val sourceName: String,
    val sourceUrl: String,
    val imageUrl: String?,
    val categories: String,
    val relatedCoins: String
)
