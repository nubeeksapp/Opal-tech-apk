package com.example.data.local

import android.content.Context
import com.example.data.model.TechArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository responsible for local caching of articles and permanent persistence of view counts.
 * View counts are preserved in both Room Database and SharedPreferences for instantaneous
 * startup recovery, zero-latency retrieval, and immunity from cache eviction.
 */
class ArticleCacheRepository(context: Context) {
    private val database = OpalTechDatabase.getInstance(context)
    private val dao = database.cachedArticleDao()
    private val viewCountDao = database.articleViewCountDao()
    private val prefs = context.getSharedPreferences("opal_article_view_counts", Context.MODE_PRIVATE)

    val cachedArticlesFlow: Flow<List<TechArticle>> = dao.getCachedArticlesFlow()
        .map { list -> list.map { it.toTechArticle() } }

    val persistentViewCountsFlow: Flow<Map<String, Long>> = viewCountDao.getAllViewCountsFlow()
        .map { list ->
            val fromRoom = list.associate { it.articleId to it.viewCount }
            val fromPrefs = getAllPrefsViewCounts()
            val combined = (fromRoom.keys + fromPrefs.keys).associateWith { id ->
                maxOf(fromRoom[id] ?: 0L, fromPrefs[id] ?: 0L)
            }
            combined
        }

    private fun getAllPrefsViewCounts(): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        try {
            prefs.all.forEach { (key, value) ->
                if (key.startsWith("view_")) {
                    val articleId = key.removePrefix("view_")
                    val count = (value as? Number)?.toLong() ?: (value as? String)?.toLongOrNull() ?: 0L
                    if (count > 0L) {
                        result[articleId] = count
                    }
                }
            }
        } catch (_: Exception) {}
        return result
    }

    suspend fun getAllViewCounts(): Map<String, Long> {
        val fromPrefs = getAllPrefsViewCounts()
        val fromRoom = try {
            viewCountDao.getAllViewCounts().associate { it.articleId to it.viewCount }
        } catch (e: Exception) {
            emptyMap()
        }
        val combined = (fromRoom.keys + fromPrefs.keys).associateWith { id ->
            maxOf(fromRoom[id] ?: 0L, fromPrefs[id] ?: 0L)
        }
        return combined
    }

    suspend fun getViewCount(articleId: String): Long {
        val prefsCount = try {
            prefs.getLong("view_$articleId", 0L)
        } catch (_: Exception) {
            0L
        }
        val roomCount = try {
            viewCountDao.getViewCount(articleId) ?: 0L
        } catch (e: Exception) {
            0L
        }
        return maxOf(prefsCount, roomCount)
    }

    suspend fun setViewCount(articleId: String, count: Long) {
        if (count <= 0L) return
        try {
            val existing = getViewCount(articleId)
            val effective = maxOf(existing, count)

            // 1. Persist to SharedPreferences immediately (synchronous / durable)
            prefs.edit().putLong("view_$articleId", effective).apply()

            // 2. Persist to Room Database
            viewCountDao.setViewCount(
                ArticleViewCountEntity(
                    articleId = articleId,
                    viewCount = effective,
                    lastViewedTimestamp = System.currentTimeMillis()
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun cacheArticleView(article: TechArticle) {
        val entity = CachedArticleEntity.fromTechArticle(article)
        dao.insertArticle(entity)
        dao.pruneOldestExceptTop10()
    }

    suspend fun cacheArticles(articles: List<TechArticle>) {
        if (articles.isEmpty()) return
        val entities = articles.map { CachedArticleEntity.fromTechArticle(it) }
        dao.insertArticles(entities)
    }

    suspend fun getCachedArticleById(id: String): TechArticle? {
        return dao.getArticleById(id)?.toTechArticle()
    }

    suspend fun getLast10CachedArticles(): List<TechArticle> {
        return dao.getLast10CachedArticles().map { it.toTechArticle() }
    }

    suspend fun getCachedCount(): Int {
        return dao.getCachedCount()
    }

    suspend fun clearCache() {
        // Only clear temporary offline article content cache.
        // View counts are permanent per user specifications and must NEVER be cleared.
        dao.clearAll()
    }

    suspend fun clearViewCountsForTesting() {
        try {
            prefs.edit().clear().commit()
            viewCountDao.clearAll()
        } catch (_: Exception) {}
    }
}
