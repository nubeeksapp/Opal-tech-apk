package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedArticleDao {
    @Query("SELECT * FROM cached_articles ORDER BY lastViewedTimestamp DESC LIMIT 10")
    fun getCachedArticlesFlow(): Flow<List<CachedArticleEntity>>

    @Query("SELECT * FROM cached_articles ORDER BY lastViewedTimestamp DESC LIMIT 10")
    suspend fun getLast10CachedArticles(): List<CachedArticleEntity>

    @Query("SELECT * FROM cached_articles WHERE id = :articleId LIMIT 1")
    suspend fun getArticleById(articleId: String): CachedArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: CachedArticleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<CachedArticleEntity>)

    @Query("DELETE FROM cached_articles WHERE id NOT IN (SELECT id FROM cached_articles ORDER BY lastViewedTimestamp DESC LIMIT 10)")
    suspend fun pruneOldestExceptTop10()

    @Query("SELECT COUNT(*) FROM cached_articles")
    suspend fun getCachedCount(): Int

    @Query("DELETE FROM cached_articles")
    suspend fun clearAll()
}
