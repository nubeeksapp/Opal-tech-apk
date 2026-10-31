package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import com.example.ads.BannerAdView
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fcm.FcmNotificationManager
import com.example.ui.components.OpalBottomNav
import com.example.ui.components.OpalTopHeader
import com.example.ui.screens.AppOpeningScreen
import com.example.ui.screens.CategoryScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.FavoriteScreen
import com.example.ui.screens.RecentFeedScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.OpalTechTheme
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.OpalTechViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private var pendingArticleId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.ads.AdManager.initialize(this)
        extractNotificationPayload(intent)

        setContent {
            val viewModel: OpalTechViewModel = viewModel()
            val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
            var isOpeningDisplayVisible by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000L)
                isOpeningDisplayVisible = false
            }

            LaunchedEffect(pendingArticleId) {
                pendingArticleId?.let { id ->
                    viewModel.openDetail(id)
                    pendingArticleId = null
                    isOpeningDisplayVisible = false
                }
            }

            OpalTechTheme(darkTheme = preferences.isDarkMode) {
                Crossfade(
                    targetState = isOpeningDisplayVisible,
                    animationSpec = tween(durationMillis = 400),
                    label = "opening_crossfade"
                ) { isOpening ->
                    if (isOpening) {
                        AppOpeningScreen(onDismiss = { isOpeningDisplayVisible = false })
                    } else {
                        OpalTechApp(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractNotificationPayload(intent)
    }

    private fun extractNotificationPayload(intent: Intent?) {
        val articleId = intent?.getStringExtra(FcmNotificationManager.EXTRA_ARTICLE_ID)
        if (!articleId.isNullOrBlank()) {
            pendingArticleId = articleId
        }
    }
}

@Composable
fun OpalTechApp(viewModel: OpalTechViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val favoriteArticles by viewModel.favoriteArticles.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val debouncedSearchQuery by viewModel.debouncedSearchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isFeedLoading by viewModel.isFeedLoading.collectAsStateWithLifecycle()
    val isInitialLoading by viewModel.isInitialLoading.collectAsStateWithLifecycle()
    val isCategoriesLoading by viewModel.isCategoriesLoading.collectAsStateWithLifecycle()
    val isCategoryArticlesLoading by viewModel.isCategoryArticlesLoading.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = context as? Activity
    val onOpenArticleWithAd: (String) -> Unit = remember(activity, viewModel) {
        { articleId ->
            if (activity != null) {
                com.example.ads.AdManager.showInterstitialAd(activity) {
                    viewModel.openDetail(articleId)
                }
            } else {
                viewModel.openDetail(articleId)
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.togglePushNotifications(isGranted)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // On the first launch after install, ask for notification permission once on Android 13+
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val prefs = context.getSharedPreferences("opal_tech_prefs", android.content.Context.MODE_PRIVATE)
            val hasAskedPermission = prefs.getBoolean("has_asked_notification_permission", false)
            if (!hasAskedPermission) {
                prefs.edit().putBoolean("has_asked_notification_permission", true).apply()
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    // Automatically sync fresh content whenever the app returns to foreground
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.refreshSilent()
    }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackBarMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Hardware and gesture Back button handler
    BackHandler(enabled = true) {
        if (!viewModel.navigateBack()) {
            // Let system handle default back if at root
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Main -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Top Pill Header
                        val headerTitle = when (selectedTab) {
                            MainTab.RECENT -> "Recent"
                            MainTab.CATEGORY -> selectedCategoryFilter ?: "Category"
                            MainTab.FAVORITE -> "Favorite"
                        }

                        OpalTopHeader(
                            title = headerTitle,
                            searchQuery = searchQuery,
                            isSearchActive = isSearchActive,
                            searchHistory = searchHistory,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onSearchActiveChange = viewModel::setSearchActive,
                            onSubmitSearch = viewModel::submitSearch,
                            onClearSearchAndReturnHome = viewModel::clearSearchAndReturnHome,
                            onSelectHistoryItem = viewModel::submitSearch,
                            onRemoveHistoryItem = viewModel::removeSearchHistoryItem,
                            onClearSearchHistory = viewModel::clearSearchHistory,
                            onOpenSettings = viewModel::openSettings,
                            isInitialLoading = isInitialLoading
                        )

                        // Main Content area based on selected tab
                        Box(modifier = Modifier.weight(1f)) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "tab_content_transition"
                            ) { tab ->
                                when (tab) {
                                     MainTab.RECENT -> {
                                        RecentFeedScreen(
                                            articles = articles,
                                            isRefreshing = isRefreshing,
                                            isLoading = isFeedLoading,
                                            onRefresh = viewModel::refreshFeed,
                                            onArticleClick = onOpenArticleWithAd,
                                            onToggleFavorite = viewModel::toggleFavorite,
                                            categories = categories,
                                            selectedCategory = selectedCategoryFilter,
                                            onSelectCategory = { cat ->
                                                if (cat != null) viewModel.selectCategory(cat) else viewModel.clearCategoryFilter()
                                            },
                                            searchQuery = debouncedSearchQuery,
                                            onClearSearchAndReturnHome = viewModel::clearSearchAndReturnHome
                                        )
                                    }
                                    MainTab.CATEGORY -> {
                                        CategoryScreen(
                                            categories = categories,
                                            selectedCategory = selectedCategoryFilter,
                                            categoryArticles = articles,
                                            isLoadingCategories = isCategoriesLoading,
                                            isLoadingCategoryArticles = isCategoryArticlesLoading,
                                            onSelectCategory = viewModel::selectCategory,
                                            onBackToCategories = viewModel::clearCategoryFilter,
                                            onArticleClick = onOpenArticleWithAd,
                                            onToggleFavorite = viewModel::toggleFavorite
                                        )
                                    }
                                    MainTab.FAVORITE -> {
                                        FavoriteScreen(
                                            favorites = favoriteArticles,
                                            onArticleClick = onOpenArticleWithAd,
                                            onToggleFavorite = viewModel::toggleFavorite,
                                            onExploreClick = { viewModel.selectTab(MainTab.RECENT) }
                                        )
                                    }
                                }
                            }
                        }

                        // Fixed Banner Ad directly above Bottom Nav (matches Screenshots 2 & 3)
                        BannerAdView(modifier = Modifier.fillMaxWidth())

                        // Bottom Navigation Bar with 3 tabs
                        OpalBottomNav(
                            selectedTab = selectedTab,
                            onTabSelected = viewModel::selectTab
                        )
                    }
                }

                is Screen.Detail -> {
                    val article = articles.find { it.id == screen.articleId } ?: viewModel.getArticle(screen.articleId)
                    if (article != null) {
                        val recommended = remember(article, articles) {
                            articles.filter {
                                it.id != article.id && (
                                    (it.categoryId.isNotBlank() && it.categoryId == article.categoryId) ||
                                    (it.category.isNotBlank() && it.category.equals(article.category, ignoreCase = true))
                                )
                            }.take(4)
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DetailScreen(
                                    article = article,
                                    textSizePreference = preferences.textSize,
                                    onBack = { viewModel.navigateBack() },
                                    onToggleFavorite = { viewModel.toggleFavorite(article.id) },
                                    onCycleTextSize = viewModel::cycleTextSize,
                                    recommendedArticles = recommended,
                                    onArticleClick = onOpenArticleWithAd,
                                    onToggleFavoriteArticle = viewModel::toggleFavorite
                                )
                            }
                            BannerAdView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                            )
                        }
                    } else {
                        // Fallback if article not found
                        viewModel.navigateBack()
                    }
                }

                is Screen.Settings -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SettingsScreen(
                                preferences = preferences,
                                onBack = { viewModel.navigateBack() },
                                onToggleSection = viewModel::toggleSettingsSection,
                                onToggleDarkMode = viewModel::toggleDarkMode,
                                onSelectTextSize = viewModel::setTextSize,
                                onTogglePushNotifications = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                    viewModel.togglePushNotifications(enabled)
                                },
                                onSendTestNotification = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                    viewModel.sendTestDailyTipNotification()
                                },
                                onClearCache = viewModel::clearCache,
                                onClearSearchHistory = viewModel::clearSearchHistory,
                                isSupabaseConfigured = viewModel.isSupabaseConfigured,
                                syncStatus = syncStatus,
                                fcmToken = fcmToken
                            )
                        }
                        BannerAdView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                        )
                    }
                }
            }
        }
    }
}
