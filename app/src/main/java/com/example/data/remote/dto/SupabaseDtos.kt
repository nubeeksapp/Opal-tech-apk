package com.example.data.remote.dto

import com.example.data.model.AppCategory
import com.example.data.model.TechArticle
import com.example.data.remote.SupabaseClient
import com.example.data.repository.ArticleRepository
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseCategoryDto(
    @param:Json(name = "id") val id: Any? = null,
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "title") val title: String? = null,
    @param:Json(name = "order") val order: Int? = 0,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "icon_emoji") val iconEmoji: String? = null,
    @param:Json(name = "icon") val icon: String? = null
) {
    fun toDomain(articleCount: Int = 0): AppCategory {
        val catId = id?.toString()?.trim() ?: ""
        val catName = name?.trim() ?: title?.trim() ?: "General"
        val (normId, normName) = ArticleRepository.normalizeCategory(catName.ifBlank { catId })
        val emoji = iconEmoji?.trim()?.ifBlank { null } ?: when (normId) {
            ArticleRepository.CAT_OPAL_TECH_ID -> "💎"
            ArticleRepository.CAT_TECH_TIPS_ID -> "📱"
            ArticleRepository.CAT_ONLINE_JOBS_ID -> "💼"
            ArticleRepository.CAT_APP_REVIEW_ID -> "⭐"
            else -> "💎"
        }
        val effectiveOrder = order ?: when (normId) {
            ArticleRepository.CAT_OPAL_TECH_ID -> 1
            ArticleRepository.CAT_TECH_TIPS_ID -> 2
            ArticleRepository.CAT_ONLINE_JOBS_ID -> 3
            ArticleRepository.CAT_APP_REVIEW_ID -> 4
            else -> 1
        }
        return AppCategory(
            id = catId.ifBlank { normId },
            name = catName,
            order = effectiveOrder,
            description = description?.trim() ?: "Explore verified guides and updates in $catName",
            iconEmoji = emoji,
            articleCount = articleCount
        )
    }
}

