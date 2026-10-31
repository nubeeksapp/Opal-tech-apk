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
    @param:Json(name = "link_info") val linkInfo: Any? = null,
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

        val promptResult = parsePromptAndAudience(
            targetAudienceRaw = targetAudience,
            whoItsForRaw = whoItsFor,
            post = this
        )

        val effectivePlayStoreUrl = playStoreUrl?.trim()?.ifBlank { null }
            ?: storeUrl?.trim()?.ifBlank { null }
            ?: appUrl?.trim()?.ifBlank { null }
            ?: link?.trim()?.ifBlank { null }
            ?: url?.trim()?.ifBlank { null }
            ?: promptResult.extractedLink?.trim()?.ifBlank { null }
            ?: ""

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
            emptyList()
        }

        // Clean audience list: strictly genuine audience bullet items or emptyList()
        val resolvedAudience = promptResult.audience

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
            prompt = promptResult.generatePrompt,
            generatePromptEnabled = promptResult.generatePromptEnabled,
            generatePrompt = promptResult.generatePrompt,
            sceneCount = promptResult.sceneCount,
            scenes = promptResult.scenes,
            sceneItems = promptResult.sceneItems
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
            val cleanBase = baseUrl.substringBefore("/rest/v1").trimEnd('/')
            val normalizedBase = if (cleanBase.endsWith("/")) cleanBase.dropLast(1) else cleanBase
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

        data class PromptAndAudienceResult(
            val generatePromptEnabled: Boolean,
            val generatePrompt: String,
            val sceneCount: Int,
            val scenes: List<String>,
            val sceneItems: List<com.example.data.model.SceneItem>,
            val audience: List<String>,
            val extractedLink: String? = null
        )

        fun parsePromptAndAudience(
            targetAudienceRaw: Any?,
            whoItsForRaw: Any?,
            post: SupabasePostDto
        ): PromptAndAudienceResult {
            var targetMap: Map<*, *>? = null
            var directAudienceList: List<String>? = null

            when (targetAudienceRaw) {
                is Map<*, *> -> {
                    targetMap = targetAudienceRaw
                }
                is String -> {
                    val trimmed = targetAudienceRaw.trim()
                    if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                        trimmed.contains("\"link_info\"") || trimmed.contains("\"code_blocks\"")) {
                        try {
                            val parsed = jsonAdapter.fromJson(trimmed)
                            if (parsed is Map<*, *>) targetMap = parsed
                        } catch (_: Exception) {}
                    } else if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                        directAudienceList = parseCleanAudienceList(trimmed)
                    } else if (trimmed.isNotEmpty() && !trimmed.contains("\"code_blocks\"") && !trimmed.contains("\"link_info\"")) {
                        directAudienceList = parseCleanAudienceList(trimmed)
                    }
                }
                is List<*> -> {
                    directAudienceList = parseCleanAudienceList(targetAudienceRaw)
                }
            }

            var enabledVal: Any? = null
            var promptVal: Any? = null
            var numScenesVal: Any? = null
            var scenesVal: Any? = null
            var extractedAudience: List<String>? = directAudienceList

            // 1. Extract link_info or direct URL from targetMap / post
            var extractedLink: String? = null
            if (targetMap != null) {
                val linkObj = targetMap["link_info"] ?: targetMap["linkInfo"] ?: targetMap["link"]
                extractedLink = extractLinkUrl(linkObj)
                    ?: extractLinkUrl(targetMap["play_store_url"])
                    ?: extractLinkUrl(targetMap["playStoreUrl"])
                    ?: extractLinkUrl(targetMap["url"])
            }
            if (extractedLink.isNullOrBlank()) {
                extractedLink = extractLinkUrl(post.linkInfo)
                    ?: extractLinkUrl(post.link)
                    ?: extractLinkUrl(post.playStoreUrl)
                    ?: extractLinkUrl(post.storeUrl)
                    ?: extractLinkUrl(post.appUrl)
                    ?: extractLinkUrl(post.url)
            }
            if (extractedLink.isNullOrBlank() && targetAudienceRaw is String) {
                extractedLink = extractUrlByRegex(targetAudienceRaw)
            }

            if (targetMap != null) {
                val gpObj = targetMap["generate_prompt"] ?: targetMap["generatePrompt"]
                val gpMap: Map<*, *>? = when (gpObj) {
                    is Map<*, *> -> gpObj
                    is String -> {
                        val t = gpObj.trim()
                        if (t.startsWith("{") && t.endsWith("}")) {
                            try {
                                val p = jsonAdapter.fromJson(t)
                                if (p is Map<*, *>) p else null
                            } catch (_: Exception) { null }
                        } else null
                    }
                    else -> null
                }

                if (gpMap != null) {
                    enabledVal = gpMap["enabled"] ?: gpMap["prompt_enabled"] ?: gpMap["generate_prompt_enabled"]
                    promptVal = gpMap["prompt"] ?: gpMap["content"] ?: gpMap["text"]
                    numScenesVal = gpMap["num_scenes"] ?: gpMap["numScenes"] ?: gpMap["scene_count"] ?: gpMap["scenes_count"]
                    scenesVal = gpMap["scenes"] ?: gpMap["scene_parts"] ?: gpMap["parts"]
                } else {
                    enabledVal = targetMap["enabled"] ?: targetMap["prompt_enabled"] ?: targetMap["generate_prompt_enabled"]
                    promptVal = targetMap["prompt"] ?: targetMap["generate_prompt"] ?: targetMap["prompt_text"]
                    numScenesVal = targetMap["num_scenes"] ?: targetMap["numScenes"] ?: targetMap["scene_count"]
                    scenesVal = targetMap["scenes"] ?: targetMap["scene_parts"] ?: targetMap["parts"]
                }

                if (extractedAudience.isNullOrEmpty()) {
                    val audCandidate = targetMap["items"]
                        ?: targetMap["who_its_for"]
                        ?: targetMap["whoItsFor"]
                        ?: targetMap["audience"]
                        ?: targetMap["points"]
                    if (audCandidate != null && audCandidate != gpObj && audCandidate != targetMap["code_blocks"]) {
                        extractedAudience = parseCleanAudienceList(audCandidate)
                    }
                }
            }

            if (enabledVal == null) {
                enabledVal = post.generatePromptEnabled
                    ?: post.enableGeneratePrompt
                    ?: post.isGeneratePrompt
                    ?: post.promptEnabled
                    ?: post.showPrompt
                    ?: post.hasPrompt
            }

            if (promptVal == null || promptVal.toString().isBlank()) {
                promptVal = post.generatePrompt
                    ?: post.prompt
                    ?: post.aiPrompt
                    ?: post.promptText
                    ?: post.generatedPrompt
                    ?: post.systemPrompt
                    ?: post.prompts
                    ?: post.codeBlock
                    ?: post.code
            }

            if (numScenesVal == null) {
                numScenesVal = post.sceneCount ?: post.scenesCount ?: post.partsCount
            }

            if (scenesVal == null) {
                scenesVal = post.scenes ?: post.sceneParts ?: post.parts ?: post.sceneList
            }

            val explicitDisabled = when (enabledVal) {
                is Boolean -> !enabledVal
                is String -> enabledVal.equals("false", ignoreCase = true) || enabledVal.equals("0")
                is Number -> enabledVal.toInt() == 0
                else -> false
            }

            val explicitEnabled = when (enabledVal) {
                is Boolean -> enabledVal
                is String -> enabledVal.equals("true", ignoreCase = true) || enabledVal.equals("1")
                is Number -> enabledVal.toInt() == 1
                else -> false
            }

            val parsedSceneItems = mutableListOf<com.example.data.model.SceneItem>()
            val rawScenesList = when (scenesVal) {
                is List<*> -> scenesVal
                is String -> {
                    val t = scenesVal.trim()
                    if (t.startsWith("[") && t.endsWith("]")) {
                        try {
                            val p = jsonAdapter.fromJson(t)
                            if (p is List<*>) p else null
                        } catch (_: Exception) { null }
                    } else null
                }
                else -> null
            }

            if (rawScenesList != null) {
                rawScenesList.forEachIndexed { index, item ->
                    when (item) {
                        is Map<*, *> -> {
                            val partNum = when (val p = item["part"] ?: item["part_number"] ?: item["number"] ?: item["scene"]) {
                                is Number -> p.toInt()
                                is String -> p.trim().toIntOrNull() ?: (index + 1)
                                else -> index + 1
                            }
                            val labelStr = (item["label"] ?: item["title"] ?: item["name"])?.toString()?.trim()
                                ?: "Part $partNum"
                            val contentStr = (item["content"] ?: item["prompt"] ?: item["text"] ?: item["body"] ?: extractTextFromMap(item))?.toString()?.trim() ?: ""
                            if (contentStr.isNotEmpty()) {
                                parsedSceneItems.add(
                                    com.example.data.model.SceneItem(
                                        part = partNum,
                                        label = labelStr,
                                        content = com.example.utils.SceneSplitter.cleanPartContent(contentStr)
                                    )
                                )
                            }
                        }
                        is String -> {
                            val t = item.trim()
                            if (t.startsWith("{") && t.endsWith("}")) {
                                try {
                                    val m = jsonAdapter.fromJson(t)
                                    if (m is Map<*, *>) {
                                        val partNum = (m["part"] as? Number)?.toInt() ?: (index + 1)
                                        val labelStr = m["label"]?.toString()?.trim() ?: "Part $partNum"
                                        val contentStr = (m["content"] ?: m["prompt"] ?: m["text"] ?: extractTextFromMap(m))?.toString()?.trim() ?: ""
                                        if (contentStr.isNotEmpty()) {
                                            parsedSceneItems.add(
                                                com.example.data.model.SceneItem(
                                                    part = partNum,
                                                    label = labelStr,
                                                    content = com.example.utils.SceneSplitter.cleanPartContent(contentStr)
                                                )
                                            )
                                        }
                                    } else {
                                        parsedSceneItems.add(
                                            com.example.data.model.SceneItem(
                                                part = index + 1,
                                                label = "Part ${index + 1}",
                                                content = com.example.utils.SceneSplitter.cleanPartContent(t)
                                            )
                                        )
                                    }
                                } catch (_: Exception) {
                                    parsedSceneItems.add(
                                        com.example.data.model.SceneItem(
                                            part = index + 1,
                                            label = "Part ${index + 1}",
                                            content = com.example.utils.SceneSplitter.cleanPartContent(t)
                                        )
                                    )
                                }
                            } else if (t.isNotEmpty()) {
                                parsedSceneItems.add(
                                    com.example.data.model.SceneItem(
                                        part = index + 1,
                                        label = "Part ${index + 1}",
                                        content = com.example.utils.SceneSplitter.cleanPartContent(t)
                                    )
                                )
                            }
                        }
                        else -> {
                            val s = item?.toString()?.trim() ?: ""
                            if (s.isNotEmpty()) {
                                parsedSceneItems.add(
                                    com.example.data.model.SceneItem(
                                        part = index + 1,
                                        label = "Part ${index + 1}",
                                        content = com.example.utils.SceneSplitter.cleanPartContent(s)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            var promptString = promptVal?.toString()?.trim() ?: ""

            if (promptString.isEmpty() && parsedSceneItems.isNotEmpty()) {
                promptString = parsedSceneItems.joinToString("\n\n") { "${it.label}:\n${it.content}" }
            }

            val targetSceneCount = when (numScenesVal) {
                is Number -> numScenesVal.toInt()
                is String -> numScenesVal.trim().toIntOrNull() ?: 0
                else -> 0
            }

            if (parsedSceneItems.isEmpty() && promptString.isNotEmpty()) {
                val derived = com.example.utils.SceneSplitter.splitIntoScenes(
                    prompt = promptString,
                    targetCount = targetSceneCount,
                    explicitScenes = emptyList()
                )
                derived.forEachIndexed { idx, s ->
                    val clean = com.example.utils.SceneSplitter.cleanPartContent(s)
                    if (clean.isNotEmpty()) {
                        parsedSceneItems.add(
                            com.example.data.model.SceneItem(
                                part = idx + 1,
                                label = "Part ${idx + 1}",
                                content = clean
                            )
                        )
                    }
                }
            }

            val sceneContents = parsedSceneItems.map { it.content }

            val isPromptValid = !explicitDisabled && (promptString.isNotBlank() || sceneContents.isNotEmpty())
            val effectiveEnabled = if (explicitDisabled) {
                false
            } else if (explicitEnabled) {
                isPromptValid
            } else {
                isPromptValid
            }

            val finalPrompt = if (effectiveEnabled) promptString else ""
            val finalScenes = if (effectiveEnabled) sceneContents else emptyList()
            val finalSceneItems = if (effectiveEnabled) parsedSceneItems else emptyList()
            val finalSceneCount = if (effectiveEnabled) {
                if (targetSceneCount > 0) targetSceneCount else finalSceneItems.size
            } else 0

            val finalAudience = if (!extractedAudience.isNullOrEmpty()) {
                parseCleanAudienceList(extractedAudience)
            } else {
                parseCleanAudienceList(whoItsForRaw)
            }

            return PromptAndAudienceResult(
                generatePromptEnabled = effectiveEnabled,
                generatePrompt = finalPrompt,
                sceneCount = finalSceneCount,
                scenes = finalScenes,
                sceneItems = finalSceneItems,
                audience = finalAudience,
                extractedLink = extractedLink
            )
        }

        fun parseTargetAudience(raw: Any?): List<String> = parseCleanAudienceList(raw)

        /**
         * Cleanly extracts human-readable audience bullet items.
         * Strictly rejects JSON code, code_blocks, link_info, or metadata fields.
         */
        fun parseCleanAudienceList(raw: Any?): List<String> {
            if (raw == null) return emptyList()
            val rawList = when (raw) {
                is List<*> -> raw.mapNotNull { it?.toString()?.trim() }
                is Map<*, *> -> {
                    val cand = raw["items"] ?: raw["who_its_for"] ?: raw["whoItsFor"] ?: raw["audience"] ?: raw["points"]
                    if (cand != null && cand != raw) return parseCleanAudienceList(cand)
                    emptyList()
                }
                is String -> {
                    val trimmed = raw.trim()
                    if (trimmed.isEmpty()) return emptyList()
                    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                        try {
                            val m = jsonAdapter.fromJson(trimmed)
                            if (m is Map<*, *>) {
                                val cand = m["items"] ?: m["who_its_for"] ?: m["whoItsFor"] ?: m["audience"] ?: m["points"]
                                if (cand != null) return parseCleanAudienceList(cand)
                            }
                        } catch (_: Exception) {}
                        return emptyList()
                    }
                    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                        try {
                            val l = jsonAdapter.fromJson(trimmed)
                            if (l is List<*>) {
                                return l.mapNotNull { it?.toString()?.trim() }
                                    .map { cleanAudienceLine(it) }
                                    .filter { isValidAudienceLine(it) }
                            }
                        } catch (_: Exception) {}
                    }
                    if (trimmed.contains("\n")) {
                        trimmed.lines()
                            .map { cleanAudienceLine(it) }
                            .filter { it.isNotEmpty() }
                    } else if (trimmed.contains(",") && trimmed.split(",").all { it.trim().split(Regex("""\s+""")).size <= 6 }) {
                        trimmed.split(",").map { cleanAudienceLine(it) }.filter { it.isNotEmpty() }
                    } else {
                        listOf(cleanAudienceLine(trimmed))
                    }
                }
                else -> listOf(cleanAudienceLine(raw.toString().trim()))
            }

            return rawList.map { cleanAudienceLine(it) }
                .filter { isValidAudienceLine(it) }
        }

        private fun isValidAudienceLine(line: String): Boolean {
            val trimmed = line.trim()
            if (trimmed.length < 3) return false
            if (trimmed.startsWith("{") || trimmed.startsWith("}") || trimmed.startsWith("[") || trimmed.startsWith("]")) return false
            if (trimmed.contains("code_blocks", ignoreCase = true)) return false
            if (trimmed.contains("link_info", ignoreCase = true)) return false
            if (trimmed.contains("generate_prompt", ignoreCase = true)) return false
            if (trimmed.contains("button_text", ignoreCase = true)) return false
            if (trimmed.contains("playstore", ignoreCase = true)) return false
            if (trimmed.contains("\"url\":", ignoreCase = true) || trimmed.contains("'url':", ignoreCase = true)) return false
            if (trimmed.contains("\":\"") || trimmed.contains("\": \"") || trimmed.contains("\":{")) return false
            if (trimmed.endsWith("}") || trimmed.endsWith("]") || trimmed.endsWith("\",") || trimmed.endsWith("\":")) return false
            return true
        }

        private fun cleanAudienceLine(line: String): String {
            return line.trim()
                .removePrefix("\"")
                .removeSuffix("\"")
                .removeSuffix(",")
                .removePrefix("-")
                .removePrefix("•")
                .removePrefix("*")
                .removePrefix("✔")
                .trim()
        }

        fun extractLinkUrl(raw: Any?): String? {
            if (raw == null) return null
            return when (raw) {
                is String -> {
                    val t = raw.trim()
                    if (t.startsWith("http://") || t.startsWith("https://") || t.startsWith("market://")) {
                        t
                    } else if (t.startsWith("{") && t.endsWith("}")) {
                        try {
                            val m = jsonAdapter.fromJson(t)
                            if (m is Map<*, *>) extractLinkFromMap(m) else extractUrlByRegex(t)
                        } catch (_: Exception) {
                            extractUrlByRegex(t)
                        }
                    } else {
                        extractUrlByRegex(t)
                    }
                }
                is Map<*, *> -> extractLinkFromMap(raw)
                else -> null
            }
        }

        fun extractLinkFromMap(map: Map<*, *>): String? {
            val candidateKeys = listOf("url", "play_store_url", "playStoreUrl", "link", "store_url", "storeUrl", "href", "download_url")
            for (k in candidateKeys) {
                val v = map[k]
                if (v is String && (v.startsWith("http://") || v.startsWith("https://") || v.startsWith("market://"))) {
                    return v.trim()
                }
            }
            val nested = map["link_info"] ?: map["linkInfo"] ?: map["link"]
            if (nested is Map<*, *>) {
                return extractLinkFromMap(nested)
            }
            return null
        }

        fun extractUrlByRegex(text: String): String? {
            val regex = Regex("""https?://[^\s"'<>{}]+|market://[^\s"'<>{}]+""")
            val match = regex.find(text)
            return match?.value?.trim()?.removeSuffix("}")?.removeSuffix("]")?.removeSuffix("\"")?.removeSuffix("'")
        }

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
