package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.TechArticle

@Entity(tableName = "cached_articles")
data class CachedArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val category: String,
    val categoryId: String = "",
    val publishDate: String,
    val thumbnailUrl: String? = null,
    val imageResName: String = "",
    val fullDescription: List<String> = emptyList(),
    val whoItsFor: List<String> = emptyList(),
    val keyFeatures: List<String> = emptyList(),
    val appName: String = "",
    val appPackageName: String = "",
    val rating: Float = 4.8f,
    val downloads: String = "",
    val playStoreUrl: String = "",
    val isFavorite: Boolean = false,
    val sourceTag: String = "",
    val views: String = "0",
    val content: String = "",
    val contentFormat: String = "",
    val prompt: String = "",
    val generatePromptEnabled: Boolean = false,
    val generatePrompt: String = "",
    val sceneCount: Int = 0,
    val scenes: List<String> = emptyList(),
    val lastViewedTimestamp: Long = System.currentTimeMillis()
) {
    fun toTechArticle(): TechArticle {
        return TechArticle(
            id = id,
            title = title,
            summary = summary,
            category = category,
            categoryId = categoryId,
            publishDate = publishDate,
            thumbnailUrl = thumbnailUrl,
            imageResName = imageResName,
            fullDescription = fullDescription,
            whoItsFor = whoItsFor,
            keyFeatures = keyFeatures,
            appName = appName,
            appPackageName = appPackageName,
            rating = rating,
            downloads = downloads,
            playStoreUrl = playStoreUrl,
            isFavorite = isFavorite,
            sourceTag = sourceTag,
            views = views,
            content = content,
            contentFormat = contentFormat,
            prompt = prompt,
            generatePromptEnabled = generatePromptEnabled,
            generatePrompt = generatePrompt,
            sceneCount = sceneCount,
            scenes = scenes,
            sceneItems = if (scenes.isNotEmpty()) {
                scenes.mapIndexed { idx, s ->
                    com.example.data.model.SceneItem(
                        part = idx + 1,
                        label = "Part ${idx + 1}",
                        content = s
                    )
                }
            } else emptyList()
        )
    }

    companion object {
        fun fromTechArticle(article: TechArticle, timestamp: Long = System.currentTimeMillis()): CachedArticleEntity {
            return CachedArticleEntity(
                id = article.id,
                title = article.title,
                summary = article.summary,
                category = article.category,
                categoryId = article.categoryId,
                publishDate = article.publishDate,
                thumbnailUrl = article.thumbnailUrl,
                imageResName = article.imageResName,
                fullDescription = article.fullDescription,
                whoItsFor = article.whoItsFor,
                keyFeatures = article.keyFeatures,
                appName = article.appName,
                appPackageName = article.appPackageName,
                rating = article.rating,
                downloads = article.downloads,
                playStoreUrl = article.playStoreUrl,
                isFavorite = article.isFavorite,
                sourceTag = article.sourceTag,
                views = article.views,
                content = article.content,
                contentFormat = article.contentFormat,
                prompt = article.prompt,
                generatePromptEnabled = article.generatePromptEnabled,
                generatePrompt = article.generatePrompt,
                sceneCount = if (article.sceneCount > 0) article.sceneCount else article.sceneItems.size.coerceAtLeast(article.scenes.size),
                scenes = article.scenes.ifEmpty { article.sceneItems.map { it.content } },
                lastViewedTimestamp = timestamp
            )
        }
    }
}
