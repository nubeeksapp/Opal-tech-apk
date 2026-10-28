package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

private val Context.searchHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "opal_tech_search_history")

class SearchHistoryManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefs = context.getSharedPreferences("opal_tech_search_history_prefs", Context.MODE_PRIVATE)

    private val inMemoryCache = mutableListOf<String>()

    init {
        // Seed in-memory cache synchronously from private SharedPreferences for zero cold-start delay
        try {
            val saved = prefs.getString("search_queries", "") ?: ""
            if (saved.isNotBlank()) {
                val list = saved.split(SEPARATOR).filter { it.isNotBlank() }
                synchronized(inMemoryCache) {
                    inMemoryCache.clear()
                    inMemoryCache.addAll(list)
                }
            }
        } catch (_: Exception) {}
    }

    val searchHistoryFlow: Flow<List<String>> = context.searchHistoryDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val serialized = preferences[KEY_SEARCH_HISTORY]
            if (serialized.isNullOrBlank()) {
                synchronized(inMemoryCache) {
                    if (inMemoryCache.isNotEmpty()) inMemoryCache.toList() else emptyList()
                }
            } else {
                val list = serialized.split(SEPARATOR).filter { it.isNotBlank() }
                synchronized(inMemoryCache) {
                    inMemoryCache.clear()
                    inMemoryCache.addAll(list)
                }
                list
            }
        }

    fun getSearchHistory(): List<String> {
        return synchronized(inMemoryCache) {
            inMemoryCache.toList()
        }
    }

    suspend fun addQuery(query: String): List<String> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return getSearchHistory()

        val updated: List<String>
        synchronized(inMemoryCache) {
            inMemoryCache.removeAll { it.equals(trimmed, ignoreCase = true) }
            inMemoryCache.add(0, trimmed)
            while (inMemoryCache.size > MAX_HISTORY_ITEMS) {
                inMemoryCache.removeAt(inMemoryCache.size - 1)
            }
            updated = inMemoryCache.toList()
        }

        try {
            prefs.edit().putString("search_queries", updated.joinToString(SEPARATOR)).apply()
            context.searchHistoryDataStore.edit { preferences ->
                preferences[KEY_SEARCH_HISTORY] = updated.joinToString(SEPARATOR)
            }
        } catch (_: Exception) {}
        return updated
    }

    suspend fun removeQuery(query: String): List<String> {
        val updated: List<String>
        synchronized(inMemoryCache) {
            inMemoryCache.removeAll { it.equals(query, ignoreCase = true) }
            updated = inMemoryCache.toList()
        }

        try {
            prefs.edit().putString("search_queries", updated.joinToString(SEPARATOR)).apply()
            context.searchHistoryDataStore.edit { preferences ->
                preferences[KEY_SEARCH_HISTORY] = updated.joinToString(SEPARATOR)
            }
        } catch (_: Exception) {}
        return updated
    }

    suspend fun clearHistory() {
        synchronized(inMemoryCache) {
            inMemoryCache.clear()
        }
        try {
            prefs.edit().putString("search_queries", "").apply()
            context.searchHistoryDataStore.edit { preferences ->
                preferences[KEY_SEARCH_HISTORY] = ""
            }
        } catch (_: Exception) {}
    }

    fun addQuerySync(query: String): List<String> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return getSearchHistory()
        val updated: List<String>
        synchronized(inMemoryCache) {
            inMemoryCache.removeAll { it.equals(trimmed, ignoreCase = true) }
            inMemoryCache.add(0, trimmed)
            while (inMemoryCache.size > MAX_HISTORY_ITEMS) {
                inMemoryCache.removeAt(inMemoryCache.size - 1)
            }
            updated = inMemoryCache.toList()
        }
        try {
            prefs.edit().putString("search_queries", updated.joinToString(SEPARATOR)).apply()
        } catch (_: Exception) {}
        scope.launch {
            try {
                context.searchHistoryDataStore.edit { preferences ->
                    preferences[KEY_SEARCH_HISTORY] = updated.joinToString(SEPARATOR)
                }
            } catch (_: Exception) {}
        }
        return updated
    }

    fun removeQuerySync(query: String): List<String> {
        val updated: List<String>
        synchronized(inMemoryCache) {
            inMemoryCache.removeAll { it.equals(query, ignoreCase = true) }
            updated = inMemoryCache.toList()
        }
        try {
            prefs.edit().putString("search_queries", updated.joinToString(SEPARATOR)).apply()
        } catch (_: Exception) {}
        scope.launch {
            try {
                context.searchHistoryDataStore.edit { preferences ->
                    preferences[KEY_SEARCH_HISTORY] = updated.joinToString(SEPARATOR)
                }
            } catch (_: Exception) {}
        }
        return updated
    }

    fun clearHistorySync() {
        synchronized(inMemoryCache) {
            inMemoryCache.clear()
        }
        try {
            prefs.edit().putString("search_queries", "").apply()
        } catch (_: Exception) {}
        scope.launch {
            try {
                context.searchHistoryDataStore.edit { preferences ->
                    preferences[KEY_SEARCH_HISTORY] = ""
                }
            } catch (_: Exception) {}
        }
    }

    companion object {
        private val KEY_SEARCH_HISTORY = stringPreferencesKey("search_history_queries")
        private const val SEPARATOR = "\n"
        private const val MAX_HISTORY_ITEMS = 15
    }
}
