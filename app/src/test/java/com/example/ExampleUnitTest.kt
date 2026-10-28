package com.example

import com.example.data.remote.dto.SupabaseCategoryDto
import com.example.data.remote.dto.SupabasePostDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `SupabaseCategoryDto toDomain maps fields and assigns emoji`() {
        val dto = SupabaseCategoryDto(
            id = "cat_101",
            name = "Tech & App Tips",
            order = 1
        )
        val domain = dto.toDomain(articleCount = 5)

        assertEquals("cat_101", domain.id)
        assertEquals("Tech & App Tips", domain.name)
        assertEquals(1, domain.order)
        assertEquals("📱", domain.iconEmoji)
        assertEquals(5, domain.articleCount)
    }

    @Test
    fun `SupabasePostDto toDomain maps content and target_audience correctly`() {
        val dto = SupabasePostDto(
            id = 42,
            title = "Pro VPN Shield — Encrypted Proxy",
            summary = "Fast WireGuard proxy for travellers",
            content = "Paragraph 1 about security.\n\nParagraph 2 about encryption.",
            thumbnailUrl = "https://example.supabase.co/storage/v1/object/public/posts/vpn.jpg",
            categoryId = 1,
            publishDate = "2026-10-07T12:00:00Z",
            playStoreUrl = "https://play.google.com/store/apps/details?id=com.provpn.app",
            targetAudience = listOf("Remote workers", "International travelers")
        )

        val article = dto.toDomain(
            categoryNameLookup = { "Security & Privacy" },
            isFavorite = true
        )

        assertEquals("42", article.id)
        assertEquals("Pro VPN Shield — Encrypted Proxy", article.title)
        assertEquals("Security & Privacy", article.category)
        assertEquals("1", article.categoryId)
        assertEquals("https://example.supabase.co/storage/v1/object/public/posts/vpn.jpg", article.thumbnailUrl)
        assertEquals("https://play.google.com/store/apps/details?id=com.provpn.app", article.playStoreUrl)
        assertTrue(article.isFavorite)
        assertEquals(2, article.fullDescription.size)
        assertEquals("Paragraph 1 about security.", article.fullDescription[0])
        assertEquals("Paragraph 2 about encryption.", article.fullDescription[1])
        assertEquals(2, article.whoItsFor.size)
        assertEquals("Remote workers", article.whoItsFor[0])
        assertEquals("International travelers", article.whoItsFor[1])
        assertEquals("Oct 7, 2026, 12:00 PM", article.publishDate)
    }

    @Test
    fun `parseTargetAudience handles JSON string array and fallback`() {
        val jsonArrayStr = "[\"Students\", \"Designers\", \"Developers\"]"
        val parsed = SupabasePostDto.parseTargetAudience(jsonArrayStr)
        assertEquals(3, parsed.size)
        assertEquals("Students", parsed[0])
        assertEquals("Designers", parsed[1])
        assertEquals("Developers", parsed[2])

        val newlineStr = "- Freelancers\n- Photographers"
        val parsedNewline = SupabasePostDto.parseTargetAudience(newlineStr)
        assertEquals(2, parsedNewline.size)
        assertEquals("Freelancers", parsedNewline[0])
        assertEquals("Photographers", parsedNewline[1])
    }

    @Test
    fun `DateUtils formats dates consistently with full year and without truncation`() {
        // Explicit sample requested by user: Oct 12, 2026, 11:12 AM
        val sampleFormatted = com.example.utils.DateUtils.formatArticleDate("October 12, 2026 11:12")
        assertEquals("Oct 12, 2026, 11:12 AM", sampleFormatted)

        val isoFormatted = com.example.utils.DateUtils.formatArticleDate("2026-10-14T14:30:00Z")
        assertEquals("Oct 14, 2026, 2:30 PM", isoFormatted)

        val morningIso = com.example.utils.DateUtils.formatArticleDate("2026-10-13T09:15:00Z")
        assertEquals("Oct 13, 2026, 9:15 AM", morningIso)

        // Verify full year is present and not truncated
        assertTrue(sampleFormatted.contains("2026"))
        assertFalse(sampleFormatted.contains("202..."))
    }

    @Test
    fun `ArticleSanitizer removes unwanted opening words and prefixes`() {
        val sim2Sample = "SIM2:- Add a 2nd line for travel and work."
        assertEquals("Add a 2nd line for travel and work.", com.example.utils.ArticleSanitizer.cleanPrefix(sim2Sample))

        val call2Sample = "CALL 2:- Make cheap international calls"
        assertEquals("Make cheap international calls", com.example.utils.ArticleSanitizer.cleanPrefix(call2Sample))

        val or2Sample = "OR2:- Scan food & cosmetic product to explore"
        assertEquals("Scan food & cosmetic product to explore", com.example.utils.ArticleSanitizer.cleanPrefix(or2Sample))

        val pho1Sample = "PHO1:- Free up to 15GB of storage"
        assertEquals("Free up to 15GB of storage", com.example.utils.ArticleSanitizer.cleanPrefix(pho1Sample))

        val cleanTitle = "Android 15 Private Space: Setup Guide"
        assertEquals(cleanTitle, com.example.utils.ArticleSanitizer.cleanPrefix(cleanTitle))
    }
}
