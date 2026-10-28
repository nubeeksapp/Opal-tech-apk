package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.ArticleBlock
import com.example.ui.components.parseArticleContent
import com.example.ui.components.parseHtmlBlocks
import com.example.utils.IntentUtils
import com.example.utils.SceneSplitter
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CodeBlockCopyTest {

    private val primaryColor = androidx.compose.ui.graphics.Color(0xFF2E6930)
    private val onSurfaceColor = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
    private val surfaceVariantColor = androidx.compose.ui.graphics.Color(0xFFEFF2EC)

    @Test
    fun `test 1 - Copy Part 1, Part 2, Part 3 each copies strictly only its own block content`() {
        val multiblockHtml = """
            <h2>Architecture Guide</h2>
            <p>Introductory paragraph describing on-device pipelines.</p>
            <div class="code-block-card" data-part="1" data-part-label="Part 1">
                <code>COPY_TEST_ALPHA_123</code>
            </div>
            <p>Intermediary discussion regarding memory profiles.</p>
            <div class="code-block-card" data-part="2" data-part-label="Part 2">
                <code>TWO_LINES_FIRST_LINE
TWO_LINES_SECOND_LINE</code>
            </div>
            <p>Summary remarks before the final module.</p>
            <div class="code-block-card" data-part="3" data-part-label="Part 3">
                <code>SPECIAL_SYMBOLS_{}_456</code>
            </div>
        """.trimIndent()

        val blocks = parseHtmlBlocks(multiblockHtml, primaryColor, onSurfaceColor, surfaceVariantColor)
        val codeBlocks = blocks.filterIsInstance<ArticleBlock.CodeBlock>()

        assertEquals(3, codeBlocks.size)

        // Block 1: Part 1
        val part1 = codeBlocks[0]
        assertEquals("Part 1", part1.title)
        assertEquals("COPY_TEST_ALPHA_123", part1.code)
        assertFalse(part1.code.contains("Architecture Guide"))
        assertFalse(part1.code.contains("Introductory paragraph"))
        assertFalse(part1.code.contains("TWO_LINES_FIRST_LINE"))
        assertFalse(part1.code.contains("SPECIAL_SYMBOLS"))

        // Block 2: Part 2 (preserves newline)
        val part2 = codeBlocks[1]
        assertEquals("Part 2", part2.title)
        assertEquals("TWO_LINES_FIRST_LINE\nTWO_LINES_SECOND_LINE", part2.code)
        assertFalse(part2.code.contains("COPY_TEST_ALPHA_123"))
        assertFalse(part2.code.contains("SPECIAL_SYMBOLS"))

        // Block 3: Part 3 (preserves special symbols)
        val part3 = codeBlocks[2]
        assertEquals("Part 3", part3.title)
        assertEquals("SPECIAL_SYMBOLS_{}_456", part3.code)
        assertFalse(part3.code.contains("COPY_TEST_ALPHA_123"))
        assertFalse(part3.code.contains("TWO_LINES_FIRST_LINE"))
    }

    @Test
    fun `test 2 - clipboard copy actually writes to clipboard and verifies content`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Copy Part 1
        val success1 = IntentUtils.copyToClipboard(context, "COPY_TEST_ALPHA_123", "Part 1")
        assertTrue(success1)

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip1 = clipboard.primaryClip
        assertNotNull(clip1)
        assertEquals("COPY_TEST_ALPHA_123", clip1?.getItemAt(0)?.text?.toString())

        // 2. Copy Part 2 (multi-line)
        val success2 = IntentUtils.copyToClipboard(context, "TWO_LINES_FIRST_LINE\nTWO_LINES_SECOND_LINE", "Part 2")
        assertTrue(success2)

        val clip2 = clipboard.primaryClip
        assertEquals("TWO_LINES_FIRST_LINE\nTWO_LINES_SECOND_LINE", clip2?.getItemAt(0)?.text?.toString())

        // 3. Copy Part 3 (special symbols)
        val success3 = IntentUtils.copyToClipboard(context, "SPECIAL_SYMBOLS_{}_456", "Part 3")
        assertTrue(success3)

        val clip3 = clipboard.primaryClip
        assertEquals("SPECIAL_SYMBOLS_{}_456", clip3?.getItemAt(0)?.text?.toString())
    }

    @Test
    fun `test 3 - empty or blank text fails copy and does not report success`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val successEmpty = IntentUtils.copyToClipboard(context, "", "Empty Part")
        assertFalse(successEmpty)

        val successBlank = IntentUtils.copyToClipboard(context, "   \n\t  ", "Blank Part")
        assertFalse(successBlank)
    }

    @Test
    fun `test 4 - SceneSplitter cleanPartContent removes redundant marker headings without altering prompt content`() {
        val rawPart1 = "Part 1:\nAnalyze hardware constraints for mobile NPUs."
        val cleaned1 = SceneSplitter.cleanPartContent(rawPart1)
        assertEquals("Analyze hardware constraints for mobile NPUs.", cleaned1)

        val rawPart2 = "Scene 2 - Evaluate battery profiles."
        val cleaned2 = SceneSplitter.cleanPartContent(rawPart2)
        assertEquals("Evaluate battery profiles.", cleaned2)

        val exactContent = "COPY_TEST_ALPHA_123"
        val cleanedExact = SceneSplitter.cleanPartContent(exactContent)
        assertEquals("COPY_TEST_ALPHA_123", cleanedExact)
    }
}
