package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "article_view_counts")
data class ArticleViewCountEntity(
    @PrimaryKey val articleId: String,
    val viewCount: Long = 0L,
    val lastViewedTimestamp: Long = System.currentTimeMillis()
)