@JsonClass(generateAdapter = true)
data class SupabasePostDto(
    @param:Json(name = "id") val id: Any? = null,
    @param:Json(name = "title") val title: String? = null,
    @param:Json(name = "summary") val summary: String? = null,
    @param:Json(name = "subtitle") val subtitle: String? = null,
    @param:Json(name = "excerpt") val excerpt: String? = null,
    @param:Json(name = "content") val content: String? = null,
    @param:Json(name = "body") val body: String? = null,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "full_description") val fullDescription: Any? = null,
    @param:Json(name = "article_content") val articleContent: String? = null,
    @param:Json(name = "post_content") val postContent: String? = null,
    @param:Json(name = "details") val details: String? = null,
    @param:Json(name = "text") val text: String? = null,
    @param:Json(name = "article") val article: String? = null,
    @param:Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @param:Json(name = "image_url") val imageUrl: String? = null,
    @param:Json(name = "thumbnail") val thumbnail: String? = null,
    @param:Json(name = "image") val image: String? = null,
    @param:Json(name = "cover_image") val coverImage: String? = null,
    @param:Json(name = "featured_image") val featuredImage: String? = null,
    @param:Json(name = "feature_image") val featureImage: String? = null,
    @param:Json(name = "thumb") val thumb: String? = null,
    @param:Json(name = "poster") val poster: String? = null,
    @param:Json(name = "photo") val photo: String? = null,
    @param:Json(name = "banner") val banner: String? = null,
    @param:Json(name = "picture") val picture: String? = null,
    @param:Json(name = "category_id") val categoryId: Any? = null,
    @param:Json(name = "category") val category: Any? = null,
    @param:Json(name = "category_name") val categoryName: String? = null,
    @param:Json(name = "cat_name") val catName: String? = null,
    @param:Json(name = "publish_date") val publishDate: String? = null,
    @param:Json(name = "published_at") val publishedAt: String? = null,
    @param:Json(name = "created_at") val createdAt: String? = null,
    @param:Json(name = "updated_at") val updatedAt: String? = null,
    @param:Json(name = "view_count") val viewCount: Any? = null,
    @param:Json(name = "views") val views: Any? = null,
    @param:Json(name = "play_store_url") val playStoreUrl: String? = null,
    @param:Json(name = "store_url") val storeUrl: String? = null,
    @param:Json(name = "app_url") val appUrl: String? = null,
    @param:Json(name = "link") val link: String? = null,
    @param:Json(name = "url") val url: String? = null,
    @param:Json(name = "target_audience") val targetAudience: Any? = null,
    @param:Json(name = "who_its_for") val whoItsFor: Any? = null,
    @param:Json(name = "key_features") val keyFeatures: Any? = null,
    @param:Json(name = "features") val features: Any? = null,
    @param:Json(name = "app_name") val appName: String? = null,
    @param:Json(name = "app_package_name") val appPackageName: String? = null,
    @param:Json(name = "content_format") val contentFormat: String? = null,
    @param:Json(name = "format") val format: String? = null,
    @param:Json(name = "prompt") val prompt: String? = null,
    @param:Json(name = "code_block") val codeBlock: String? = null,
    @param:Json(name = "code") val code: String? = null,
    @param:Json(name = "prompt_text") val promptText: String? = null,
    @param:Json(name = "ai_prompt") val aiPrompt: String? = null,
    @param:Json(name = "system_prompt") val systemPrompt: String? = null,
    @param:Json(name = "prompts") val prompts: String? = null,
    @param:Json(name = "generate_prompt_enabled") val generatePromptEnabled: Boolean? = null,
    @param:Json(name = "enable_generate_prompt") val enableGeneratePrompt: Boolean? = null,
    @param:Json(name = "is_generate_prompt") val isGeneratePrompt: Boolean? = null,
    @param:Json(name = "prompt_enabled") val promptEnabled: Boolean? = null,
    @param:Json(name = "show_prompt") val showPrompt: Boolean? = null,
    @param:Json(name = "has_prompt") val hasPrompt: Boolean? = null,
    @param:Json(name = "generate_prompt") val generatePrompt: String? = null,
    @param:Json(name = "generated_prompt") val generatedPrompt: String? = null,
    @param:Json(name = "scene_count") val sceneCount: Any? = null,
    @param:Json(name = "scenes_count") val scenesCount: Any? = null,
    @param:Json(name = "parts_count") val partsCount: Any? = null,
    @param:Json(name = "scenes") val scenes: Any? = null,
    @param:Json(name = "scene_parts") val sceneParts: Any? = null,
    @param:Json(name = "parts") val parts: Any? = null,
    @param:Json(name = "scene_list") val sceneList: Any? = null,
    @param:Json(name = "is_published") val isPublished: Boolean? = null,
    @param:Json(name = "status") val status: String? = null
) {
    fun toDomain(
        categoryNameLookup: (String) -> String = { "Tech & App Tips" },
        isFavorite: Boolean = false
    ): TechArticle {
        val postId = id?.toString()?.trim() ?: ""
        val catIdStr = categoryId?.toString()?.trim()
            ?: category?.toString()?.trim()
            ?: ""

        val rawCategoryName = categoryName?.trim()
            ?: catName?.trim()
            ?: (category as? String)?.takeIf { it.isNotBlank() && !it.contains("-") }
        val lookedUpCat = categoryNameLookup(catIdStr).ifBlank { catIdStr }
        val resolvedCatName = rawCategoryName ?: lookedUpCat

        val effectiveTitle = title?.trim() ?: "Tech Recommendation"
        val effectiveSummary = summary?.trim()
            ?: subtitle?.trim()
            ?: excerpt?.trim()
            ?: ""

        val effectiveContent = content?.trim()
            ?: body?.trim()
            ?: description?.trim()
            ?: articleContent?.trim()
            ?: postContent?.trim()
            ?: details?.trim()
            ?: text?.trim()
            ?: article?.trim()
            ?: ""

        val rawThumbnail = thumbnailUrl?.trim()
            ?: imageUrl?.trim()
            ?: thumbnail?.trim()
            ?: image?.trim()
            ?: coverImage?.trim()
            ?: featuredImage?.trim()
            ?: featureImage?.trim()
            ?: thumb?.trim()
            ?: poster?.trim()
            ?: photo?.trim()
            ?: banner?.trim()
            ?: picture?.trim()

        val resolvedThumbnail = resolveStorageUrl(rawThumbnail)

        val effectivePlayStoreUrl = playStoreUrl?.trim()
            ?: storeUrl?.trim()
            ?: appUrl?.trim()
            ?: link?.trim()
            ?: url?.trim()
            ?: ""

        val parsedAudience = parseTargetAudience(targetAudience ?: whoItsFor)
        val parsedFeatures = parseStringList(keyFeatures ?: features)
        val parsedFullDesc = parseStringList(fullDescription).takeIf { it.isNotEmpty() }
            ?: parseContentParagraphs(effectiveContent)

        val effectiveDate = publishDate?.takeIf { it.isNotBlank() }
            ?: publishedAt?.takeIf { it.isNotBlank() }
            ?: createdAt
        val formattedDate = com.example.utils.DateUtils.formatArticleDate(effectiveDate)

        val rawViewCount = viewCount ?: views
        val resolvedViews = when (rawViewCount) {
            is Number -> java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(rawViewCount.toLong())
            is String -> {
                val digitsOnly = rawViewCount.replace(",", "").filter { it.isDigit() }
                val num = digitsOnly.toLongOrNull()
                if (num != null) java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(num)
                else rawViewCount.trim().ifBlank { "0" }
            }
            else -> "0"
        }

        val cleanedTitle = com.example.utils.ArticleSanitizer.cleanPrefix(effectiveTitle)
        val cleanedSummary = com.example.utils.ArticleSanitizer.cleanPrefix(effectiveSummary)
        val cleanedParagraphs = com.example.utils.ArticleSanitizer.cleanList(parsedFullDesc)

        val resolvedFeatures = if (parsedFeatures.isNotEmpty()) {
            parsedFeatures
        } else {
            listOf(
                "Direct verified link to official source & tools.",
                "Streamlined overview with zero intrusive clutter.",
                "Real-time dynamic updates via Supabase cloud backend."
            )
        }

        val resolvedAudience = if (parsedAudience.isNotEmpty()) {
            parsedAudience
        } else {
            listOf(
                "Mobile power users seeking daily tech efficiency.",
                "Smart device users wanting verified digital recommendations.",
                "Tech enthusiasts and digital innovators."
            )
        }

        val resolvedAppName = appName?.trim()?.ifBlank { null }
            ?: cleanedTitle.substringBefore("—").substringBefore(":").trim().ifBlank { "Opal Tech Recommendation" }

        return TechArticle(
            id = postId,
            title = cleanedTitle,
            summary = cleanedSummary,
            category = resolvedCatName.ifBlank { "General" },
            categoryId = catIdStr,
            publishDate = formattedDate,
            thumbnailUrl = resolvedThumbnail,
            imageResName = "img_scanner_workflow_1792069380471",
            fullDescription = if (cleanedParagraphs.isNotEmpty()) cleanedParagraphs else listOf(cleanedSummary),
            whoItsFor = resolvedAudience,
            keyFeatures = resolvedFeatures,
            appName = resolvedAppName,
            appPackageName = appPackageName?.trim() ?: "",
            playStoreUrl = effectivePlayStoreUrl,
            isFavorite = isFavorite,
            sourceTag = "SUPABASE",
            views = resolvedViews,
            content = effectiveContent,
            contentFormat = contentFormat?.trim() ?: format?.trim() ?: "",
            prompt = prompt?.trim()
                ?: aiPrompt?.trim()
                ?: promptText?.trim()
                ?: systemPrompt?.trim()
                ?: prompts?.trim()
                ?: codeBlock?.trim()
                ?: code?.trim()
                ?: "",
            generatePromptEnabled = run {
                val candidatePrompt = generatePrompt?.trim()?.ifBlank { null }
                    ?: prompt?.trim()?.ifBlank { null }
                    ?: aiPrompt?.trim()?.ifBlank { null }
                    ?: promptText?.trim()?.ifBlank { null }
                    ?: generatedPrompt?.trim()?.ifBlank { null }
                    ?: systemPrompt?.trim()?.ifBlank { null }
                    ?: prompts?.trim()?.ifBlank { null }
                    ?: codeBlock?.trim()?.ifBlank { null }
                    ?: code?.trim()?.ifBlank { null }
                    ?: ""

                val explicitlyDisabled = generatePromptEnabled == false ||
                    enableGeneratePrompt == false ||
                    isGeneratePrompt == false ||
                    promptEnabled == false ||
                    showPrompt == false

                val explicitlyEnabled = generatePromptEnabled == true ||
                    enableGeneratePrompt == true ||
                    isGeneratePrompt == true ||
                    promptEnabled == true ||
                    showPrompt == true ||
                    hasPrompt == true

                when {
                    explicitlyDisabled -> false
                    explicitlyEnabled -> candidatePrompt.isNotBlank()
                    candidatePrompt.isNotBlank() -> true
                    else -> false
                }
            },
            generatePrompt = run {
                val candidatePrompt = generatePrompt?.trim()?.ifBlank { null }
                    ?: prompt?.trim()?.ifBlank { null }
                    ?: aiPrompt?.trim()?.ifBlank { null }
                    ?: promptText?.trim()?.ifBlank { null }
                    ?: generatedPrompt?.trim()?.ifBlank { null }
                    ?: systemPrompt?.trim()?.ifBlank { null }
                    ?: prompts?.trim()?.ifBlank { null }
                    ?: codeBlock?.trim()?.ifBlank { null }
                    ?: code?.trim()?.ifBlank { null }
                    ?: ""

                val explicitlyDisabled = generatePromptEnabled == false ||
                    enableGeneratePrompt == false ||
                    isGeneratePrompt == false ||
                    promptEnabled == false ||
                    showPrompt == false

                if (!explicitlyDisabled) candidatePrompt else ""
            },
            sceneCount = when (val sc = sceneCount ?: scenesCount ?: partsCount) {
                is Number -> sc.toInt()
                is String -> sc.trim().toIntOrNull() ?: 0
                else -> 0
            }.let { count ->
                val parsed = parseAdminScenes(scenes ?: sceneParts ?: parts ?: sceneList)
                if (count > 0) count else if (parsed.isNotEmpty()) parsed.size else 0
            },
            scenes = parseAdminScenes(scenes ?: sceneParts ?: parts ?: sceneList)
        )
    }

    companion object {
        fun resolveStorageUrl(raw: String?): String? {
            if (raw.isNullOrBlank()) return null
            val trimmed = raw.trim()
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("data:")) {
                return trimmed
            }
            val baseUrl = SupabaseClient.supabaseUrl.trim()
            if (baseUrl.isBlank() || baseUrl.contains("your-project", ignoreCase = true)) {
                return trimmed
            }
            val normalizedBase = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
            val cleanPath = trimmed.removePrefix("/")
            return when {
                cleanPath.startsWith("storage/v1/object/public/") -> "$normalizedBase/$cleanPath"
                cleanPath.startsWith("storage/v1/object/sign/") -> "$normalizedBase/$cleanPath"
                cleanPath.startsWith("thumbnails/") || cleanPath.startsWith("images/") ||
                    cleanPath.startsWith("covers/") || cleanPath.startsWith("posts/") ||
                    cleanPath.startsWith("public/") -> "$normalizedBase/storage/v1/object/public/$cleanPath"
                else -> "$normalizedBase/storage/v1/object/public/thumbnails/$cleanPath"
            }
        }

        private val jsonAdapter by lazy {
            com.squareup.moshi.Moshi.Builder().build().adapter(Any::class.java)
        }

        fun parseTargetAudience(raw: Any?): List<String> = parseStringList(raw)

        fun parseStringList(raw: Any?): List<String> {
            if (raw == null) return emptyList()
            return when (raw) {
                is List<*> -> {
                    raw.mapNotNull { item ->
                        when (item) {
                            null -> null
                            is Map<*, *> -> extractTextFromMap(item)
                            else -> item.toString().trim().ifEmpty { null }
                        }
                    }.filter { it.isNotBlank() }
                }
                is String -> {
                    val trimmed = raw.trim()
                    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                        val parsed = try {
                            jsonAdapter.fromJson(trimmed)
                        } catch (e: Exception) {
                            null
                        }
                        if (parsed is List<*>) {
                            parsed.mapNotNull { item ->
                                when (item) {
                                    null -> null
                                    is Map<*, *> -> extractTextFromMap(item)
                                    else -> item.toString().trim().ifEmpty { null }
                                }
                            }.filter { it.isNotBlank() }
                        } else {
                            val inner = trimmed.substring(1, trimmed.length - 1).trim()
                            if (inner.isNotEmpty()) {
                                inner.split(",")
                                    .map { it.trim().removeSurrounding("\"").removeSurrounding("'").trim() }
                                    .filter { it.isNotEmpty() }
                            } else {
                                emptyList()
                            }
                        }
                    } else if (trimmed.contains("\n")) {
                        trimmed.lines()
                            .map { it.trim().removePrefix("-").removePrefix("•").removePrefix("*").trim() }
                            .filter { it.isNotEmpty() }
                    } else if (trimmed.contains(",") && trimmed.split(",").all { it.trim().split(Regex("""\s+""")).size <= 5 }) {
                        // Only split on comma for short tag-like phrases, not long sentences with internal commas
                        trimmed.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    } else {
                        if (trimmed.isNotEmpty()) listOf(trimmed) else emptyList()
                    }
                }
                else -> listOf(raw.toString().trim()).filter { it.isNotEmpty() }
            }
        }

        /**
         * Parses scene parts configured by admin panel.
         * Handles:
         * 1. List of JSON objects / Maps e.g. [{"scene": 1, "prompt": "..."}]
         * 2. JSON array string
         * 3. List of strings
         * 4. Multi-line / natural scene markers
         * CRITICAL: Never splits natural sentences on internal commas.
         */
        fun parseAdminScenes(raw: Any?): List<String> {
            if (raw == null) return emptyList()
            return when (raw) {
                is List<*> -> {
                    raw.mapNotNull { item ->
                        when (item) {
                            null -> null
                            is Map<*, *> -> extractTextFromMap(item)
                            is String -> {
                                val trimmed = item.trim()
                                if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                                    try {
                                        val obj = jsonAdapter.fromJson(trimmed)
                                        if (obj is Map<*, *>) extractTextFromMap(obj) else trimmed
                                    } catch (e: Exception) {
                                        trimmed
                                    }
                                } else {
                                    trimmed.ifEmpty { null }
                                }
                            }
                            else -> item.toString().trim().ifEmpty { null }
                        }
                    }.filter { it.isNotBlank() }
                }
                is String -> {
                    val trimmed = raw.trim()
                    if (trimmed.isEmpty()) return emptyList()

                    // 1. JSON Array string or JSON Object string
                    if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
                        try {
                            val parsed = jsonAdapter.fromJson(trimmed)
                            if (parsed is List<*>) {
                                val result = parseAdminScenes(parsed)
                                if (result.isNotEmpty()) return result
                            } else if (parsed is Map<*, *>) {
                                val text = extractTextFromMap(parsed)
                                if (!text.isNullOrBlank()) return listOf(text)
                            }
                        } catch (e: Exception) {
                            // Fallback to text parsing below
                        }
                    }

                    // 2. Natural scene markers (Scene 1:, Part 1:, ---, etc.)
                    val natural = com.example.utils.SceneSplitter.detectNaturalScenes(trimmed)
                    if (natural.size > 1) {
                        return natural
                    }

                    // 3. Double newlines (paragraphs)
                    if (trimmed.contains("\n\n")) {
                        val paragraphs = trimmed.split(Regex("""(?:\r?\n\s*){2,}"""))
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        if (paragraphs.size > 1) return paragraphs
                    }

                    // 4. Preserves full single prompt intact without splitting on internal commas!
                    listOf(trimmed)
                }
                is Map<*, *> -> {
                    val text = extractTextFromMap(raw)
                    if (!text.isNullOrBlank()) listOf(text) else emptyList()
                }
                else -> listOf(raw.toString().trim()).filter { it.isNotEmpty() }
            }
        }

        private fun extractTextFromMap(map: Map<*, *>): String? {
            val candidateKeys = listOf(
                "prompt", "text", "content", "scene_prompt", "scenePrompt",
                "scene_text", "sceneText", "description", "body", "part", "value"
            )
            for (key in candidateKeys) {
                val value = map[key]
                if (value is String && value.isNotBlank()) {
                    return value.trim()
                }
            }
            // Case-insensitive key match
            for ((k, v) in map) {
                val keyStr = k?.toString()?.lowercase() ?: continue
                if (candidateKeys.any { it.lowercase() == keyStr }) {
                    val str = v?.toString()?.trim() ?: ""
                    if (str.isNotBlank()) return str
                }
            }
            // If map has only one meaningful non-ID value
            val nonIdEntries = map.filterKeys {
                val ks = it.toString().lowercase()
                ks != "id" && ks != "scene" && ks != "scene_number" && ks != "part_number" && ks != "number" && ks != "order"
            }
            if (nonIdEntries.size == 1) {
                return nonIdEntries.values.firstOrNull()?.toString()?.trim()?.ifBlank { null }
            }
            return null
        }

        fun parseContentParagraphs(content: String?): List<String> {
            if (content.isNullOrBlank()) return emptyList()
            val paragraphs = content.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
            return if (paragraphs.isNotEmpty()) paragraphs else listOf(content.trim())
        }
    }
}
