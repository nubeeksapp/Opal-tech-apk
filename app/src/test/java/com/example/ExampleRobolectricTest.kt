package com.example

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TextSizePreference
import com.example.data.repository.ArticleRepository
import com.example.data.repository.SearchHistoryManager
import com.example.fcm.FcmNotificationManager
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.OpalTechViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @org.junit.Before
    fun setUp() = kotlinx.coroutines.runBlocking {
        val app = ApplicationProvider.getApplicationContext<Context>()
        val cacheRepo = com.example.data.local.ArticleCacheRepository(app)
        cacheRepo.clearViewCountsForTesting()
    }

    @Test
    fun `read string from context matches Opal Tech`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Opal Tech", appName)
    }

    @Test
    fun `repository has initial articles and categories`() {
        val repository = ArticleRepository()
        val articles = repository.articles.value
        val categories = repository.getCategories()

        assertTrue(articles.isNotEmpty())
        assertTrue(categories.isNotEmpty())

        val privateSpaceArticle = repository.getArticleById("android-private-space")
        assertNotNull(privateSpaceArticle)
        assertEquals("Tech Tips", privateSpaceArticle?.category)
    }

    @Test
    fun `viewModel toggles tabs and favorites`() {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val repository = ArticleRepository()
        val viewModel = OpalTechViewModel(app, repository)

        viewModel.selectTab(MainTab.CATEGORY)
        assertEquals(MainTab.CATEGORY, viewModel.selectedTab.value)

        viewModel.cycleTextSize()
        assertEquals(TextSizePreference.LARGE, viewModel.userPreferences.value.textSize)
    }

    @Test
    fun `search history saves queries and clears properly`() {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val repository = ArticleRepository()
        val viewModel = OpalTechViewModel(app, repository)

        viewModel.submitSearch("Cyber Security")
        assertTrue(viewModel.searchHistory.value.contains("Cyber Security"))

        viewModel.removeSearchHistoryItem("Cyber Security")
        assertTrue(!viewModel.searchHistory.value.contains("Cyber Security"))

        viewModel.clearSearchHistory()
        assertTrue(viewModel.searchHistory.value.isEmpty())
    }

    @Test
    fun `viewModel can be instantiated by AndroidViewModelFactory`() {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        val viewModel = factory.create(OpalTechViewModel::class.java)
        assertNotNull(viewModel)
    }

    @Test
    fun `article playStoreUrl is configured and can be updated by admin link`() {
        val repository = ArticleRepository()
        val privateSpace = repository.getArticleById("android-private-space")
        assertNotNull(privateSpace)

        val newUrl = "https://play.google.com/store/apps/details?id=com.google.android.gms.updated"
        repository.updatePlayStoreLink("android-private-space", newUrl)
        val updated = repository.getArticleById("android-private-space")
        assertEquals(newUrl, updated?.playStoreUrl)
    }

    @Test
    fun `pull to refresh loads new articles`() {
        val repository = ArticleRepository()
        val initialCount = repository.articles.value.size
        val added = repository.refreshArticles()
        assertTrue(added)
        assertEquals(initialCount + 1, repository.articles.value.size)
        assertEquals("wifi7-ultra-low-latency", repository.articles.value.first().id)
    }

    @Test
    fun `viewModel provides loading states for feed and categories`() {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val repository = ArticleRepository()
        val viewModel = OpalTechViewModel(app, repository)

        assertNotNull(viewModel.isFeedLoading.value)
        assertNotNull(viewModel.isCategoriesLoading.value)
        assertNotNull(viewModel.isCategoryArticlesLoading.value)
        assertNotNull(viewModel.isInitialLoading.value)
    }

    @Test
    fun `removing search query navigates back to home feed`() {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val repository = ArticleRepository()
        val viewModel = OpalTechViewModel(app, repository)

        viewModel.selectTab(MainTab.CATEGORY)
        viewModel.submitSearch("Cyber")
        assertEquals("Cyber", viewModel.searchQuery.value)

        // Clear query returns to Home (RECENT tab)
        viewModel.updateSearchQuery("")
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(MainTab.RECENT, viewModel.selectedTab.value)

        // Explicit clearSearchAndReturnHome
        viewModel.submitSearch("Security")
        viewModel.clearSearchAndReturnHome()
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(MainTab.RECENT, viewModel.selectedTab.value)
    }

    @Test
    fun `fcm notification channel is registered successfully`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FcmNotificationManager.createNotificationChannel(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = notificationManager.getNotificationChannel(FcmNotificationManager.CHANNEL_ID)
        assertNotNull(channel)
        assertEquals(FcmNotificationManager.CHANNEL_NAME, channel.name)
    }

    @Test
    fun `togglePushNotifications updates preferences and subscription state`() {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val repository = ArticleRepository()
        val viewModel = OpalTechViewModel(app, repository)

        assertTrue(viewModel.userPreferences.value.pushNotificationsEnabled)

        viewModel.togglePushNotifications(false)
        assertFalse(viewModel.userPreferences.value.pushNotificationsEnabled)
        assertFalse(viewModel.isFcmSubscribed.value)

        viewModel.togglePushNotifications(true)
        assertTrue(viewModel.userPreferences.value.pushNotificationsEnabled)
        assertTrue(viewModel.isFcmSubscribed.value)
    }

    @Test
    fun `triggerSampleDailyTipNotification delivers top daily tech tip`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tip = FcmNotificationManager.triggerSampleDailyTipNotification(context)

        assertNotNull(tip)
        assertTrue(tip.title.isNotEmpty())
        assertTrue(tip.body.isNotEmpty())
    }

    @Test
    fun `search history manager persists queries using DataStore`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SearchHistoryManager(context)

        val updated = manager.addQuerySync("Android Tech")
        assertTrue(updated.contains("Android Tech"))
        assertTrue(manager.getSearchHistory().contains("Android Tech"))

        manager.removeQuerySync("Android Tech")
        assertFalse(manager.getSearchHistory().contains("Android Tech"))
    }

    @Test
    fun `room database caches viewed articles and maintains limit of 10`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cacheRepo = com.example.data.local.ArticleCacheRepository(context)
        cacheRepo.clearCache()

        val repository = ArticleRepository()
        val initialArticles = repository.articles.value

        // Cache 12 articles
        for (i in 0 until minOf(12, initialArticles.size)) {
            val article = initialArticles[i]
            cacheRepo.cacheArticleView(article)
        }

        val cachedArticles = cacheRepo.getLast10CachedArticles()
        assertTrue(cachedArticles.isNotEmpty())
        assertTrue(cachedArticles.size <= 10)

        // Verify retrieval by id
        val firstId = cachedArticles.first().id
        val retrieved = cacheRepo.getCachedArticleById(firstId)
        assertNotNull(retrieved)
        assertEquals(firstId, retrieved?.id)

        // Clear cache
        cacheRepo.clearCache()
        assertEquals(0, cacheRepo.getCachedCount())
    }

    @Test
    fun `search input updates immediately and debounces for filtering`() = kotlinx.coroutines.runBlocking {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val repository = ArticleRepository()
        val viewModel = OpalTechViewModel(app, repository)

        // Typing updates searchQuery instantly without waiting
        viewModel.updateSearchQuery("VPN")
        assertEquals("VPN", viewModel.searchQuery.value)

        // Submitting immediately syncs debounced query
        viewModel.submitSearch("VPN")
        assertEquals("VPN", viewModel.debouncedSearchQuery.value)

        // Clearing query resets both immediately
        viewModel.updateSearchQuery("")
        assertEquals("", viewModel.searchQuery.value)
        assertEquals("", viewModel.debouncedSearchQuery.value)
    }

    @Test
    fun `category filter recognizes admin categories and flexible names`() {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val repository = ArticleRepository()
        val viewModel = OpalTechViewModel(app, repository)

        // Select category with variant name
        viewModel.selectCategory("Tech and Tips")
        assertEquals("Tech and Tips", viewModel.selectedCategoryFilter.value)

        val filteredArticles = viewModel.articles.value
        // Articles under "Tech & App Tips" are recommended
        assertTrue(filteredArticles.any { it.title.contains("Private Space", ignoreCase = true) })
    }

    @Test
    fun `article view count increments from zero when opened and read`() = kotlinx.coroutines.runBlocking {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val repository = ArticleRepository()
        val viewModel = OpalTechViewModel(app, repository)

        val firstArticle = repository.articles.value.first()
        assertEquals("0", firstArticle.views)

        viewModel.openDetail(firstArticle.id)
        kotlinx.coroutines.delay(100L)

        val updated = repository.articles.value.first { it.id == firstArticle.id }
        assertEquals("1", updated.views)
    }

    @Test
    fun `view counts persist in room database and survive refresh and reopening`() = kotlinx.coroutines.runBlocking {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val cacheRepo = com.example.data.local.ArticleCacheRepository(app)
        val repository = ArticleRepository()
        val viewModel = OpalTechViewModel(app, repository)

        val targetArticle = repository.articles.value.first()
        viewModel.openDetail(targetArticle.id)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        kotlinx.coroutines.delay(50L)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        // View count incremented to 1
        assertEquals("1", repository.articles.value.first { it.id == targetArticle.id }.views)

        // Open same article again
        viewModel.openDetail(targetArticle.id)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        kotlinx.coroutines.delay(50L)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals("2", repository.articles.value.first { it.id == targetArticle.id }.views)

        // Simulate refresh
        viewModel.refreshFeed()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        kotlinx.coroutines.delay(50L)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        // View count should remain 2 after refresh, not reset to 0 or 1
        val afterRefresh = repository.articles.value.first { it.id == targetArticle.id }
        assertEquals("2", afterRefresh.views)

        // Simulate new app session / new ViewModel instance loading saved counts
        val newRepo = ArticleRepository()
        val savedCounts = cacheRepo.getAllViewCounts()
        newRepo.applyPersistentViewCounts(savedCounts)
        val reloadedArticle = newRepo.articles.value.first { it.id == targetArticle.id }
        assertEquals("2", reloadedArticle.views)
    }

    @Test
    fun `dark mode toggle persists across viewModel instances`() {
        val app = ApplicationProvider.getApplicationContext<Context>() as Application
        val repository = ArticleRepository()
        val viewModel1 = OpalTechViewModel(app, repository)

        viewModel1.toggleDarkMode(true)
        assertTrue(viewModel1.userPreferences.value.isDarkMode)

        // New viewModel instance should restore dark mode as true
        val viewModel2 = OpalTechViewModel(app, repository)
        assertTrue(viewModel2.userPreferences.value.isDarkMode)

        // Reset back to light mode for clean state
        viewModel2.toggleDarkMode(false)
        assertFalse(viewModel2.userPreferences.value.isDarkMode)
    }

    @Test
    fun `article sanitizer removes unwanted prefixes`() {
        val raw1 = "SIM2:- Add a 2nd line for travel and work"
        val raw2 = "CALL 2: Real-time spam call blocker"
        val raw3 = "OR2: Instant messaging and calls"
        val raw4 = "PHO1:- AI photo cleaner tool"

        assertEquals("Add a 2nd line for travel and work", com.example.utils.ArticleSanitizer.cleanPrefix(raw1))
        assertEquals("Real-time spam call blocker", com.example.utils.ArticleSanitizer.cleanPrefix(raw2))
        assertEquals("Instant messaging and calls", com.example.utils.ArticleSanitizer.cleanPrefix(raw3))
        assertEquals("AI photo cleaner tool", com.example.utils.ArticleSanitizer.cleanPrefix(raw4))
    }
}
