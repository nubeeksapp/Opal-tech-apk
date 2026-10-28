package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ArticleCacheRepository
import com.example.data.model.AppCategory
import com.example.data.model.SettingsSection
import com.example.data.model.TechArticle
import com.example.data.model.TextSizePreference
import com.example.data.model.UserPreferences
import com.example.data.remote.SupabaseClient
import com.example.data.repository.ArticleRepository
import com.example.data.repository.SearchHistoryManager
import com.example.fcm.FcmNotificationManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab {
    RECENT,
    CATEGORY,
    FAVORITE
}

sealed class Screen {
    data object Main : Screen()
    data class Detail(val articleId: String) : Screen()
    data object Settings : Screen()
}

@OptIn(kotlinx.coroutines.FlowPreview::class)
class OpalTechViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ArticleRepository = ArticleRepository(application),
    private val searchHistoryManager: SearchHistoryManager = SearchHistoryManager(application),
    private val cacheRepository: ArticleCacheRepository = ArticleCacheRepository(application)
) : AndroidViewModel(application) {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Main)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val cachedArticles: StateFlow<List<TechArticle>> = cacheRepository.cachedArticlesFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedTab = MutableStateFlow(MainTab.RECENT)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _debouncedSearchQuery = MutableStateFlow("")
    val debouncedSearchQuery: StateFlow<String> = _debouncedSearchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(searchHistoryManager.getSearchHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isFeedLoading = MutableStateFlow(true)
    val isFeedLoading: StateFlow<Boolean> = _isFeedLoading.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _isCategoriesLoading = MutableStateFlow(true)
    val isCategoriesLoading: StateFlow<Boolean> = _isCategoriesLoading.asStateFlow()

    private val _isCategoryArticlesLoading = MutableStateFlow(false)
    val isCategoryArticlesLoading: StateFlow<Boolean> = _isCategoryArticlesLoading.asStateFlow()

    private val prefs by lazy {
        getApplication<Application>().getSharedPreferences("opal_tech_user_prefs", Context.MODE_PRIVATE)
    }

    private val _userPreferences = MutableStateFlow(
        UserPreferences(
            isDarkMode = getApplication<Application>()
                .getSharedPreferences("opal_tech_user_prefs", Context.MODE_PRIVATE)
                .getBoolean("is_dark_mode", false),
            textSize = run {
                val p = getApplication<Application>().getSharedPreferences("opal_tech_user_prefs", Context.MODE_PRIVATE)
                val savedName = p.getString("text_size", TextSizePreference.MEDIUM.name)
                TextSizePreference.entries.find { it.name == savedName } ?: TextSizePreference.MEDIUM
            },
            searchHistory = searchHistoryManager.getSearchHistory()
        )
    )
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    val categories: StateFlow<List<AppCategory>> = repository.categoriesFlow

    val isSupabaseConfigured: Boolean = SupabaseClient.isConfigured
    val supabaseUrl: String = SupabaseClient.supabaseUrl

    private fun filterArticles(
        repoArticles: List<TechArticle>,
        cachedList: List<TechArticle>,
        query: String,
        categoryFilter: String?
    ): List<TechArticle> {
        val sourceArticles = if (repoArticles.isEmpty() && cachedList.isNotEmpty()) cachedList else repoArticles
        return sourceArticles.filter { article ->
            val matchesCategory = if (categoryFilter == null) {
                true
            } else {
                val (filterNormId, filterNormName) = ArticleRepository.normalizeCategory(categoryFilter)
                val (articleNormId, articleNormName) = ArticleRepository.normalizeCategory(article.category)

                article.category.equals(categoryFilter, ignoreCase = true) ||
                    article.categoryId.equals(categoryFilter, ignoreCase = true) ||
                    article.categoryId.equals(filterNormId, ignoreCase = true) ||
                    articleNormId.equals(filterNormId, ignoreCase = true) ||
                    articleNormName.equals(filterNormName, ignoreCase = true) ||
                    article.category.contains(filterNormName, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                article.title.contains(query, ignoreCase = true) ||
                article.summary.contains(query, ignoreCase = true) ||
                article.category.contains(query, ignoreCase = true) ||
                article.appName.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    private fun filterFavorites(
        allArticles: List<TechArticle>,
        query: String
    ): List<TechArticle> {
        return allArticles.filter { it.isFavorite }.filter { article ->
            query.isBlank() ||
                article.title.contains(query, ignoreCase = true) ||
                article.summary.contains(query, ignoreCase = true)
        }
    }

    private val _articles = MutableStateFlow(
        filterArticles(repository.articles.value, emptyList(), "", null)
    )
    val articles: StateFlow<List<TechArticle>> = _articles.asStateFlow()

    private val _favoriteArticles = MutableStateFlow(
        filterFavorites(repository.articles.value, "")
    )
    val favoriteArticles: StateFlow<List<TechArticle>> = _favoriteArticles.asStateFlow()

    val syncStatus: StateFlow<ArticleRepository.SyncStatus> = repository.syncStatus

    val fcmToken: StateFlow<String?> = FcmNotificationManager.fcmToken
    val isFcmSubscribed: StateFlow<Boolean> = FcmNotificationManager.isSubscribed

    init {
        try {
            FcmNotificationManager.initialize(getApplication())
        } catch (e: Exception) {
            // Safe fallback for testing environments
        }
        viewModelScope.launch {
            searchHistoryManager.searchHistoryFlow.collectLatest { history ->
                _searchHistory.value = history
                _userPreferences.update { it.copy(searchHistory = history) }
            }
        }
        viewModelScope.launch {
            _searchQuery
                .debounce(350L)
                .collectLatest { debounced ->
                    _debouncedSearchQuery.value = debounced
                }
        }
        viewModelScope.launch {
            combine(
                repository.articles,
                cacheRepository.cachedArticlesFlow,
                _debouncedSearchQuery,
                _selectedCategoryFilter
            ) { repoArticles, cachedList, query, categoryFilter ->
                filterArticles(repoArticles, cachedList, query, categoryFilter)
            }.collect { filtered ->
                _articles.value = filtered
            }
        }
        viewModelScope.launch {
            combine(
                repository.articles,
                _debouncedSearchQuery
            ) { allArticles, query ->
                filterFavorites(allArticles, query)
            }.collect { filteredFavs ->
                _favoriteArticles.value = filteredFavs
            }
        }
        viewModelScope.launch {
            // Load persistent view counts immediately on startup
            val initialSaved = cacheRepository.getAllViewCounts()
            if (initialSaved.isNotEmpty()) {
                repository.applyPersistentViewCounts(initialSaved)
            }
            cacheRepository.persistentViewCountsFlow.collectLatest { counts ->
                if (counts.isNotEmpty()) {
                    repository.applyPersistentViewCounts(counts)
                }
            }
        }
        loadInitialData()
        startRealtimeArticleSync()
    }

    private fun startRealtimeArticleSync() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(20000L)
                val postsResult = repository.fetchPosts()
                postsResult.getOrNull()?.let { freshArticles ->
                    if (freshArticles.isNotEmpty()) {
                        val savedCounts = cacheRepository.getAllViewCounts()
                        if (savedCounts.isNotEmpty()) {
                            repository.applyPersistentViewCounts(savedCounts)
                        }
                        cacheRepository.cacheArticles(freshArticles)
                    }
                }
            }
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _isFeedLoading.value = true
            _isCategoriesLoading.value = true
            _isRefreshing.value = true

            // Ensure persisted view counts are loaded
            val initialCounts = cacheRepository.getAllViewCounts()
            if (initialCounts.isNotEmpty()) {
                repository.applyPersistentViewCounts(initialCounts)
            }

            repository.fetchCategories()
            _isCategoriesLoading.value = false

            val postsResult = repository.fetchPosts()
            postsResult.getOrNull()?.let { freshArticles ->
                if (freshArticles.isNotEmpty()) {
                    val savedCounts = cacheRepository.getAllViewCounts()
                    if (savedCounts.isNotEmpty()) {
                        repository.applyPersistentViewCounts(savedCounts)
                    }
                    cacheRepository.cacheArticles(freshArticles)
                }
            }
            _isFeedLoading.value = false
            _isRefreshing.value = false
            _isInitialLoading.value = false

            when (repository.syncStatus.value) {
                ArticleRepository.SyncStatus.SYNCED_LIVE -> {
                    _snackBarMessage.emit("Connected to Supabase: Content synced live")
                }
                ArticleRepository.SyncStatus.PENDING_TABLES -> {
                    // Graceful fallback to bundled content
                }
                else -> {}
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
        if (tab != MainTab.CATEGORY) {
            _selectedCategoryFilter.value = null
        }
    }

    private val lastArticleOpenTimestamps = java.util.concurrent.ConcurrentHashMap<String, Long>()

    fun openDetail(articleId: String) {
        val now = System.currentTimeMillis()
        val lastTime = lastArticleOpenTimestamps[articleId] ?: 0L
        val isDebounced = (now - lastTime) < 20L
        lastArticleOpenTimestamps[articleId] = now

        // If user was searching, save search query to persistent search history
        if (_searchQuery.value.isNotBlank()) {
            val q = _searchQuery.value.trim()
            val updated = searchHistoryManager.addQuerySync(q)
            _searchHistory.value = updated
            _userPreferences.update { it.copy(searchHistory = updated) }
        }

        if (!isDebounced) {
            val newViews = repository.incrementArticleViewCountSync(articleId)
            viewModelScope.launch {
                // Persist increment in Room database, SharedPreferences, and Supabase backend
                cacheRepository.setViewCount(articleId, newViews)
                repository.syncArticleViewCountToBackend(articleId, newViews)
                val article = getArticle(articleId)
                if (article != null) {
                    cacheRepository.cacheArticleView(article)
                }
            }
        }
        _currentScreen.value = Screen.Detail(articleId)
    }

    fun openSettings() {
        _isSearchActive.value = false
        _currentScreen.value = Screen.Settings
    }

    fun clearSearchAndReturnHome() {
        _searchQuery.value = ""
        _debouncedSearchQuery.value = ""
        _isSearchActive.value = false
        _selectedCategoryFilter.value = null
        _selectedTab.value = MainTab.RECENT
    }

    fun navigateBack(): Boolean {
        if (_currentScreen.value !is Screen.Main) {
            _currentScreen.value = Screen.Main
            return true
        }
        if (_searchQuery.value.isNotEmpty()) {
            clearSearchAndReturnHome()
            return true
        }
        if (_isSearchActive.value) {
            _isSearchActive.value = false
            return true
        }
        if (_selectedCategoryFilter.value != null && _selectedTab.value == MainTab.CATEGORY) {
            _selectedCategoryFilter.value = null
            return true
        }
        return false
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _debouncedSearchQuery.value = query
        if (query.isEmpty()) {
            _selectedTab.value = MainTab.RECENT
        }
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
    }

    fun submitSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotEmpty()) {
            val updated = searchHistoryManager.addQuerySync(trimmed)
            _searchHistory.value = updated
            _userPreferences.update { it.copy(searchHistory = updated) }
            viewModelScope.launch {
                searchHistoryManager.addQuery(trimmed)
            }
        }
        _searchQuery.value = trimmed
        _debouncedSearchQuery.value = trimmed
        _isSearchActive.value = false
    }

    fun removeSearchHistoryItem(query: String) {
        val updated = searchHistoryManager.removeQuerySync(query)
        _searchHistory.value = updated
        _userPreferences.update { it.copy(searchHistory = updated) }
        viewModelScope.launch {
            searchHistoryManager.removeQuery(query)
        }
    }

    fun clearSearchHistory() {
        searchHistoryManager.clearHistorySync()
        _searchHistory.value = emptyList()
        _userPreferences.update { it.copy(searchHistory = emptyList()) }
        viewModelScope.launch {
            searchHistoryManager.clearHistory()
            _snackBarMessage.emit("Search history cleared")
        }
    }

    fun selectCategory(category: AppCategory) {
        val (normId, normName) = ArticleRepository.normalizeCategory(category.name)
        _selectedCategoryFilter.value = category.name
        _articles.value = filterArticles(
            repository.articles.value,
            cachedArticles.value,
            _debouncedSearchQuery.value,
            category.name
        )
        viewModelScope.launch {
            _isCategoryArticlesLoading.value = true
            repository.fetchPostsByCategory(normId)
            _isCategoryArticlesLoading.value = false
        }
    }

    fun selectCategory(categoryName: String) {
        val (normId, normName) = ArticleRepository.normalizeCategory(categoryName)
        _selectedCategoryFilter.value = categoryName
        _articles.value = filterArticles(
            repository.articles.value,
            cachedArticles.value,
            _debouncedSearchQuery.value,
            categoryName
        )
        viewModelScope.launch {
            _isCategoryArticlesLoading.value = true
            repository.fetchPostsByCategory(normId)
            _isCategoryArticlesLoading.value = false
        }
    }

    fun clearCategoryFilter() {
        _selectedCategoryFilter.value = null
        _articles.value = filterArticles(
            repository.articles.value,
            cachedArticles.value,
            _debouncedSearchQuery.value,
            null
        )
    }

    fun toggleFavorite(articleId: String) {
        repository.toggleFavorite(articleId)
        viewModelScope.launch {
            val isNowFavorite = repository.getArticleById(articleId)?.isFavorite == true
            val msg = if (isNowFavorite) "Saved to Favorites" else "Removed from Favorites"
            _snackBarMessage.emit(msg)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _userPreferences.update { it.copy(isDarkMode = enabled) }
        try {
            prefs.edit().putBoolean("is_dark_mode", enabled).apply()
        } catch (_: Exception) {}
        viewModelScope.launch {
            _snackBarMessage.emit(if (enabled) "Dark mode enabled" else "Light mode enabled")
        }
    }

    fun cycleTextSize() {
        val nextSize = when (_userPreferences.value.textSize) {
            TextSizePreference.SMALL -> TextSizePreference.MEDIUM
            TextSizePreference.MEDIUM -> TextSizePreference.LARGE
            TextSizePreference.LARGE -> TextSizePreference.EXTRA_LARGE
            TextSizePreference.EXTRA_LARGE -> TextSizePreference.SMALL
        }
        setTextSize(nextSize)
    }

    fun setTextSize(size: TextSizePreference) {
        _userPreferences.update { it.copy(textSize = size) }
        try {
            prefs.edit().putString("text_size", size.name).apply()
        } catch (_: Exception) {}
        viewModelScope.launch {
            _snackBarMessage.emit("Text size set to ${size.displayName}")
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        _userPreferences.update { it.copy(pushNotificationsEnabled = enabled) }
        FcmNotificationManager.updateSubscription(getApplication(), enabled)
        viewModelScope.launch {
            _snackBarMessage.emit(
                if (enabled) "Subscribed to Top Daily Tech Tips"
                else "Unsubscribed from Daily Tech Tips"
            )
        }
    }

    fun sendTestDailyTipNotification() {
        val tip = FcmNotificationManager.triggerSampleDailyTipNotification(getApplication())
        viewModelScope.launch {
            _snackBarMessage.emit("Sent daily tip notification: ${tip.title}")
        }
    }

    fun toggleSettingsSection(section: SettingsSection) {
        // Maintained cleanly in SettingsScreen UI state
    }

    fun clearCache() {
        val previous = _userPreferences.value.cacheSizeMB
        _userPreferences.update { it.copy(cacheSizeMB = 0.0) }
        viewModelScope.launch {
            cacheRepository.clearCache()
            _snackBarMessage.emit("Cache cleared: Offline articles removed")
        }
    }

    fun getArticle(id: String): TechArticle? {
        val memArticle = repository.getArticleById(id)
        if (memArticle != null) return memArticle
        return cachedArticles.value.find { it.id == id }
    }

    fun updatePlayStoreLink(articleId: String, playStoreUrl: String) {
        repository.updatePlayStoreLink(articleId, playStoreUrl)
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _isFeedLoading.value = true
            repository.refreshAll()
            // Re-apply persisted view counts so refresh never resets counts
            val savedCounts = cacheRepository.getAllViewCounts()
            if (savedCounts.isNotEmpty()) {
                repository.applyPersistentViewCounts(savedCounts)
            }
            val currentArticles = repository.articles.value
            if (currentArticles.isNotEmpty()) {
                cacheRepository.cacheArticles(currentArticles)
            }
            _isFeedLoading.value = false
            _isRefreshing.value = false
            when (repository.syncStatus.value) {
                ArticleRepository.SyncStatus.SYNCED_LIVE -> {
                    _snackBarMessage.emit("Latest articles synced from Supabase!")
                }
                ArticleRepository.SyncStatus.PENDING_TABLES -> {
                    _snackBarMessage.emit("Feed up to date (Supabase tables pending in SQL Editor)")
                }
                else -> {
                    _snackBarMessage.emit("Feed is up to date")
                }
            }
        }
    }
}
