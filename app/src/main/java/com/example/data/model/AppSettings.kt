package com.example.data.model

data class UserPreferences(
    val isDarkMode: Boolean = false,
    val textSize: TextSizePreference = TextSizePreference.MEDIUM,
    val pushNotificationsEnabled: Boolean = true,
    val cacheSizeMB: Double = 65.7,
    val searchHistory: List<String> = emptyList(),
    val expandedSection: SettingsSection? = null
)

enum class SettingsSection {
    GENERAL,
    CACHE,
    PRIVACY,
    ABOUT
}
