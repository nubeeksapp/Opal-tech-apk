package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleViewCountDao {
    @Query("SELECT viewCount FROM article_view_counts WHERE articleId = :articleId")
    suspend fun getViewCount(articleId: String): Long?

    @Query("SELECT * FROM article_view_counts")
    suspend fun getAllViewCounts(): List<ArticleViewCountEntity>

    @Query("SELECT * FROM article_view_counts")
    fun getAllViewCountsFlow(): Flow<List<ArticleViewCountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setViewCount(entity: ArticleViewCountEntity)

    @Query("DELETE FROM article_view_counts")
    suspend fun clearAll()
}
