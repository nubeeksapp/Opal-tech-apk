package com.example

import com.example.data.local.CachedArticleEntity
import com.example.data.model.TechArticle
import com.example.data.remote.dto.SupabasePostDto
import com.example.utils.SceneSplitter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratePromptTest {

    @Test
    fun `test 1 - article without Generate Prompt has disabled flag and empty prompt`() {
        val articleWithoutPrompt = TechArticle(
            id = "article-1",
            title = "Article without Generate Prompt",
            summary = "Summary text",
            category = "Tech",
            publishDate = "Oct 24, 2026",
            fullDescription = listOf("Normal body content without prompt."),
            whoItsFor = listOf("All users"),
            generatePromptEnabled = false,
            generatePrompt = ""
        )

        assertFalse(articleWithoutPrompt.generatePromptEnabled)
        assertTrue(articleWithoutPrompt.generatePrompt.isBlank())

        // Also test Supabase DTO with generate_prompt_enabled = false
        val dto = SupabasePostDto(
            id = "dto-1",
            title = "Post without prompt",
            generatePromptEnabled = false,
            generatePrompt = "Some prompt that was disabled"
        )
        val domain = dto.toDomain()
        assertFalse(domain.generatePromptEnabled)
    }

    @Test
    fun `test 2 - enabled article displays valid prewritten prompt from admin panel`() {
        val prewrittenPrompt = "Act as a security auditor. Verify TLS 1.3 configuration."
        val dto = SupabasePostDto(
            id = "dto-2",
            title = "Post with enabled prompt",
            generatePromptEnabled = true,
            generatePrompt = prewrittenPrompt,
            sceneCount = 2
        )
        val domain = dto.toDomain()

        assertTrue(domain.generatePromptEnabled)
        assertEquals(prewrittenPrompt, domain.generatePrompt)
        assertEquals(2, domain.sceneCount)
    }

    @Test
    fun `test 3 - SceneSplitter splits prompt into configured number of parts with markers`() {
        val promptWithScenes = """
            Scene 1: Character enters the abandoned laboratory.
            Scene 2: Character discovers holographic data logs.
            Scene 3: An automated alert triggers emergency lockdown.
        """.trimIndent()

        val scenes = SceneSplitter.splitIntoScenes(promptWithScenes, targetCount = 3)

        assertEquals(3, scenes.size)
        assertTrue(scenes[0].contains("Scene 1"))
        assertTrue(scenes[1].contains("Scene 2"))
        assertTrue(scenes[2].contains("Scene 3"))
    }

    @Test
    fun `test 4 - Copy button isolates full prewritten prompt without article body or metadata`() {
        val articleBody = "This is the normal article body discussing mobile photography."
        val prewrittenPrompt = "Preset: Vintage 35mm Film\n- Exposure: +0.3\n- Contrast: -10\n- Grain: 25%"

        val article = TechArticle(
            id = "photo-article",
            title = "Mobile Photography Workflow",
            summary = "Shoot like a pro",
            category = "Technology",
            publishDate = "Oct 24, 2026",
            fullDescription = listOf(articleBody),
            whoItsFor = listOf("Photographers"),
            generatePromptEnabled = true,
            generatePrompt = prewrittenPrompt,
            sceneCount = 1
        )

        val fullPromptToCopy = article.generatePrompt

        // Verify full prompt has exact prewritten content
        assertEquals(prewrittenPrompt, fullPromptToCopy)

        // Verify that copying the prompt does NOT include the article body, title, or tags
        assertFalse(fullPromptToCopy.contains(articleBody))
        assertFalse(fullPromptToCopy.contains(article.title))
        assertFalse(fullPromptToCopy.contains(article.summary))
        assertFalse(fullPromptToCopy.contains("Watch ad to copy"))
    }

    @Test
    fun `test 5 - All Scenes creates exactly configured number of parts for balanced text`() {
        val multilinePrompt = """
            First paragraph detailing the background of quantum encryption and photon polarization.

            Second paragraph explaining satellite quantum key distribution across ground stations.

            Third paragraph outlining defensive strategies against brute force post-quantum attacks.

            Fourth paragraph summarizing future implications for mobile device security chips.
        """.trimIndent()

        // Test configured scene count = 2
        val twoParts = SceneSplitter.splitIntoScenes(multilinePrompt, targetCount = 2)
        assertEquals(2, twoParts.size)
        assertTrue(twoParts[0].isNotBlank())
        assertTrue(twoParts[1].isNotBlank())

        // Test configured scene count = 4
        val fourParts = SceneSplitter.splitIntoScenes(multilinePrompt, targetCount = 4)
        assertEquals(4, fourParts.size)
        assertTrue(fourParts.all { it.isNotBlank() })
    }

    @Test
    fun `test 6 - independent Scene parts copy only their own part without cross-contamination`() {
        val prompt = """
            Part 1:
            Analyze system latency metrics.
            Part 2:
            Identify thread pool deadlocks.
            Part 3:
            Recommend non-blocking Coroutine dispatchers.
        """.trimIndent()

        val scenes = SceneSplitter.splitIntoScenes(prompt, targetCount = 3)
        assertEquals(3, scenes.size)

        val part1 = scenes[0]
        val part2 = scenes[1]
        val part3 = scenes[2]

        // Part 1 contains only Part 1 content
        assertTrue(part1.contains("Analyze system latency metrics"))
        assertFalse(part1.contains("thread pool deadlocks"))
        assertFalse(part1.contains("non-blocking Coroutine dispatchers"))

        // Part 2 contains only Part 2 content
        assertTrue(part2.contains("thread pool deadlocks"))
        assertFalse(part2.contains("Analyze system latency metrics"))
        assertFalse(part2.contains("non-blocking Coroutine dispatchers"))

        // Part 3 contains only Part 3 content
        assertTrue(part3.contains("non-blocking Coroutine dispatchers"))
        assertFalse(part3.contains("Analyze system latency metrics"))
        assertFalse(part3.contains("thread pool deadlocks"))
    }

    @Test
    fun `test 7 - complete prompt is preserved across all parts without missing words or duplication`() {
        val originalPrompt = "The quick brown fox jumps over the lazy dog and explores the futuristic smart city with autonomous flying vehicles."

        val parts = SceneSplitter.splitIntoScenes(originalPrompt, targetCount = 3)
        assertEquals(3, parts.size)

        // Reconstituting parts preserves every word in order
        val joined = parts.joinToString(" ")
        val originalWords = originalPrompt.split(" ")
        val reconstitutedWords = joined.split(Regex("""\s+"""))

        assertEquals(originalWords, reconstitutedWords)
    }

    @Test
    fun `test 8 - prompt fields roundtrip through Room database entity cache intact`() {
        val originalArticle = TechArticle(
            id = "cached-art-1",
            title = "Cached Article with Generate Prompt",
            summary = "Summary",
            category = "Tools",
            publishDate = "Oct 24, 2026",
            fullDescription = listOf("Description"),
            whoItsFor = listOf("Audience"),
            generatePromptEnabled = true,
            generatePrompt = "Prewritten prompt for Room cache persistence.",
            sceneCount = 2,
            scenes = listOf("Scene Part 1", "Scene Part 2")
        )

        // Convert to Room Entity
        val entity = CachedArticleEntity.fromTechArticle(originalArticle)
        assertTrue(entity.generatePromptEnabled)
        assertEquals("Prewritten prompt for Room cache persistence.", entity.generatePrompt)
        assertEquals(2, entity.sceneCount)
        assertEquals(2, entity.scenes.size)

        // Convert back to domain TechArticle
        val restoredArticle = entity.toTechArticle()
        assertEquals(originalArticle.generatePromptEnabled, restoredArticle.generatePromptEnabled)
        assertEquals(originalArticle.generatePrompt, restoredArticle.generatePrompt)
        assertEquals(originalArticle.sceneCount, restoredArticle.sceneCount)
        assertEquals(originalArticle.scenes, restoredArticle.scenes)
    }

    @Test
    fun `test fallback - safe fallback to 1 complete part when scene count is 0 or unconfigured`() {
        val prompt = "Single self-contained prompt instruction."
        val scenes = SceneSplitter.splitIntoScenes(prompt, targetCount = 0)
        assertEquals(1, scenes.size)
        assertEquals(prompt, scenes[0])

        val scenesNegative = SceneSplitter.splitIntoScenes(prompt, targetCount = -1)
        assertEquals(1, scenesNegative.size)
        assertEquals(prompt, scenesNegative[0])
    }

    @Test
    fun `test 9 - scenes containing sentences with commas do not get split into scattered words`() {
        val scene1 = "In a dark, rainy alleyway, a detective searches for clues, holding a flashlight."
        val scene2 = "Suddenly, footsteps echo in the distance, startling the pigeons on the rooftop."
        val rawScenesList = listOf(scene1, scene2)

        val parsed = com.example.data.remote.dto.SupabasePostDto.parseAdminScenes(rawScenesList)

        assertEquals(2, parsed.size)
        assertEquals(scene1, parsed[0])
        assertEquals(scene2, parsed[1])
        // Verify internal commas are intact
        assertTrue(parsed[0].contains("dark, rainy alleyway"))
        assertTrue(parsed[0].contains("clues, holding"))
    }

    @Test
    fun `test 10 - scenes provided as JSON array of objects extracts pure prompt strings`() {
        val jsonScenes = """
            [
              {"scene": 1, "prompt": "Cinematic aerial view of futuristic Tokyo, neon signs, rainy night."},
              {"scene": 2, "prompt": "Low angle tracking shot of a speeding magnetic train gliding across the bridge."}
            ]
        """.trimIndent()

        val parsed = com.example.data.remote.dto.SupabasePostDto.parseAdminScenes(jsonScenes)

        assertEquals(2, parsed.size)
        assertEquals("Cinematic aerial view of futuristic Tokyo, neon signs, rainy night.", parsed[0])
        assertEquals("Low angle tracking shot of a speeding magnetic train gliding across the bridge.", parsed[1])
        assertFalse(parsed[0].contains("{"))
        assertFalse(parsed[0].contains("scene"))
    }

    @Test
    fun `test 11 - prompt is not added to body blocks when generatePromptEnabled is true`() {
        val prewrittenPrompt = "A secret cyberpunk prompt that must not appear in the body."
        val article = TechArticle(
            id = "test-article",
            title = "Test Article",
            summary = "Summary",
            category = "Tech",
            publishDate = "Oct 25, 2026",
            fullDescription = listOf("Normal body text paragraph."),
            whoItsFor = listOf("Users"),
            content = "<p>Normal body text paragraph.</p>",
            prompt = prewrittenPrompt,
            generatePromptEnabled = true,
            generatePrompt = prewrittenPrompt
        )

        val blocks = com.example.ui.components.parseArticleContent(
            rawContent = article.content,
            contentFormat = "html",
            fallbackParagraphs = article.fullDescription,
            prompt = if (article.generatePromptEnabled) "" else article.prompt,
            primaryColor = androidx.compose.ui.graphics.Color.Blue,
            onSurfaceColor = androidx.compose.ui.graphics.Color.Black,
            surfaceVariantColor = androidx.compose.ui.graphics.Color.LightGray
        )

        // Verify that blocks in body does not contain any CodeBlock with the prompt
        val hasPromptInBlocks = blocks.any { b ->
            b is com.example.ui.components.ArticleBlock.CodeBlock && b.code.contains(prewrittenPrompt)
        }
        assertFalse(hasPromptInBlocks)
    }
}
