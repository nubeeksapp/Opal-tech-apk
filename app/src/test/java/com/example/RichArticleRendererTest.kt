package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.ArticleBlock
import com.example.ui.components.isHtmlContent
import com.example.ui.components.parseArticleContent
import com.example.ui.components.parseHtmlBlocks
import com.example.ui.components.parseIndentDp
import com.example.ui.components.sanitizeHtml
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RichArticleRendererTest {

    private val primaryColor = Color(0xFF2563EB)
    private val onSurfaceColor = Color(0xFF1F2937)
    private val surfaceVariantColor = Color(0xFFE5E7EB)

    @Test
    fun `parseIndentDp detects semantic data-indent attribute correctly`() {
        val el1 = Jsoup.parseBodyFragment("<p data-indent=\"1\">Indented level 1</p>").selectFirst("p")!!
        val el3 = Jsoup.parseBodyFragment("<p data-indent=\"3\">Indented level 3</p>").selectFirst("p")!!
        val el8 = Jsoup.parseBodyFragment("<p data-indent=\"8\">Indented level 8</p>").selectFirst("p")!!

        assertEquals(24.dp, parseIndentDp(el1))
        assertEquals(72.dp, parseIndentDp(el3))
        assertEquals(192.dp, parseIndentDp(el8))
    }

    @Test
    fun `parseIndentDp detects CSS classes indent and ql-indent correctly`() {
        val el2 = Jsoup.parseBodyFragment("<p class=\"indent-2\">Class indent 2</p>").selectFirst("p")!!
        val el4 = Jsoup.parseBodyFragment("<p class=\"ql-indent-4\">Quill indent 4</p>").selectFirst("p")!!
        val el6 = Jsoup.parseBodyFragment("<p class=\"custom ql-indent-6 bold\">Multiple classes</p>").selectFirst("p")!!

        assertEquals(48.dp, parseIndentDp(el2))
        assertEquals(96.dp, parseIndentDp(el4))
        assertEquals(144.dp, parseIndentDp(el6))
    }

    @Test
    fun `parseIndentDp detects inline margin-left and padding-left CSS`() {
        val elMargin = Jsoup.parseBodyFragment("<p style=\"margin-left: 24px; padding-left: 24px;\">24px indent</p>").selectFirst("p")!!
        val el72 = Jsoup.parseBodyFragment("<p style=\"margin-left: 72px;\">72px indent</p>").selectFirst("p")!!
        val el192 = Jsoup.parseBodyFragment("<p style=\"margin-left: 192px;\">192px indent</p>").selectFirst("p")!!

        assertEquals(24.dp, parseIndentDp(elMargin))
        assertEquals(72.dp, parseIndentDp(el72))
        assertEquals(192.dp, parseIndentDp(el192))
    }

    @Test
    fun `parseIndentDp works on headings blockquotes and list items`() {
        val heading = Jsoup.parseBodyFragment("<h2 data-indent=\"2\">Indented Heading</h2>").selectFirst("h2")!!
        val quote = Jsoup.parseBodyFragment("<blockquote class=\"indent-3\">Indented Quote</blockquote>").selectFirst("blockquote")!!
        val li = Jsoup.parseBodyFragment("<li style=\"margin-left: 48px;\">Indented List Item</li>").selectFirst("li")!!

        assertEquals(48.dp, parseIndentDp(heading))
        assertEquals(72.dp, parseIndentDp(quote))
        assertEquals(48.dp, parseIndentDp(li))
    }

    @Test
    fun `sanitizeHtml removes scripts iframes and unsafe javascript handlers`() {
        val maliciousHtml = """
            <p>Safe paragraph</p>
            <script>alert('XSS');</script>
            <iframe src="https://evil.com"></iframe>
            <a href="javascript:stealData()">Click here</a>
            <p onclick="hack()">Click me</p>
        """.trimIndent()

        val sanitized = sanitizeHtml(maliciousHtml)

        assertFalse(sanitized.contains("<script>"))
        assertFalse(sanitized.contains("<iframe>"))
        assertFalse(sanitized.contains("javascript:"))
        assertFalse(sanitized.contains("onclick"))
        assertTrue(sanitized.contains("Safe paragraph"))
    }

    @Test
    fun `parseHtmlBlocks parses headings paragraphs and inline styles`() {
        val html = """
            <h1>Main Title</h1>
            <h2>Section Subtitle</h2>
            <p>This is <strong>bold</strong>, <em>italic</em>, <u>underlined</u>, and <del>strikethrough</del>.</p>
            <p><a href="https://example.com">Visit Website</a> and <code>val x = 42</code></p>
        """.trimIndent()

        val blocks = parseHtmlBlocks(html, primaryColor, onSurfaceColor, surfaceVariantColor)

        assertEquals(4, blocks.size)
        assertTrue(blocks[0] is ArticleBlock.Heading && (blocks[0] as ArticleBlock.Heading).level == 1)
        assertTrue(blocks[1] is ArticleBlock.Heading && (blocks[1] as ArticleBlock.Heading).level == 2)
        assertTrue(blocks[2] is ArticleBlock.Paragraph)
        assertTrue(blocks[3] is ArticleBlock.Paragraph)
    }

    @Test
    fun `parseHtmlBlocks parses ordered and unordered nested lists with correct bullets`() {
        val html = """
            <ol>
                <li>First ordered item</li>
                <li>Second ordered item</li>
            </ol>
            <ul>
                <li>Emerging Tech:
                    <ul>
                        <li>AR-Powered Virtual Try-Ons:
                            <ul>
                                <li>Review: High engagement.</li>
                            </ul>
                        </li>
                    </ul>
                </li>
            </ul>
        """.trimIndent()

        val blocks = parseHtmlBlocks(html, primaryColor, onSurfaceColor, surfaceVariantColor)

        // 2 from OL + 3 from nested UL
        assertEquals(5, blocks.size)

        // Ordered list items
        val ol1 = blocks[0] as ArticleBlock.ListItem
        assertEquals("1.", ol1.prefix)
        assertEquals(0, ol1.depth)

        val ol2 = blocks[1] as ArticleBlock.ListItem
        assertEquals("2.", ol2.prefix)
        assertEquals(0, ol2.depth)

        // Nested unordered list items
        val ulDepth0 = blocks[2] as ArticleBlock.ListItem
        assertEquals("•", ulDepth0.prefix)
        assertEquals(0, ulDepth0.depth)

        val ulDepth1 = blocks[3] as ArticleBlock.ListItem
        assertEquals("◦", ulDepth1.prefix)
        assertEquals(1, ulDepth1.depth)

        val ulDepth2 = blocks[4] as ArticleBlock.ListItem
        assertEquals("▪", ulDepth2.prefix)
        assertEquals(2, ulDepth2.depth)
    }

    @Test
    fun `parseHtmlBlocks respects code-block copy button attribute`() {
        val htmlWithCopy = "<pre data-copy=\"true\"><code>println(\"Hello\")</code></pre>"
        val htmlWithoutCopy = "<pre data-copy=\"false\"><code>val secret = 123</code></pre>"

        val blocksWithCopy = parseHtmlBlocks(htmlWithCopy, primaryColor, onSurfaceColor, surfaceVariantColor)
        val blocksWithoutCopy = parseHtmlBlocks(htmlWithoutCopy, primaryColor, onSurfaceColor, surfaceVariantColor)

        val code1 = blocksWithCopy[0] as ArticleBlock.CodeBlock
        assertTrue(code1.showCopyButton)
        assertEquals("println(\"Hello\")", code1.code)

        val code2 = blocksWithoutCopy[0] as ArticleBlock.CodeBlock
        assertFalse(code2.showCopyButton)
        assertEquals("val secret = 123", code2.code)
    }

    @Test
    fun `parseHtmlBlocks parses prompt and summary blocks with titles and preserves line breaks`() {
        val promptHtml = """
            <pre class="prompt" data-title="Prompt"><code>Line 1 of prompt
Line 2 of prompt
Line 3 of prompt</code></pre>
            <div class="summary-box"><code>Key summary line 1
Key summary line 2</code></div>
        """.trimIndent()

        val blocks = parseHtmlBlocks(promptHtml, primaryColor, onSurfaceColor, surfaceVariantColor)
        assertEquals(2, blocks.size)

        val block1 = blocks[0] as ArticleBlock.CodeBlock
        assertEquals("Prompt", block1.title)
        assertTrue(block1.code.contains("\n"))
        assertEquals("Line 1 of prompt\nLine 2 of prompt\nLine 3 of prompt", block1.code)
        assertTrue(block1.showCopyButton)

        val block2 = blocks[1] as ArticleBlock.CodeBlock
        assertEquals("Summary", block2.title)
        assertTrue(block2.code.contains("\n"))
        assertEquals("Key summary line 1\nKey summary line 2", block2.code)
        assertTrue(block2.showCopyButton)
    }

    @Test
    fun `parseHtmlBlocks parses tables with headers and rows`() {
        val tableHtml = """
            <table>
                <thead>
                    <tr><th>Plan</th><th>Price</th><th>Storage</th></tr>
                </thead>
                <tbody>
                    <tr><td>Free</td><td>$0</td><td>5GB</td></tr>
                    <tr><td>Pro</td><td>$10</td><td>100GB</td></tr>
                </tbody>
            </table>
        """.trimIndent()

        val blocks = parseHtmlBlocks(tableHtml, primaryColor, onSurfaceColor, surfaceVariantColor)
        assertEquals(1, blocks.size)
        val table = blocks[0] as ArticleBlock.Table
        assertEquals(3, table.headers.size)
        assertEquals("Plan", table.headers[0].text)
        assertEquals("Price", table.headers[1].text)
        assertEquals("Storage", table.headers[2].text)
        assertEquals(2, table.rows.size)
        assertEquals("Free", table.rows[0][0].text)
        assertEquals("Pro", table.rows[1][0].text)
    }

    @Test
    fun `parseArticleContent preserves plain text articles without mangling`() {
        val plainText = "First paragraph about mobile apps.\n\nSecond paragraph discussing privacy."
        val blocks = parseArticleContent(
            rawContent = plainText,
            contentFormat = "plain_text",
            fallbackParagraphs = null,
            primaryColor = primaryColor,
            onSurfaceColor = onSurfaceColor,
            surfaceVariantColor = surfaceVariantColor
        )

        assertEquals(2, blocks.size)
        assertTrue(blocks[0] is ArticleBlock.Paragraph)
        assertEquals("First paragraph about mobile apps.", (blocks[0] as ArticleBlock.Paragraph).text.text)
        assertTrue(blocks[1] is ArticleBlock.Paragraph)
        assertEquals("Second paragraph discussing privacy.", (blocks[1] as ArticleBlock.Paragraph).text.text)
    }

    @Test
    fun `isHtmlContent correctly detects rich HTML vs plain text`() {
        assertTrue(isHtmlContent("<p>Sample</p>", "rich_html_v1"))
        assertTrue(isHtmlContent("<h2>Title</h2><p>Body</p>", ""))
        assertFalse(isHtmlContent("Plain text without tags", "plain_text"))
        assertFalse(isHtmlContent("Just some plain words here.", ""))
    }

    @Test
    fun `article with normal text and separate code block isolates code block strictly`() {
        val html = """
            <p>Welcome to this guide. Here is normal article text explaining how to use generative prompts for analysis.</p>
            <pre class="prompt-box"><code>Act as a senior software architect.
1. Analyze the system design.
2. Identify 3 performance bottlenecks.
3. Recommend Kotlin Coroutines best practices!</code></pre>
        """.trimIndent()

        val blocks = parseArticleContent(
            rawContent = html,
            contentFormat = "rich_html_v1",
            fallbackParagraphs = null,
            primaryColor = primaryColor,
            onSurfaceColor = onSurfaceColor,
            surfaceVariantColor = surfaceVariantColor
        )

        assertEquals(2, blocks.size)
        assertTrue(blocks[0] is ArticleBlock.Paragraph)
        assertTrue(blocks[1] is ArticleBlock.CodeBlock)

        val paragraph = blocks[0] as ArticleBlock.Paragraph
        val codeBlock = blocks[1] as ArticleBlock.CodeBlock

        assertEquals(
            "Welcome to this guide. Here is normal article text explaining how to use generative prompts for analysis.",
            paragraph.text.text
        )

        // Verifies the code block does NOT contain the normal article text!
        assertFalse(codeBlock.code.contains("Welcome to this guide"))
        assertFalse(codeBlock.code.contains("normal article text"))

        // Verifies exact wording, punctuation, numbering, and line breaks are preserved!
        val expectedPrompt = """
            Act as a senior software architect.
            1. Analyze the system design.
            2. Identify 3 performance bottlenecks.
            3. Recommend Kotlin Coroutines best practices!
        """.trimIndent()

        assertEquals(expectedPrompt, codeBlock.code)
        assertTrue(codeBlock.showCopyButton)
    }

    @Test
    fun `article with normal text and markdown fenced code block isolates prompt text`() {
        val markdown = """
            This is Part 1: the normal article content discussing AI automation.

            ```prompt
            Summarize this quarter's cloud infrastructure report:
            - Cost efficiency: +14%
            - Latency p99: 120ms
            - Incident count: 0
            Format as bullet points with bold metrics.
            ```

            This is additional normal article text following the code block.
        """.trimIndent()

        val blocks = parseArticleContent(
            rawContent = markdown,
            contentFormat = "plain_text",
            fallbackParagraphs = null,
            primaryColor = primaryColor,
            onSurfaceColor = onSurfaceColor,
            surfaceVariantColor = surfaceVariantColor
        )

        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is ArticleBlock.Paragraph)
        assertTrue(blocks[1] is ArticleBlock.CodeBlock)
        assertTrue(blocks[2] is ArticleBlock.Paragraph)

        val codeBlock = blocks[1] as ArticleBlock.CodeBlock
        assertFalse(codeBlock.code.contains("Part 1"))
        assertFalse(codeBlock.code.contains("additional normal article text"))
        assertFalse(codeBlock.code.contains("```"))

        val expectedCode = """
            Summarize this quarter's cloud infrastructure report:
            - Cost efficiency: +14%
            - Latency p99: 120ms
            - Incident count: 0
            Format as bullet points with bold metrics.
        """.trimIndent()

        assertEquals(expectedCode, codeBlock.code)
    }

    @Test
    fun `multiple code blocks in one article work independently and copy only their own block`() {
        val html = """
            <p>First step intro</p>
            <pre class="prompt-box" data-title="Prompt 1"><code>PROMPT ONE: Brainstorm 5 ideas for mobile productivity.</code></pre>
            <p>Second step intro</p>
            <pre class="prompt-box" data-title="Prompt 2"><code>PROMPT TWO: Write unit tests with Robolectric.</code></pre>
        """.trimIndent()

        val blocks = parseArticleContent(
            rawContent = html,
            contentFormat = "rich_html_v1",
            fallbackParagraphs = null,
            primaryColor = primaryColor,
            onSurfaceColor = onSurfaceColor,
            surfaceVariantColor = surfaceVariantColor
        )

        assertEquals(4, blocks.size)
        val code1 = blocks[1] as ArticleBlock.CodeBlock
        val code2 = blocks[3] as ArticleBlock.CodeBlock

        assertEquals("PROMPT ONE: Brainstorm 5 ideas for mobile productivity.", code1.code)
        assertEquals("PROMPT TWO: Write unit tests with Robolectric.", code2.code)

        assertFalse(code1.code.contains("PROMPT TWO"))
        assertFalse(code2.code.contains("PROMPT ONE"))
        assertFalse(code1.code.contains("First step intro"))
        assertFalse(code2.code.contains("Second step intro"))
    }

    @Test
    fun `web editor button labels and titles are stripped from pre code block`() {
        val htmlWithButton = """
            <pre class="prompt-box">
                <div class="prompt-header"><span>Prompt</span><button class="copy-btn">Copy prompt</button></div>
                <code>Write an email to team requesting Q4 project status.</code>
            </pre>
        """.trimIndent()

        val blocks = parseHtmlBlocks(htmlWithButton, primaryColor, onSurfaceColor, surfaceVariantColor)
        assertEquals(1, blocks.size)

        val codeBlock = blocks[0] as ArticleBlock.CodeBlock
        assertEquals("Write an email to team requesting Q4 project status.", codeBlock.code)
        assertFalse(codeBlock.code.contains("Copy prompt"))
        assertFalse(codeBlock.code.contains("Prompt\n"))
    }

    @Test
    fun `clicking copy on next codeblock copies strictly its own prompt without first or surrounding article`() {
        val html = """
            <p>Article header paragraph describing AI prompt engineering.</p>
            <pre class="prompt-box"><code>PROMPT 1: Brainstorm 10 UX improvements for Android app.</code></pre>
            <p>Intermediary paragraph describing the testing phase.</p>
            <pre class="prompt-box"><code>PROMPT 2: Write automated Robolectric unit tests for Jetpack Compose.</code></pre>
            <p>Final conclusion paragraph.</p>
            <pre class="prompt-box"><code>PROMPT 3: Generate deployment release notes for production build.</code></pre>
        """.trimIndent()

        val blocks = parseArticleContent(
            rawContent = html,
            contentFormat = "html",
            fallbackParagraphs = null,
            primaryColor = primaryColor,
            onSurfaceColor = onSurfaceColor,
            surfaceVariantColor = surfaceVariantColor
        )

        assertEquals(6, blocks.size)
        val p1 = blocks[0] as ArticleBlock.Paragraph
        val cb1 = blocks[1] as ArticleBlock.CodeBlock
        val p2 = blocks[2] as ArticleBlock.Paragraph
        val cb2 = blocks[3] as ArticleBlock.CodeBlock
        val p3 = blocks[4] as ArticleBlock.Paragraph
        val cb3 = blocks[5] as ArticleBlock.CodeBlock

        // Block 1 strictly isolated
        assertEquals("PROMPT 1: Brainstorm 10 UX improvements for Android app.", cb1.code)
        assertFalse(cb1.code.contains(cb2.code))
        assertFalse(cb1.code.contains(cb3.code))
        assertFalse(cb1.code.contains(p1.text.text))
        assertFalse(cb1.code.contains(p2.text.text))

        // Block 2 strictly isolated - NEVER contains Block 1 or surrounding articles
        assertEquals("PROMPT 2: Write automated Robolectric unit tests for Jetpack Compose.", cb2.code)
        assertFalse(cb2.code.contains(cb1.code))
        assertFalse(cb2.code.contains(cb3.code))
        assertFalse(cb2.code.contains(p1.text.text))
        assertFalse(cb2.code.contains(p2.text.text))
        assertFalse(cb2.code.contains(p3.text.text))

        // Block 3 strictly isolated - NEVER contains Block 1, Block 2, or surrounding articles
        assertEquals("PROMPT 3: Generate deployment release notes for production build.", cb3.code)
        assertFalse(cb3.code.contains(cb1.code))
        assertFalse(cb3.code.contains(cb2.code))
        assertFalse(cb3.code.contains(p1.text.text))
        assertFalse(cb3.code.contains(p2.text.text))
        assertFalse(cb3.code.contains(p3.text.text))
    }
}
