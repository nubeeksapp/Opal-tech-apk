package com.example.data.model

data class TechArticle(
    val id: String,
    val title: String,
    val summary: String,
    val category: String,
    val categoryId: String = "",
    val publishDate: String,
    val thumbnailUrl: String? = null,
    val imageResName: String = "",
    val fullDescription: List<String>,
    val whoItsFor: List<String>,
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
    val scenes: List<String> = emptyList()
)

enum class TextSizePreference(val displayName: String, val scale: Float) {
    SMALL("Small", 0.85f),
    MEDIUM("Default (Medium)", 1.0f),
    LARGE("Large", 1.18f),
    EXTRA_LARGE("Extra Large", 1.35f)
}

data class AppCategory(
    val id: String,
    val name: String,
    val order: Int = 0,
    val description: String = "",
    val iconEmoji: String = "📱",
    val articleCount: Int = 0
)
