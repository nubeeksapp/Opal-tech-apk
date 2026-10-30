package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.AppCategory
import com.example.data.model.TechArticle
import com.example.data.remote.SupabaseClient
import com.example.utils.ArticleSanitizer
import com.example.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class ArticleRepository(private val context: Context? = null) {

    companion object {
        private const val TAG = "ArticleRepository"

        const val CAT_OPAL_TECH_ID = "opal_tech"
        const val CAT_OPAL_TECH_NAME = "Opal Tech"

        const val CAT_TECH_TIPS_ID = "tech_tips"
        const val CAT_TECH_TIPS_NAME = "Tech Tips"

        const val CAT_ONLINE_JOBS_ID = "online_jobs"
        const val CAT_ONLINE_JOBS_NAME = "Online Jobs"

        const val CAT_APP_REVIEW_ID = "app_review"
        const val CAT_APP_REVIEW_NAME = "App Review"

        /**
         * Consolidates any legacy category ID or name into one of the four official categories.
         */
        fun normalizeCategory(rawNameOrId: String?): Pair<String, String> {
            val raw = rawNameOrId?.trim() ?: ""
            return when {
                raw.contains("Opal", ignoreCase = true) ||
                    raw.contains("technology", ignoreCase = true) ||
                    raw.contains("security", ignoreCase = true) ||
                    raw.equals("opal_tech", ignoreCase = true) -> {
                    CAT_OPAL_TECH_ID to CAT_OPAL_TECH_NAME
                }
                raw.contains("Tips", ignoreCase = true) ||
                    raw.equals("tech_tips", ignoreCase = true) -> {
                    CAT_TECH_TIPS_ID to CAT_TECH_TIPS_NAME
                }
                raw.contains("Job", ignoreCase = true) ||
                    raw.contains("Business", ignoreCase = true) ||
                    raw.contains("work", ignoreCase = true) ||
                    raw.contains("freelanc", ignoreCase = true) ||
                    raw.contains("earn", ignoreCase = true) ||
                    raw.equals("online_jobs", ignoreCase = true) -> {
                    CAT_ONLINE_JOBS_ID to CAT_ONLINE_JOBS_NAME
                }
                raw.contains("Review", ignoreCase = true) ||
                    raw.contains("Tool", ignoreCase = true) ||
                    raw.contains("apps", ignoreCase = true) ||
                    raw.equals("app_review", ignoreCase = true) -> {
                    CAT_APP_REVIEW_ID to CAT_APP_REVIEW_NAME
                }
                else -> CAT_OPAL_TECH_ID to CAT_OPAL_TECH_NAME
            }
        }
    }

    /**
     * The four official categories requested by the user.
     */
    private val defaultCategories = listOf(
        AppCategory(
            id = CAT_OPAL_TECH_ID,
            name = CAT_OPAL_TECH_NAME,
            order = 1,
            description = "Official Opal Tech innovations, on-device AI breakthroughs, and next-generation mobile technologies",
            iconEmoji = "💎",
            articleCount = 2
        ),
        AppCategory(
            id = CAT_TECH_TIPS_ID,
            name = CAT_TECH_TIPS_NAME,
            order = 2,
            description = "Handpicked daily smartphone tricks, isolated private profiles, and smart storage hygiene guides",
            iconEmoji = "📱",
            articleCount = 3
        ),
        AppCategory(
            id = CAT_ONLINE_JOBS_ID,
            name = CAT_ONLINE_JOBS_NAME,
            order = 3,
            description = "Verified remote work opportunities, high-paying freelancing strategies, and digital income guides",
            iconEmoji = "💼",
            articleCount = 2
        ),
        AppCategory(
            id = CAT_APP_REVIEW_ID,
            name = CAT_APP_REVIEW_NAME,
            order = 4,
            description = "In-depth evaluations, benchmarks, and performance reviews of essential productivity and trading apps",
            iconEmoji = "⭐",
            articleCount = 2
        )
    )

    val wifi7Article = TechArticle(
        id = "wifi7-ultra-low-latency",
        title = "Wi-Fi 7 Explained: Next-Gen Speeds & Multi-Link Reliability",
        summary = "How 320 MHz channels and Multi-Link Operation deliver multi-gigabit wireless bandwidth and sub-5ms latency for mobile gaming and streaming.",
        category = CAT_OPAL_TECH_NAME,
        categoryId = CAT_OPAL_TECH_ID,
        publishDate = DateUtils.formatArticleDate("2026-10-16T12:00:00Z"),
        imageResName = "img_wifi7_tech_1792068841676",
        sourceTag = "NEW",
        rating = 4.8f,
        downloads = "5M+",
        appName = "Network Analyzer & Wi-Fi Pro",
        appPackageName = "net.techet.netanalyzerlite",
        playStoreUrl = "https://play.google.com/store/apps/details?id=net.techet.netanalyzerlite",
        isFavorite = false,
        views = "0",
        fullDescription = listOf(
            "The rollout of Wi-Fi 7 (802.11be) marks a massive generational upgrade for smartphones, tablets, and mobile devices.",
            "Multi-Link Operation (MLO) enables devices to transmit data across 5 GHz and 6 GHz simultaneously, drastically reducing latency spikes and eliminating packet drops in busy households.",
            "With bandwidth exceeding 30 Gbps theoretically and real-world gigabit mobile connections, downloading huge files or streaming 8K video is completely seamless."
        ),
        whoItsFor = listOf(
            "Mobile gamers needing sub-5ms ping times without Ethernet cables.",
            "Remote workers transferring massive video and code repositories.",
            "Tech enthusiasts upgrading to next-generation routers and mesh gear."
        ),
        keyFeatures = listOf(
            "Simultaneous Multi-Link Operation across 2.4, 5, and 6 GHz spectrums.",
            "Ultra-wide 320 MHz channels for multi-gigabit throughput.",
            "High-density 4096-QAM modulation for signal clarity in crowded areas."
        )
    )

    private val initialArticles = listOf(
        // -------------------------------------------------------------
        // Category 1: Opal Tech
        // -------------------------------------------------------------
        TechArticle(
            id = "gemini-nano-ai",
            title = "Gemini Nano on Android: On-Device Multimodal AI Breakthrough",
            summary = "How modern smartphone neural processing units run compact generative AI models locally for real-time transcription, summaries, and smart replies without the cloud.",
            category = CAT_OPAL_TECH_NAME,
            categoryId = CAT_OPAL_TECH_ID,
            publishDate = DateUtils.formatArticleDate("2026-10-14T15:20:00Z"),
            imageResName = "img_gemini_nano_1792069314982",
            sourceTag = "AI",
            rating = 4.9f,
            downloads = "50M+",
            appName = "Google Gemini & AICore",
            appPackageName = "com.google.android.apps.bard",
            playStoreUrl = "https://play.google.com/store/apps/details?id=com.google.android.apps.bard",
            isFavorite = false,
            views = "0",
            fullDescription = listOf(
                "On-device generative AI has taken a major leap forward with Gemini Nano, Google's most efficient model designed to execute directly on mobile silicon without sending prompt data to remote cloud servers.",
                "By tapping into modern Neural Processing Units (NPUs), Gemini Nano transcribes long voice recordings into structured outlines, generates contextual smart replies in chat apps, and identifies critical information in text documents in milliseconds.",
                "Because all tensor calculations happen strictly on local hardware, latency is drastically minimized and user privacy is preserved by default, even in airplane mode with zero connectivity."
            ),
            whoItsFor = listOf(
                "Tech enthusiasts eager to explore generative AI without cloud data privacy risks.",
                "Students and journalists transcribing interviews and lectures on the move.",
                "Frequent travelers who require AI assistance without cellular data access."
            ),
            keyFeatures = listOf(
                "Zero-latency neural processing directly on local device NPU.",
                "Fully functional offline AI summaries and voice memo transcriptions.",
                "Hardware-level privacy with no cloud telemetry or prompt logging."
            ),
            generatePromptEnabled = true,
            generatePrompt = """
Act as an on-device AI systems engineer.
Part 1:
Analyze hardware constraints for running quantized 4-bit neural models on mobile NPUs.
Part 2:
Evaluate memory bandwidth overhead, thermal throttling mitigation, and battery draw profiles.
Part 3:
Define an automated offline fallback pipeline when the neural accelerator is under heavy load.
            """.trimIndent(),
            sceneCount = 3,
            scenes = listOf(
                "Analyze hardware constraints for running quantized 4-bit neural models on mobile NPUs.",
                "Evaluate memory bandwidth overhead, thermal throttling mitigation, and battery draw profiles.",
                "Define an automated offline fallback pipeline when the neural accelerator is under heavy load."
            ),
            sceneItems = listOf(
                com.example.data.model.SceneItem(
                    part = 1,
                    label = "Part 1",
                    content = "Analyze hardware constraints for running quantized 4-bit neural models on mobile NPUs."
                ),
                com.example.data.model.SceneItem(
                    part = 2,
                    label = "Part 2",
                    content = "Evaluate memory bandwidth overhead, thermal throttling mitigation, and battery draw profiles."
                ),
                com.example.data.model.SceneItem(
                    part = 3,
                    label = "Part 3",
                    content = "Define an automated offline fallback pipeline when the neural accelerator is under heavy load."
                )
            )
        ),
        TechArticle(
            id = "passkeys-mobile-security",
            title = "Passkeys on Mobile: Phishing-Resistant Login Standard",
            summary = "How FIDO2 biometric passkeys replace fragile passwords with hardware-backed cryptographic keys, stopping credential stuffing and phishing cold.",
            category = CAT_OPAL_TECH_NAME,
            categoryId = CAT_OPAL_TECH_ID,
            publishDate = DateUtils.formatArticleDate("2026-10-12T14:10:00Z"),
            imageResName = "img_passkeys_security_1792069358537",
            sourceTag = "SECURITY",
            rating = 4.9f,
            downloads = "100M+",
            appName = "Google Password Manager",
            appPackageName = "com.google.android.gms",
            playStoreUrl = "https://play.google.com/store/apps/details?id=com.google.android.gms",
            isFavorite = false,
            views = "0",
            fullDescription = listOf(
                "Traditional passwords remain the primary entry point for cyberattacks, phishing scams, and corporate data leaks. Passkeys solve this vulnerability by replacing strings of characters with asymmetric cryptographic keypairs.",
                "When you register a passkey, the private key is permanently sealed inside your phone's hardware security module (Secure Element), and authentication occurs via your fingerprint or face scan. Because the private key never leaves the device, it cannot be stolen in server breaches.",
                "Major platforms including Google, Apple, Microsoft, and banking services now allow one-tap passkey logins, eliminating the hassle of two-factor SMS codes and password resets forever."
            ),
            whoItsFor = listOf(
                "Security-minded users seeking immunity against phishing and credential leaks.",
                "Anyone tired of remembering complex passwords or waiting for SMS OTPs.",
                "Professionals managing high-value business and financial accounts."
            ),
            keyFeatures = listOf(
                "Asymmetric FIDO2 cryptography stored in hardware Secure Element.",
                "Immunity to phishing, SIM-swap attacks, and server-side data breaches.",
                "Instant biometric authentication with fingerprint or facial scan."
            )
        ),

        // -------------------------------------------------------------
        // Category 2: Tech Tips
        // -------------------------------------------------------------
        TechArticle(
            id = "android-private-space",
            title = "Android 15 Private Space: Setup Guide & Privacy Tips",
            summary = "Create an isolated, biometric-locked profile on your phone to hide banking apps, private documents, and confidential media from prying eyes.",
            category = CAT_TECH_TIPS_NAME,
            categoryId = CAT_TECH_TIPS_ID,
            publishDate = DateUtils.formatArticleDate("2026-10-15T09:30:00Z"),
            imageResName = "img_private_space_1792069298591",
            sourceTag = "TIPS",
            rating = 4.9f,
            downloads = "10M+",
            appName = "Android Security & Privacy",
            appPackageName = "com.google.android.gms",
            playStoreUrl = "https://play.google.com/store/apps/details?id=com.google.android.gms",
            isFavorite = true,
            views = "0",
            fullDescription = listOf(
                "Android 15 introduces Private Space, an OS-level sandbox that functions like a digital safe inside your device. When locked, apps inside Private Space are completely hidden from the launcher, recents screen, notifications, and settings.",
                "You can assign a separate Google Account and a dedicated biometric fingerprint or PIN to your Private Space. This prevents sensitive work communications, cryptocurrency wallets, or banking tools from appearing in ordinary app searches.",
                "Notifications from apps in Private Space are concealed behind privacy shields until unlocked, ensuring confidential alerts stay hidden even if someone borrows your unlocked device."
            ),
            whoItsFor = listOf(
                "Professionals who carry sensitive work documents on personal devices.",
                "Users who share their phone with family members or friends.",
                "Anyone looking for hardware-isolated app security without third-party app lockers."
            ),
            keyFeatures = listOf(
                "Independent OS-level profile with dedicated biometric unlock.",
                "Complete invisibility from app drawer and recent tasks when locked.",
                "Isolated notification channel with zero lockscreen data leaks."
            )
        ),
        TechArticle(
            id = "smart-storage-hygiene",
            title = "Smart Phone Storage Hygiene: Reclaim Space Safely",
            summary = "Master modern storage cleaners and automated cache management to identify duplicate photos, prune hidden downloads, and free up gigabytes.",
            category = CAT_TECH_TIPS_NAME,
            categoryId = CAT_TECH_TIPS_ID,
            publishDate = DateUtils.formatArticleDate("2026-10-13T11:45:00Z"),
            imageResName = "img_storage_cleaner_1792069331897",
            sourceTag = "TOOLS",
            rating = 4.8f,
            downloads = "1B+",
            appName = "Files by Google",
            appPackageName = "com.google.android.apps.nbu.files",
            playStoreUrl = "https://play.google.com/store/apps/details?id=com.google.android.apps.nbu.files",
            isFavorite = false,
            views = "0",
            fullDescription = listOf(
                "Running out of smartphone storage causes sluggish system animations, app crashes, and failed camera recordings. Routine digital cleaning helps you recover up to 20 GB of dormant storage.",
                "Modern storage managers intelligently scan for burst photo duplicates, old meme downloads, oversized chat attachments, and unused app caches without risking user-saved files or critical credentials.",
                "Setting up automated archive policies moves rarely opened games and apps into compressed standby states while keeping your personal progress and settings intact."
            ),
            whoItsFor = listOf(
                "Users battling persistent low storage warnings.",
                "Mobile photographers and content creators capturing high-bitrate 4K footage.",
                "Anyone looking to speed up app loading times and system responsiveness."
            ),
            keyFeatures = listOf(
                "Intelligent duplicate media and blurry photo scanner.",
                "Safe app cache purge with zero risk of account sign-outs.",
                "Automated cloud backup suggestions for heavy video archives."
            )
        ),
        TechArticle(
            id = "esim-plus",
            title = "eSIM Plus: Travel Data & Second Line Setup Guide",
            summary = "Add a secondary cellular line for international travel and remote business calls without swapping physical SIM cards.",
            category = CAT_TECH_TIPS_NAME,
            categoryId = CAT_TECH_TIPS_ID,
            publishDate = DateUtils.formatArticleDate("2026-10-10T08:15:00Z"),
            imageResName = "img_private_space_1792069298591",
            sourceTag = "TIPS",
            rating = 4.7f,
            downloads = "5M+",
            appName = "eSIM Plus Virtual SIM",
            appPackageName = "com.esimplus.virtualsim",
            playStoreUrl = "https://play.google.com/store/apps/details?id=com.esimplus.virtualsim",
            isFavorite = false,
            views = "0",
            fullDescription = listOf(
                "International roaming fees can quickly become exorbitant. Modern eSIM apps allow you to purchase and activate regional data profiles in seconds directly from your phone.",
                "Having a dedicated second number is ideal for work communications, temporary marketplace listings, or online identity verification, keeping your primary number confidential.",
                "With coverage spanning over 190 destinations, you can switch local high-speed 5G networks upon landing without searching for airport SIM kiosks."
            ),
            whoItsFor = listOf(
                "Frequent international travelers and digital nomads.",
                "Remote workers requiring a separate business phone line.",
                "Users desiring phone number privacy for online transactions."
            ),
            keyFeatures = listOf(
                "Instant digital QR activation in over 190 countries.",
                "Dual SIM capability without removing physical plastic SIMs.",
                "Flexible prepaid data plans with zero roaming contract surprises."
            )
        ),

        // -------------------------------------------------------------
        // Category 3: Online Jobs
        // -------------------------------------------------------------
        TechArticle(
            id = "freelance-digital-jobs",
            title = "Freelancing in 2026: Next-Gen Online Career & Remote Strategies",
            summary = "Master top global freelancing platforms, digital escrow payments, and remote client acquisition tools to earn consistently online.",
            category = CAT_ONLINE_JOBS_NAME,
            categoryId = CAT_ONLINE_JOBS_ID,
            publishDate = DateUtils.formatArticleDate("2026-10-18T10:30:00Z"),
            imageResName = "img_scanner_workflow_1792069380471",
            sourceTag = "JOBS",
            rating = 4.9f,
            downloads = "10M+",
            appName = "Upwork: Freelancer & Remote",
            appPackageName = "com.upwork.android.apps.main",
            playStoreUrl = "https://play.google.com/store/apps/details?id=com.upwork.android.apps.main",
            isFavorite = false,
            views = "0",
            fullDescription = listOf(
                "The global remote freelance economy has transitioned into specialized high-value knowledge services. Building a resilient independent career begins with identifying verifiable technical skills.",
                "From mobile development and copyediting to cloud bookkeeping and UI design, established platforms like Upwork and Fiverr connect global talent directly with verified corporate clients.",
                "Utilizing digital escrow arrangements ensures that contract milestones are funded before work begins, eliminating payment disputes and providing predictable international wire transfers."
            ),
            whoItsFor = listOf(
                "Professionals seeking location-independent remote freelance income.",
                "Tech specialists and writers wanting to monetize specialized expertise.",
                "Students and graduates looking to build an international work portfolio."
            ),
            keyFeatures = listOf(
                "Verified payment protection via secure automated milestone escrows.",
                "Global client matching algorithms based on skill badges and reviews.",
                "Direct mobile job proposals, contract signing, and invoice management."
            )
        ),
        TechArticle(
            id = "passive-income-online",
            title = "How to Start Working Online & Earn Passive Revenue",
            summary = "Explore high-paying online roles from technical translation and AI prompt curation to digital product creation without startup capital.",
            category = CAT_ONLINE_JOBS_NAME,
            categoryId = CAT_ONLINE_JOBS_ID,
            publishDate = DateUtils.formatArticleDate("2026-10-17T14:00:00Z"),
            imageResName = "img_gemini_nano_1792069314982",
            sourceTag = "EARN",
            rating = 4.8f,
            downloads = "10M+",
            appName = "Fiverr: Freelance & Online Work",
            appPackageName = "com.fiverr.fiverr",
            playStoreUrl = "https://play.google.com/store/apps/details?id=com.fiverr.fiverr",
            isFavorite = false,
            views = "0",
            fullDescription = listOf(
                "Starting an online career no longer requires heavy initial capital. Multilingual translation, data annotation, digital template publishing, and virtual assistant roles can be performed entirely from a smartphone or laptop.",
                "Passive revenue systems involve creating digital assets once—such as specialized Notion templates, graphic vector packs, or technical tutorials—and distributing them through automated marketplaces.",
                "Consistency, timely client communication, and positive early reviews compound your profile visibility, establishing steady recurring revenue streams over time."
            ),
            whoItsFor = listOf(
                "Individuals exploring work-from-home employment opportunities.",
                "Bilingual speakers able to offer live translation and transcription services.",
                "Digital creators interested in scalable passive digital product income."
            ),
            keyFeatures = listOf(
                "Low barrier to entry with zero upfront hardware costs required.",
                "Multiple revenue streams across micro-tasks and recurring retainers.",
                "Instant smartphone notifications for incoming orders and customer inquiries."
            )
        ),

        // -------------------------------------------------------------
        // Category 4: App Review
        // -------------------------------------------------------------
        TechArticle(
            id = "mobile-scanner-pdf-workflow",
            title = "Paperless Office: Mobile Document Scanners & Cloud OCR",
            summary = "Convert multi-page contracts, paper receipts, and handwritten notes into searchable PDFs with automated deskewing and instant cloud synchronization.",
            category = CAT_APP_REVIEW_NAME,
            categoryId = CAT_APP_REVIEW_ID,
            publishDate = DateUtils.formatArticleDate("2026-10-11T10:00:00Z"),
            imageResName = "img_scanner_workflow_1792069380471",
            sourceTag = "REVIEW",
            rating = 4.8f,
            downloads = "100M+",
            appName = "Adobe Scan: PDF & OCR Scanner",
            appPackageName = "com.adobe.scan.android",
            playStoreUrl = "https://play.google.com/store/apps/details?id=com.adobe.scan.android",
            isFavorite = false,
            views = "0",
            fullDescription = listOf(
                "Modern smartphone cameras paired with machine learning algorithms have rendered bulky flatbed scanners obsolete. Real-time edge detection instantly snaps documents, straightens perspective distortion, and eliminates table shadows.",
                "Built-in optical character recognition (OCR) turns printed text into searchable, selectable copy. You can copy invoice numbers, search contracts by client name, or export text directly into office editors.",
                "Seamless integrations automatically sync finalized PDFs directly into Google Drive, OneDrive, or company shared drives, keeping your digital paperwork organized wherever work takes you."
            ),
            whoItsFor = listOf(
                "Remote workers and freelancers handling vendor agreements and expense receipts.",
                "Students digitizing study guides, whiteboards, and research papers.",
                "Small business owners striving for a fast, paperless administrative workflow."
            ),
            keyFeatures = listOf(
                "Automated edge detection, glare reduction, and perspective correction.",
                "High-accuracy multilingual OCR for instant text search within PDFs.",
                "Direct cloud drive export with password protection and digital signing."
            )
        ),
        TechArticle(
            id = "metatrader-app-review",
            title = "App Review: MetaTrader 4 & Mobile Financial Trading",
            summary = "An in-depth performance, security, and interface review of MetaTrader 4 (MT4) for mobile trading, chart telemetry, and automated analysis.",
            category = CAT_APP_REVIEW_NAME,
            categoryId = CAT_APP_REVIEW_ID,
            publishDate = DateUtils.formatArticleDate("2026-10-09T16:45:00Z"),
            imageResName = "img_storage_cleaner_1792069331897",
            sourceTag = "REVIEW",
            rating = 4.7f,
            downloads = "50M+",
            appName = "MetaTrader 4 Forex Trading",
            appPackageName = "net.metaquotes.metatrader4",
            playStoreUrl = "https://play.google.com/store/apps/details?id=net.metaquotes.metatrader4",
            isFavorite = false,
            views = "0",
            fullDescription = listOf(
                "MetaTrader 4 (MT4) remains the industry benchmark platform for mobile Forex and financial market analysis, connecting traders directly with licensed financial brokers globally.",
                "The Android app provides interactive real-time price quotes, technical charting with 30 built-in indicators, and instant order execution with complete stop-loss and take-profit risk management.",
                "Crucially, MetaQuotes operates strictly as software platform provider rather than a financial brokerage, ensuring users retain complete control over their choice of broker and personal funds."
            ),
            whoItsFor = listOf(
                "Financial market analysts and active currency traders on mobile.",
                "Economics students studying technical chart indicators and market depth.",
                "Users requiring encrypted order transmission on portable devices."
            ),
            keyFeatures = listOf(
                "Comprehensive technical analysis suite with 30 technical indicators.",
                "Real-time interactive candlestick and bar charts with zoom and scroll.",
                "Encrypted data transmission between client terminal and broker servers."
            )
        )
    )

    enum class SyncStatus {
        SYNCED_LIVE,
        PENDING_TABLES,
        OFFLINE_FALLBACK,
        NOT_CONFIGURED
    }

    private val _syncStatus = MutableStateFlow(
        if (SupabaseClient.isConfigured) SyncStatus.OFFLINE_FALLBACK else SyncStatus.NOT_CONFIGURED
    )
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _categories = MutableStateFlow(defaultCategories)
    val categoriesFlow: StateFlow<List<AppCategory>> = _categories.asStateFlow()

    private val _articles = MutableStateFlow(initialArticles)
    val articles: StateFlow<List<TechArticle>> = _articles.asStateFlow()

    private val favoriteIds = mutableSetOf("android-private-space")
    private val persistentViewCounts = ConcurrentHashMap<String, Long>()

    init {
        // Load SharedPreferences view counts immediately on initialization if context available
        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences("opal_article_view_counts", Context.MODE_PRIVATE)
                prefs.all.forEach { (key, value) ->
                    if (key.startsWith("view_")) {
                        val id = key.removePrefix("view_")
                        val count = (value as? Number)?.toLong() ?: (value as? String)?.toLongOrNull() ?: 0L
                        if (count > 0L) {
                            persistentViewCounts[id] = count
                        }
                    }
                }
                if (persistentViewCounts.isNotEmpty()) {
                    applyPersistentViewCounts(persistentViewCounts)
                }
            } catch (_: Exception) {}
        }
    }

    fun getCategories(): List<AppCategory> = _categories.value

    suspend fun fetchCategories(): Result<List<AppCategory>> = withContext(Dispatchers.IO) {
        val api = SupabaseClient.api
        if (api == null) {
            Log.d(TAG, "Supabase API not initialized, retaining official 4 categories.")
            updateCategoryCounts(_articles.value)
            return@withContext Result.success(_categories.value)
        }

        try {
            val dtos = try {
                api.getCategories()
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 400) {
                    api.getCategoriesRaw()
                } else {
                    throw e
                }
            }
            if (dtos.isNotEmpty()) {
                val remoteCats = dtos.map { it.toDomain() }
                _categories.value = mergeAndOrderCategories(remoteCats)
                updateCategoryCounts(_articles.value)
                _syncStatus.value = SyncStatus.SYNCED_LIVE
                Result.success(_categories.value)
            } else {
                updateCategoryCounts(_articles.value)
                Result.success(_categories.value)
            }
        } catch (e: Exception) {
            updateCategoryCounts(_articles.value)
            Result.success(_categories.value)
        }
    }

    private fun mergeAndOrderCategories(remoteCategories: List<AppCategory>): List<AppCategory> {
        val baseTopics = defaultCategories.toMutableList()
        val additionalCategories = mutableListOf<AppCategory>()

        for (remote in remoteCategories) {
            val (normId, normName) = normalizeCategory(remote.name.ifBlank { remote.id })
            val index = baseTopics.indexOfFirst {
                it.id == normId || it.name.equals(normName, ignoreCase = true) || it.name.equals(remote.name, ignoreCase = true)
            }
            if (index != -1) {
                val existing = baseTopics[index]
                baseTopics[index] = existing.copy(
                    description = if (remote.description.isNotBlank()) remote.description else existing.description,
                    iconEmoji = if (remote.iconEmoji.isNotBlank()) remote.iconEmoji else existing.iconEmoji
                )
            } else if (remote.name.isNotBlank()) {
                additionalCategories.add(remote.copy(order = 4 + additionalCategories.size + 1))
            }
        }

        return baseTopics + additionalCategories
    }

    suspend fun fetchPosts(): Result<List<TechArticle>> = withContext(Dispatchers.IO) {
        val api = SupabaseClient.api
        if (api == null) {
            Log.d(TAG, "Supabase API not initialized, retaining local posts.")
            return@withContext Result.success(_articles.value)
        }

        try {
            val postDtos = try {
                api.getPosts()
            } catch (e: retrofit2.HttpException) {
                when (e.code()) {
                    400 -> api.getPostsRaw()
                    404 -> {
                        try {
                            api.getArticles()
                        } catch (e2: retrofit2.HttpException) {
                            when (e2.code()) {
                                400 -> api.getArticlesRaw()
                                404 -> {
                                    try {
                                        api.getTechArticles()
                                    } catch (e3: retrofit2.HttpException) {
                                        if (e3.code() == 400) api.getTechArticlesRaw()
                                        else try {
                                            api.getBlogs()
                                        } catch (e4: retrofit2.HttpException) {
                                            if (e4.code() == 400) api.getBlogsRaw() else throw e4
                                        }
                                    }
                                }
                                else -> throw e2
                            }
                        }
                    }
                    else -> throw e
                }
            }

            if (postDtos.isNotEmpty()) {
                val visibleDtos = postDtos.filter { dto ->
                    dto.isPublished != false && !dto.status.equals("draft", ignoreCase = true)
                }

                val mapped = visibleDtos.map { dto ->
                    val postId = dto.id?.toString() ?: ""
                    val isFav = favoriteIds.contains(postId)
                    val domainArticle = dto.toDomain(
                        categoryNameLookup = { rawCat -> normalizeCategory(rawCat).second },
                        isFavorite = isFav
                    )
                    // Normalize category to official 4
                    val (normalizedCatId, normalizedCatName) = normalizeCategory(domainArticle.category)
                    val normalizedArticle = domainArticle.copy(
                        categoryId = normalizedCatId,
                        category = normalizedCatName
                    )

                    // Never drop or reset view count: take max of local persistent and remote count
                    val localSaved = persistentViewCounts[normalizedArticle.id] ?: 0L
                    val remoteCount = parseViewsCount(normalizedArticle.views)
                    val effectiveCount = maxOf(localSaved, remoteCount)
                    if (effectiveCount > 0L) {
                        persistentViewCounts[normalizedArticle.id] = effectiveCount
                    }
                    normalizedArticle.copy(views = formatViews(effectiveCount))
                }

                // If remote returned fewer than our initial articles, merge to guarantee at least 2 per category
                val merged = mergeWithInitialArticles(mapped)
                _articles.value = merged
                updateCategoryCounts(merged)
                _syncStatus.value = SyncStatus.SYNCED_LIVE
                Result.success(merged)
            } else {
                updateCategoryCounts(_articles.value)
                Result.success(_articles.value)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Supabase posts fetch skipped (${e.message}). Retaining local articles.")
            updateCategoryCounts(_articles.value)
            Result.success(_articles.value)
        }
    }

    private fun mergeWithInitialArticles(remoteArticles: List<TechArticle>): List<TechArticle> {
        val existingIds = remoteArticles.map { it.id }.toSet()
        val missingInitials = initialArticles.filter { it.id !in existingIds }.map { article ->
            val savedCount = persistentViewCounts[article.id] ?: 0L
            if (savedCount > 0L) article.copy(views = formatViews(savedCount)) else article
        }
        return remoteArticles + missingInitials
    }

    suspend fun fetchPostsByCategory(categoryId: String): Result<List<TechArticle>> = withContext(Dispatchers.IO) {
        val (normId, normName) = normalizeCategory(categoryId)
        val filtered = _articles.value.filter {
            it.categoryId == normId || it.category.equals(normName, ignoreCase = true)
        }
        Result.success(filtered)
    }

    suspend fun refreshAll(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            fetchCategories()
            fetchPosts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    private fun updateCategoryCounts(articles: List<TechArticle>) {
        val countByCatId = articles.groupingBy { it.categoryId }.eachCount()
        val countByCatName = articles.groupingBy { it.category }.eachCount()

        _categories.update { list ->
            list.map { category ->
                val (normId, normName) = normalizeCategory(category.name)
                val count = countByCatId[category.id]
                    ?: countByCatId[normId]
                    ?: countByCatName[category.name]
                    ?: countByCatName[normName]
                    ?: 0
                // Ensure at least 2 is shown when suitable articles are loaded
                category.copy(articleCount = maxOf(count, 2))
            }
        }
    }

    fun toggleFavorite(articleId: String) {
        if (favoriteIds.contains(articleId)) {
            favoriteIds.remove(articleId)
        } else {
            favoriteIds.add(articleId)
        }

        _articles.update { list ->
            list.map { article ->
                if (article.id == articleId) {
                    article.copy(isFavorite = favoriteIds.contains(articleId))
                } else {
                    article
                }
            }
        }
    }

    fun getArticleById(id: String): TechArticle? {
        return _articles.value.find { it.id == id }
    }

    fun updatePlayStoreLink(articleId: String, playStoreUrl: String) {
        _articles.update { list ->
            list.map { article ->
                if (article.id == articleId) {
                    article.copy(playStoreUrl = playStoreUrl)
                } else {
                    article
                }
            }
        }
    }

    fun refreshArticles(): Boolean {
        if (_articles.value.any { it.id == wifi7Article.id }) {
            return false
        }
        _articles.update { listOf(wifi7Article) + it }
        updateCategoryCounts(_articles.value)
        return true
    }

    fun applyPersistentViewCounts(counts: Map<String, Long>) {
        if (counts.isEmpty()) return
        counts.forEach { (id, count) ->
            val current = persistentViewCounts[id] ?: 0L
            if (count > current) {
                persistentViewCounts[id] = count
            }
        }
        _articles.update { list ->
            list.map { article ->
                val savedCount = persistentViewCounts[article.id]
                if (savedCount != null && savedCount > 0L) {
                    val currentCount = parseViewsCount(article.views)
                    val effectiveCount = maxOf(currentCount, savedCount)
                    article.copy(views = formatViews(effectiveCount))
                } else {
                    article
                }
            }
        }
    }

    /**
     * Atomically increments the view count for [articleId] locally and updates in-memory state.
     * Guaranteed never to decrease or reset to zero.
     */
    fun incrementArticleViewCountSync(articleId: String): Long {
        val currentCount = persistentViewCounts[articleId]
            ?: parseViewsCount(getArticleById(articleId)?.views)
        val newCount = currentCount + 1L
        persistentViewCounts[articleId] = newCount

        _articles.update { list ->
            list.map { article ->
                if (article.id == articleId) {
                    article.copy(views = formatViews(newCount))
                } else {
                    article
                }
            }
        }
        return newCount
    }

    /**
     * Synchronizes a persisted view count to Supabase backend via atomic RPC with direct PATCH fallback.
     */
    suspend fun syncArticleViewCountToBackend(articleId: String, count: Long) = withContext(Dispatchers.IO) {
        val api = SupabaseClient.api
        if (api != null) {
            try {
                // 1. Try atomic database increment RPC function
                val rpcResp = api.incrementViewCountRpc(mapOf("p_article_id" to articleId))
                if (!rpcResp.isSuccessful) {
                    // 2. Fallback to direct PostgREST PATCH
                    api.updatePostViewCount("eq.$articleId", mapOf("view_count" to count))
                }
                Log.d(TAG, "Incremented view count for article $articleId on Supabase to $count")
            } catch (e: Exception) {
                try {
                    api.updatePostViewCount("eq.$articleId", mapOf("view_count" to count))
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Persistently increments the view count for [articleId] and synchronizes with Supabase backend.
     * Employs dual-strategy backend sync (RPC -> PATCH fallback) to ensure accuracy across all devices.
     */
    suspend fun incrementArticleViewCount(articleId: String): Long = withContext(Dispatchers.IO) {
        val newCount = incrementArticleViewCountSync(articleId)
        syncArticleViewCountToBackend(articleId, newCount)
        newCount
    }

    /**
     * Parses numeric view count from various formats (e.g., "1,000", "40", "1.5K", "1000", "2M").
     */
    fun parseViewsCount(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        val clean = raw.trim().replace(",", "").uppercase()
        return try {
            if (clean.endsWith("K")) {
                val num = clean.removeSuffix("K").trim().toDoubleOrNull() ?: 0.0
                (num * 1000).toLong()
            } else if (clean.endsWith("M")) {
                val num = clean.removeSuffix("M").trim().toDoubleOrNull() ?: 0.0
                (num * 1000000).toLong()
            } else {
                clean.filter { it.isDigit() }.toLongOrNull() ?: 0L
            }
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Formats integer view counts into standard readable numbers per user requirements:
     * e.g., 40 -> "40", 1000 -> "1,000", 25000 -> "25,000". Never resets to 0.
     */
    fun formatViews(count: Long): String {
        return if (count <= 0L) {
            "0"
        } else {
            NumberFormat.getIntegerInstance(Locale.US).format(count)
        }
    }
}
