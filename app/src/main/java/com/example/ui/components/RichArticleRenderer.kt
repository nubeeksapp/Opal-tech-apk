package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TechArticle
import com.example.utils.ArticleSanitizer
import com.example.utils.IntentUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.safety.Safelist

/**
 * Structural representations of parsed article blocks for Jetpack Compose rendering.
 */
sealed class ArticleBlock {
    data class Heading(
        val level: Int,
        val text: AnnotatedString,
        val alignment: TextAlign = TextAlign.Start,
        val startIndent: Dp = 0.dp
    ) : ArticleBlock()

    data class Paragraph(
        val text: AnnotatedString,
        val alignment: TextAlign = TextAlign.Start,
        val isBlank: Boolean = false,
        val startIndent: Dp = 0.dp
    ) : ArticleBlock()

    data class ListItem(
        val depth: Int,
        val prefix: String,
        val text: AnnotatedString,
        val alignment: TextAlign = TextAlign.Start,
        val startIndent: Dp = 0.dp
    ) : ArticleBlock()

    data class CodeBlock(
        val code: String,
        val showCopyButton: Boolean,
        val startIndent: Dp = 0.dp,
        val title: String = ""
    ) : ArticleBlock()

    data class Blockquote(
        val text: AnnotatedString,
        val startIndent: Dp = 0.dp
    ) : ArticleBlock()

    data class Table(
        val headers: List<AnnotatedString>,
        val rows: List<List<AnnotatedString>>,
        val startIndent: Dp = 0.dp
    ) : ArticleBlock()

    object Divider : ArticleBlock()
}

/**
 * Renders the detailed description of a [TechArticle], supporting rich HTML from the admin panel,
 * structured rich-text formatting, and plain-text fallback.
 */
@Composable
fun RichArticleContent(
    article: TechArticle,
    baseFontSize: TextUnit,
    lineSpacing: TextUnit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    val blocks = remember(
        article.content,
        article.contentFormat,
        article.fullDescription,
        article.prompt,
        article.generatePromptEnabled,
        primaryColor,
        onSurfaceColor,
        surfaceVariantColor
    ) {
        parseArticleContent(
            rawContent = article.content,
            contentFormat = article.contentFormat,
            fallbackParagraphs = article.fullDescription,
            prompt = if (article.generatePromptEnabled) "" else article.prompt,
            primaryColor = primaryColor,
            onSurfaceColor = onSurfaceColor,
            surfaceVariantColor = surfaceVariantColor
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEachIndexed { index, block ->
            androidx.compose.runtime.key(index, block) {
                RenderArticleBlock(
                    block = block,
                    baseFontSize = baseFontSize,
                    lineSpacing = lineSpacing,
                    context = context
                )
            }
        }
    }
}

@Composable
private fun RenderArticleBlock(
    block: ArticleBlock,
    baseFontSize: TextUnit,
    lineSpacing: TextUnit,
    context: Context
) {
    when (block) {
        is ArticleBlock.Heading -> {
            val (fontSize, lineHeight, topPadding, bottomPadding) = when (block.level) {
                1 -> Quadruple((baseFontSize.value + 6).sp, (lineSpacing.value + 6).sp, 18.dp, 8.dp)
                2 -> Quadruple((baseFontSize.value + 4).sp, (lineSpacing.value + 4).sp, 16.dp, 6.dp)
                else -> Quadruple((baseFontSize.value + 2).sp, (lineSpacing.value + 2).sp, 14.dp, 4.dp)
            }
            Spacer(modifier = Modifier.height(topPadding))
            Text(
                text = block.text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = block.alignment,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = block.startIndent)
            )
            Spacer(modifier = Modifier.height(bottomPadding))
        }

        is ArticleBlock.Paragraph -> {
            if (block.isBlank) {
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = baseFontSize,
                        lineHeight = lineSpacing
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = block.alignment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = block.startIndent)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        is ArticleBlock.ListItem -> {
            val totalIndent = if (block.startIndent > 0.dp) {
                block.startIndent
            } else {
                when (block.depth) {
                    0 -> 0.dp
                    1 -> 24.dp
                    2 -> 48.dp
                    3 -> 72.dp
                    else -> ((block.depth) * 24).dp
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = totalIndent, top = 2.dp, bottom = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = block.prefix,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = baseFontSize,
                        lineHeight = lineSpacing,
                        fontWeight = if (block.prefix.contains(".")) FontWeight.Normal else FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = baseFontSize,
                        lineHeight = lineSpacing
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = block.alignment,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        is ArticleBlock.CodeBlock -> {
            PromptBox(
                promptText = block.code,
                title = block.title,
                showCopyButton = block.showCopyButton,
                startIndent = block.startIndent,
                baseFontSize = baseFontSize,
                lineSpacing = lineSpacing
            )
        }

        is ArticleBlock.Blockquote -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = block.startIndent, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(28.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = baseFontSize,
                        lineHeight = lineSpacing,
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        is ArticleBlock.Table -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = block.startIndent, top = 10.dp, bottom = 10.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        if (block.headers.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                block.headers.forEach { header ->
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = baseFontSize
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .widthIn(min = 90.dp, max = 220.dp)
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        block.rows.forEachIndexed { rowIndex, row ->
                            val bg = if (rowIndex % 2 == 1) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            } else {
                                Color.Transparent
                            }
                            Row(
                                modifier = Modifier
                                    .background(bg)
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                row.forEach { cell ->
                                    Text(
                                        text = cell,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = baseFontSize),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .widthIn(min = 90.dp, max = 220.dp)
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (rowIndex < block.rows.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }

        is ArticleBlock.Divider -> {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Parses raw article content into structural [ArticleBlock] items.
 * Supports rich HTML, Markdown with code fences (``` or ~~~), and plain text.
 * Code blocks are isolated strictly so the Copy button copies only its own code block.
 */
fun parseArticleContent(
    rawContent: String?,
    contentFormat: String?,
    fallbackParagraphs: List<String>?,
    prompt: String = "",
    primaryColor: Color,
    onSurfaceColor: Color,
    surfaceVariantColor: Color
): List<ArticleBlock> {
    val content = rawContent?.trim() ?: ""
    val format = contentFormat?.trim() ?: ""
    val blocks = mutableListOf<ArticleBlock>()

    val isHtml = isHtmlContent(content, format)
    if (isHtml) {
        val sanitized = sanitizeHtml(content)
        blocks.addAll(parseHtmlBlocks(sanitized, primaryColor, onSurfaceColor, surfaceVariantColor))
    } else {
        // Plain text or Markdown processing
        val textToUse = if (content.isNotBlank()) {
            content
        } else {
            fallbackParagraphs?.joinToString("\n\n") ?: ""
        }

        if (textToUse.isNotBlank()) {
            blocks.addAll(parseMarkdownOrPlainText(textToUse))
        }
    }

    // If an explicit prompt was provided and no code blocks exist in the content
    if (prompt.isNotBlank() && blocks.none { it is ArticleBlock.CodeBlock }) {
        val promptTrimmed = prompt.trim()
        if (promptTrimmed.isNotEmpty()) {
            blocks.add(
                ArticleBlock.CodeBlock(
                    code = promptTrimmed,
                    showCopyButton = true,
                    title = "Prompt"
                )
            )
        }
    }

    return blocks
}

/**
 * Detects whether content is formatted as HTML based on explicit format metadata or HTML tags.
 */
fun isHtmlContent(content: String, format: String): Boolean {
    if (format.equals("rich_html_v1", ignoreCase = true) || format.equals("html", ignoreCase = true)) {
        return true
    }
    if (format.equals("plain_text", ignoreCase = true) || format.equals("text", ignoreCase = true)) {
        return false
    }
    val trimmed = content.trim()
    val htmlTagPattern = Regex(
        """<(?:p|h[1-6]|ul|ol|li|pre|code|blockquote|table|div|span|strong|b|em|i|u|s|a)\b[^>]*>""",
        RegexOption.IGNORE_CASE
    )
    return htmlTagPattern.containsMatchIn(trimmed)
}

/**
 * Sanitizes untrusted HTML using a strict whitelist, eliminating script execution, iframes,
 * object/embed tags, and malicious javascript: URIs, while preserving styling and indentation attributes.
 */
fun sanitizeHtml(rawHtml: String): String {
    val safelist = Safelist.relaxed()
        .addTags("hr", "s", "strike", "del", "span")
        .addAttributes("pre", "data-copy", "class", "style", "data-indent", "data-part", "data-part-label", "data-title", "title")
        .addAttributes("code", "class", "style", "data-copy", "data-part", "data-part-label", "data-title", "title")
        .addAttributes("p", "style", "class", "align", "data-indent")
        .addAttributes("h1", "style", "class", "align", "data-indent")
        .addAttributes("h2", "style", "class", "align", "data-indent")
        .addAttributes("h3", "style", "class", "align", "data-indent")
        .addAttributes("h4", "style", "class", "align", "data-indent")
        .addAttributes("h5", "style", "class", "align", "data-indent")
        .addAttributes("h6", "style", "class", "align", "data-indent")
        .addAttributes("div", "style", "class", "align", "data-indent", "data-copy", "data-part", "data-part-label", "data-title", "title")
        .addAttributes("blockquote", "style", "class", "data-indent")
        .addAttributes("ul", "style", "class", "data-indent")
        .addAttributes("ol", "style", "class", "data-indent")
        .addAttributes("li", "style", "class", "data-indent")
        .addAttributes("span", "style", "class")
        .addAttributes("table", "style", "class", "border", "data-indent")
        .addAttributes("th", "style", "class", "align", "colspan", "rowspan")
        .addAttributes("td", "style", "class", "align", "colspan", "rowspan")
        .addProtocols("a", "href", "http", "https", "mailto", "tel")

    return Jsoup.clean(rawHtml, safelist)
}

/**
 * Extracts indentation in Dp based on:
 * - data-indent="1" through data-indent="8"
 * - class="indent-1" through "indent-8" or "ql-indent-1" through "ql-indent-8"
 * - style="margin-left: 24px; padding-left: 24px;" (24px per level up to 192px)
 */
fun parseIndentDp(element: Element, baseIndentLevel: Int = 0): Dp {
    var level = baseIndentLevel

    fun extractLevel(el: Element): Int {
        var l = 0

        // 1. Semantic attribute data-indent
        val dataIndent = el.attr("data-indent").toIntOrNull()
        if (dataIndent != null && dataIndent in 1..8) {
            l = maxOf(l, dataIndent)
        }

        // 2. CSS classes: indent-X and ql-indent-X
        val classNames = el.className().lowercase()
        val classRegex = Regex("""(?:^|\s)(?:ql-)?indent-(\d+)(?:\s|$)""")
        val classMatch = classRegex.find(classNames)
        if (classMatch != null) {
            val classLevel = classMatch.groupValues[1].toIntOrNull()
            if (classLevel != null && classLevel in 1..8) {
                l = maxOf(l, classLevel)
            }
        }

        // 3. Inline CSS: margin-left: Xpx or padding-left: Xpx
        val style = el.attr("style").lowercase()
        val marginMatch = Regex("""margin-left:\s*(\d+(?:\.\d+)?)px""").find(style)
        val paddingMatch = Regex("""padding-left:\s*(\d+(?:\.\d+)?)px""").find(style)
        val pxVal = marginMatch?.groupValues?.get(1)?.toFloatOrNull()
            ?: paddingMatch?.groupValues?.get(1)?.toFloatOrNull()
        if (pxVal != null && pxVal > 0f) {
            val pxLevel = kotlin.math.round(pxVal / 24f).toInt()
            if (pxLevel in 1..8) {
                l = maxOf(l, pxLevel)
            }
        }

        return l
    }

    val directLevel = extractLevel(element)
    level = maxOf(level, directLevel)

    // For LI elements, also check if child P has indent, or if P's parent LI has indent
    if (element.tagName().equals("li", ignoreCase = true)) {
        element.children().firstOrNull { it.tagName().equals("p", ignoreCase = true) }?.let { childP ->
            level = maxOf(level, extractLevel(childP))
        }
    } else if (element.tagName().equals("p", ignoreCase = true) && element.parent()?.tagName().equals("li", ignoreCase = true)) {
        element.parent()?.let { parentLi ->
            level = maxOf(level, extractLevel(parentLi))
        }
    }

    level = level.coerceIn(0, 8)
    return (level * 24).dp
}

/**
 * Parses sanitized HTML DOM into a list of [ArticleBlock] items.
 */
fun parseHtmlBlocks(
    sanitizedHtml: String,
    primaryColor: Color,
    onSurfaceColor: Color,
    surfaceVariantColor: Color
): List<ArticleBlock> {
    val doc = Jsoup.parseBodyFragment(sanitizedHtml)
    val body = doc.body()
    val blocks = mutableListOf<ArticleBlock>()

    for (node in body.childNodes()) {
        if (node is Element) {
            processElementAsBlock(node, blocks, primaryColor, onSurfaceColor, surfaceVariantColor)
        } else if (node is TextNode) {
            val text = node.text().replace("\u200B", "").trim()
            if (text.isNotEmpty()) {
                blocks.add(ArticleBlock.Paragraph(text = AnnotatedString(text)))
            }
        }
    }

    return blocks
}

private fun processElementAsBlock(
    element: Element,
    outBlocks: MutableList<ArticleBlock>,
    primaryColor: Color,
    onSurfaceColor: Color,
    surfaceVariantColor: Color
) {
    val tag = element.tagName().lowercase()
    val alignment = parseAlignment(element)
    val indent = parseIndentDp(element)

    when (tag) {
        "h1" -> {
            val inline = buildInlineAnnotatedString(element, primaryColor, onSurfaceColor, surfaceVariantColor)
            if (inline.isNotBlank()) outBlocks.add(ArticleBlock.Heading(1, inline, alignment, indent))
        }
        "h2" -> {
            val inline = buildInlineAnnotatedString(element, primaryColor, onSurfaceColor, surfaceVariantColor)
            if (inline.isNotBlank()) outBlocks.add(ArticleBlock.Heading(2, inline, alignment, indent))
        }
        "h3", "h4", "h5", "h6" -> {
            val inline = buildInlineAnnotatedString(element, primaryColor, onSurfaceColor, surfaceVariantColor)
            if (inline.isNotBlank()) outBlocks.add(ArticleBlock.Heading(3, inline, alignment, indent))
        }
        "p" -> {
            val singleChild = element.children().firstOrNull()
            val isPreOrCodeOnly = element.children().size == 1 && singleChild != null &&
                (singleChild.tagName().equals("code", ignoreCase = true) || singleChild.tagName().equals("pre", ignoreCase = true))
            if (isPreOrCodeOnly) {
                processElementAsBlock(singleChild!!, outBlocks, primaryColor, onSurfaceColor, surfaceVariantColor)
            } else {
                val inline = buildInlineAnnotatedString(element, primaryColor, onSurfaceColor, surfaceVariantColor)
                if (inline.isBlank()) {
                    outBlocks.add(ArticleBlock.Paragraph(AnnotatedString(""), alignment, isBlank = true, startIndent = indent))
                } else {
                    outBlocks.add(ArticleBlock.Paragraph(inline, alignment, startIndent = indent))
                }
            }
        }
        "ul" -> {
            processListElement(element, outBlocks, isOrdered = false, depth = 0, primaryColor, onSurfaceColor, surfaceVariantColor)
        }
        "ol" -> {
            processListElement(element, outBlocks, isOrdered = true, depth = 0, primaryColor, onSurfaceColor, surfaceVariantColor)
        }
        "pre" -> {
            val preClone = element.clone()
            // Remove any UI buttons, headers, or copy badges generated by web admin editors
            preClone.select("button, .copy-btn, .copy-button, .btn-copy, .prompt-header, .prompt-title, [data-ui='copy'], .part-label, .part_label, .card-label, .code-label").remove()
            val codeElement = preClone.selectFirst("code")
            val rawText = codeElement?.wholeText() ?: preClone.wholeText().ifEmpty { preClone.text() }
            // Remove code fences if typed inside the pre block
            val unescapedText = org.jsoup.parser.Parser.unescapeEntities(rawText, true)
                .replace(Regex("""^```[a-zA-Z0-9_-]*\r?\n"""), "")
                .replace(Regex("""\r?\n```$"""), "")
                .trimIndent()
                .trim('\r', '\n')
            val dataCopyAttr = element.attr("data-copy").trim()
            val showCopy = !dataCopyAttr.equals("false", ignoreCase = true)
            val rawPart = element.attr("data-part").toIntOrNull()
            val rawPartLabel = element.attr("data-part-label").trim()
            val titleAttr = rawPartLabel.ifEmpty {
                if (rawPart != null) "Part $rawPart" else ""
            }.ifEmpty { element.attr("data-title") }.ifEmpty { element.attr("title") }.ifEmpty {
                if (element.hasClass("prompt") || element.hasClass("prompt-box")) "Prompt"
                else if (element.hasClass("summary") || element.hasClass("summary-box")) "Summary"
                else ""
            }
            if (unescapedText.isNotEmpty()) {
                outBlocks.add(
                    ArticleBlock.CodeBlock(
                        code = unescapedText,
                        showCopyButton = showCopy,
                        startIndent = indent,
                        title = titleAttr
                    )
                )
            }
        }
        "code" -> {
            val rawText = element.wholeText().ifEmpty { element.text() }
            val cleanText = org.jsoup.parser.Parser.unescapeEntities(rawText, true)
                .trimIndent()
                .trim('\r', '\n')
            val dataCopyAttr = element.attr("data-copy").trim()
            val showCopy = !dataCopyAttr.equals("false", ignoreCase = true)
            val rawPart = element.attr("data-part").toIntOrNull()
            val rawPartLabel = element.attr("data-part-label").trim()
            val titleAttr = rawPartLabel.ifEmpty {
                if (rawPart != null) "Part $rawPart" else ""
            }.ifEmpty { element.attr("data-title") }.ifEmpty { element.attr("title") }.ifEmpty { "Prompt" }
            if (cleanText.isNotEmpty()) {
                outBlocks.add(
                    ArticleBlock.CodeBlock(
                        code = cleanText,
                        showCopyButton = showCopy,
                        startIndent = indent,
                        title = titleAttr
                    )
                )
            }
        }
        "blockquote" -> {
            val inline = buildInlineAnnotatedString(element, primaryColor, onSurfaceColor, surfaceVariantColor)
            if (inline.isNotBlank()) outBlocks.add(ArticleBlock.Blockquote(inline, startIndent = indent))
        }
        "table" -> {
            val headers = mutableListOf<AnnotatedString>()
            val rows = mutableListOf<List<AnnotatedString>>()

            element.select("tr").forEachIndexed { index, tr ->
                val ths = tr.select("th")
                if (ths.isNotEmpty()) {
                    ths.forEach { th ->
                        headers.add(buildInlineAnnotatedString(th, primaryColor, onSurfaceColor, surfaceVariantColor))
                    }
                } else {
                    val tds = tr.select("td")
                    if (tds.isNotEmpty()) {
                        val rowCells = tds.map { td ->
                            buildInlineAnnotatedString(td, primaryColor, onSurfaceColor, surfaceVariantColor)
                        }
                        if (headers.isEmpty() && index == 0) {
                            headers.addAll(rowCells)
                        } else {
                            rows.add(rowCells)
                        }
                    }
                }
            }
            if (headers.isNotEmpty() || rows.isNotEmpty()) {
                outBlocks.add(ArticleBlock.Table(headers, rows, startIndent = indent))
            }
        }
        "hr" -> {
            outBlocks.add(ArticleBlock.Divider)
        }
        "div", "section", "article", "main", "aside" -> {
            val isCodeBlockCard = element.hasClass("code-block-card") || element.hasClass("code-block") ||
                element.hasAttr("data-part") || element.hasAttr("data-part-label")
            val isPrompt = element.hasClass("prompt-box") || element.hasClass("prompt") || isCodeBlockCard
            val isSummary = element.hasClass("summary-box") || element.hasClass("summary")

            // Check if container holds block-level child elements (paragraphs, headings, pre, lists, or child containers)
            val hasChildBlocks = !isCodeBlockCard && element.children().any { child ->
                child.tagName().lowercase() in listOf(
                    "p", "h1", "h2", "h3", "h4", "h5", "h6",
                    "ul", "ol", "pre", "blockquote", "table",
                    "div", "section", "article", "main", "aside", "hr"
                )
            }

            if (hasChildBlocks) {
                // Recursively process children so normal text stays in Paragraphs and code blocks stay isolated in CodeBlocks
                for (child in element.children()) {
                    processElementAsBlock(child, outBlocks, primaryColor, onSurfaceColor, surfaceVariantColor)
                }
            } else if (isPrompt || isSummary) {
                val divClone = element.clone()
                divClone.select("button, .copy-btn, .copy-button, .btn-copy, .prompt-header, .prompt-title, [data-ui='copy'], .part-label, .part_label, .card-label, .code-label").remove()
                val codeElement = divClone.selectFirst("code, pre, .code-content, textarea")
                val rawText = codeElement?.wholeText() ?: divClone.wholeText().ifEmpty { divClone.text() }
                val cleanText = org.jsoup.parser.Parser.unescapeEntities(rawText, true)
                    .replace(Regex("""^```[a-zA-Z0-9_-]*\r?\n"""), "")
                    .replace(Regex("""\r?\n```$"""), "")
                    .trimIndent()
                    .trim('\r', '\n')
                val rawPart = element.attr("data-part").toIntOrNull()
                val rawPartLabel = element.attr("data-part-label").trim()
                val title = rawPartLabel.ifEmpty {
                    if (rawPart != null) "Part $rawPart" else ""
                }.ifEmpty { element.attr("data-title") }.ifEmpty { element.attr("title") }.ifEmpty {
                    if (isSummary) "Summary" else "Prompt"
                }
                val dataCopyAttr = element.attr("data-copy").trim()
                val showCopy = !dataCopyAttr.equals("false", ignoreCase = true)
                if (cleanText.isNotEmpty()) {
                    outBlocks.add(
                        ArticleBlock.CodeBlock(
                            code = cleanText,
                            showCopyButton = showCopy,
                            startIndent = indent,
                            title = title
                        )
                    )
                }
            } else {
                val inline = buildInlineAnnotatedString(element, primaryColor, onSurfaceColor, surfaceVariantColor)
                if (inline.isNotBlank()) {
                    outBlocks.add(ArticleBlock.Paragraph(inline, alignment, startIndent = indent))
                }
            }
        }
        else -> {
            val hasChildBlocks = element.children().any { child ->
                child.tagName().lowercase() in listOf(
                    "p", "h1", "h2", "h3", "h4", "h5", "h6",
                    "ul", "ol", "pre", "blockquote", "table",
                    "div", "section", "article", "main", "aside", "hr"
                )
            }
            if (hasChildBlocks) {
                for (child in element.children()) {
                    processElementAsBlock(child, outBlocks, primaryColor, onSurfaceColor, surfaceVariantColor)
                }
            } else {
                val inline = buildInlineAnnotatedString(element, primaryColor, onSurfaceColor, surfaceVariantColor)
                if (inline.isNotBlank()) {
                    outBlocks.add(ArticleBlock.Paragraph(inline, alignment, startIndent = indent))
                }
            }
        }
    }
}

/**
 * Handles lists and recursive nested lists at arbitrary depth levels.
 * Matches Apex Tech reference screenshots:
 * Unordered: depth 0 -> '•', depth 1 -> '◦', depth 2+ -> '▪'
 * Ordered: depth 0 -> '1.', '2.', etc.
 */
private fun processListElement(
    listElement: Element,
    outBlocks: MutableList<ArticleBlock>,
    isOrdered: Boolean,
    depth: Int,
    primaryColor: Color,
    onSurfaceColor: Color,
    surfaceVariantColor: Color
) {
    var itemIndex = 1

    for (child in listElement.children()) {
        if (child.tagName().equals("li", ignoreCase = true)) {
            val prefix = if (isOrdered) {
                "${itemIndex}."
            } else {
                when (depth) {
                    0 -> "•"
                    1 -> "◦"
                    else -> "▪"
                }
            }
            itemIndex++

            val indent = parseIndentDp(child)

            // Extract child lists for recursion
            val nestedLists = child.children().filter {
                it.tagName().equals("ul", ignoreCase = true) || it.tagName().equals("ol", ignoreCase = true)
            }

            // Remove nested lists temporarily to extract li direct text
            val liClone = child.clone()
            liClone.select("ul, ol").remove()

            val inline = buildInlineAnnotatedString(liClone, primaryColor, onSurfaceColor, surfaceVariantColor)
            if (inline.isNotBlank()) {
                outBlocks.add(ArticleBlock.ListItem(depth, prefix, inline, parseAlignment(child), indent))
            }

            // Recursively process any nested lists
            nestedLists.forEach { subList ->
                val isSubOrdered = subList.tagName().equals("ol", ignoreCase = true)
                processListElement(
                    listElement = subList,
                    outBlocks = outBlocks,
                    isOrdered = isSubOrdered,
                    depth = depth + 1,
                    primaryColor = primaryColor,
                    onSurfaceColor = onSurfaceColor,
                    surfaceVariantColor = surfaceVariantColor
                )
            }
        }
    }
}

/**
 * Parses inline elements and attributes into an [AnnotatedString].
 */
fun buildInlineAnnotatedString(
    element: Element,
    primaryColor: Color,
    onSurfaceColor: Color,
    surfaceVariantColor: Color
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    for (child in element.childNodes()) {
        appendInlineNode(child, builder, primaryColor, onSurfaceColor, surfaceVariantColor)
    }
    return builder.toAnnotatedString()
}

private fun appendInlineNode(
    node: Node,
    builder: AnnotatedString.Builder,
    primaryColor: Color,
    onSurfaceColor: Color,
    surfaceVariantColor: Color
) {
    when (node) {
        is TextNode -> {
            // Remove zero-width spaces common in rich-text editor outputs
            val text = node.text().replace("\u200B", "")
            builder.append(text)
        }
        is Element -> {
            val tag = node.tagName().lowercase()
            when (tag) {
                "strong", "b" -> {
                    builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        for (child in node.childNodes()) {
                            appendInlineNode(child, builder, primaryColor, onSurfaceColor, surfaceVariantColor)
                        }
                    }
                }
                "em", "i" -> {
                    builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        for (child in node.childNodes()) {
                            appendInlineNode(child, builder, primaryColor, onSurfaceColor, surfaceVariantColor)
                        }
                    }
                }
                "u" -> {
                    builder.withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        for (child in node.childNodes()) {
                            appendInlineNode(child, builder, primaryColor, onSurfaceColor, surfaceVariantColor)
                        }
                    }
                }
                "s", "strike", "del" -> {
                    builder.withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        for (child in node.childNodes()) {
                            appendInlineNode(child, builder, primaryColor, onSurfaceColor, surfaceVariantColor)
                        }
                    }
                }
                "code" -> {
                    builder.withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = surfaceVariantColor.copy(alpha = 0.6f),
                            color = primaryColor
                        )
                    ) {
                        for (child in node.childNodes()) {
                            appendInlineNode(child, builder, primaryColor, onSurfaceColor, surfaceVariantColor)
                        }
                    }
                }
                "a" -> {
                    val href = node.attr("href").trim()
                    if (href.isNotBlank()) {
                        try {
                            builder.withLink(LinkAnnotation.Url(url = href)) {
                                builder.withStyle(
                                    SpanStyle(
                                        color = primaryColor,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    for (child in node.childNodes()) {
                                        appendInlineNode(child, builder, primaryColor, onSurfaceColor, surfaceVariantColor)
                                    }
                                }
                            }
                        } catch (t: Throwable) {
                            builder.withStyle(
                                SpanStyle(
                                    color = primaryColor,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                for (child in node.childNodes()) {
                                    appendInlineNode(child, builder, primaryColor, onSurfaceColor, surfaceVariantColor)
                                }
                            }
                        }
                    } else {
                        for (child in node.childNodes()) {
                            appendInlineNode(child, builder, primaryColor, onSurfaceColor, surfaceVariantColor)
                        }
                    }
                }
                "br" -> {
                    builder.append("\n")
                }
                else -> {
                    val style = node.attr("style").lowercase()
                    val spanStyles = mutableListOf<SpanStyle>()

                    if (style.contains("font-weight: bold") || style.contains("font-weight: 700")) {
                        spanStyles.add(SpanStyle(fontWeight = FontWeight.Bold))
                    }
                    if (style.contains("font-style: italic")) {
                        spanStyles.add(SpanStyle(fontStyle = FontStyle.Italic))
                    }
                    if (style.contains("text-decoration: underline")) {
                        spanStyles.add(SpanStyle(textDecoration = TextDecoration.Underline))
                    }
                    if (style.contains("text-decoration: line-through")) {
                        spanStyles.add(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    }

                    fun applyNested(index: Int) {
                        if (index < spanStyles.size) {
                            builder.withStyle(spanStyles[index]) {
                                applyNested(index + 1)
                            }
                        } else {
                            for (child in node.childNodes()) {
                                appendInlineNode(child, builder, primaryColor, onSurfaceColor, surfaceVariantColor)
                            }
                        }
                    }
                    applyNested(0)
                }
            }
        }
    }
}

/**
 * Extracts text alignment from style, class, or align attributes.
 */
fun parseAlignment(element: Element): TextAlign {
    val style = element.attr("style").lowercase()
    val className = element.className().lowercase()
    val alignAttr = element.attr("align").lowercase()

    return when {
        style.contains("text-align: center") || className.contains("align-center") || alignAttr == "center" -> TextAlign.Center
        style.contains("text-align: right") || className.contains("align-right") || alignAttr == "right" -> TextAlign.End
        style.contains("text-align: justify") || className.contains("align-justify") || alignAttr == "justify" -> TextAlign.Justify
        else -> TextAlign.Start
    }
}

/**
 * Parses markdown or plain text, isolating code blocks fenced by ``` or ~~~
 * so that normal article text becomes Paragraphs/Headings/ListItems, and code blocks
 * become distinct [ArticleBlock.CodeBlock] instances holding strictly the isolated code.
 */
fun parseMarkdownOrPlainText(text: String): List<ArticleBlock> {
    val lines = text.lines()
    val blocks = mutableListOf<ArticleBlock>()
    var inCodeFence = false
    var fenceDelimiter = ""
    var codeFenceTitle = "Prompt"
    val currentCodeLines = mutableListOf<String>()
    val currentTextLines = mutableListOf<String>()

    fun flushTextLines() {
        if (currentTextLines.isEmpty()) return
        val combined = currentTextLines.joinToString("\n").trim()
        currentTextLines.clear()
        if (combined.isEmpty()) return
        val paragraphs = combined.split(Regex("""\n\s*\n"""))
        for (p in paragraphs) {
            val cleanP = ArticleSanitizer.cleanPrefix(p).trim()
            if (cleanP.isNotEmpty()) {
                blocks.add(parsePlainTextParagraph(cleanP))
            }
        }
    }

    for (line in lines) {
        val trimmedLine = line.trim()
        if (!inCodeFence) {
            if (trimmedLine.startsWith("```") || trimmedLine.startsWith("~~~")) {
                flushTextLines()
                inCodeFence = true
                fenceDelimiter = if (trimmedLine.startsWith("```")) "```" else "~~~"
                val info = trimmedLine.removePrefix(fenceDelimiter).trim()
                codeFenceTitle = if (info.contains("prompt", ignoreCase = true)) "Prompt"
                    else if (info.contains("summary", ignoreCase = true)) "Summary"
                    else if (info.isNotBlank()) info
                    else "Prompt"
                currentCodeLines.clear()
            } else {
                currentTextLines.add(line)
            }
        } else {
            if (trimmedLine.startsWith(fenceDelimiter)) {
                // Closing fence
                inCodeFence = false
                val codeContent = currentCodeLines.joinToString("\n")
                val cleanCode = codeContent.trimIndent().trim('\r', '\n')
                if (cleanCode.isNotEmpty()) {
                    blocks.add(
                        ArticleBlock.CodeBlock(
                            code = cleanCode,
                            showCopyButton = true,
                            title = codeFenceTitle
                        )
                    )
                }
                currentCodeLines.clear()
            } else {
                currentCodeLines.add(line)
            }
        }
    }

    if (inCodeFence && currentCodeLines.isNotEmpty()) {
        val codeContent = currentCodeLines.joinToString("\n")
        val cleanCode = codeContent.trimIndent().trim('\r', '\n')
        if (cleanCode.isNotEmpty()) {
            blocks.add(
                ArticleBlock.CodeBlock(
                    code = cleanCode,
                    showCopyButton = true,
                    title = codeFenceTitle
                )
            )
        }
    } else {
        flushTextLines()
    }

    return blocks
}

/**
 * Parses a plain text paragraph, preserving bullet lines, numbered lists, or markdown headings.
 */
private fun parsePlainTextParagraph(paragraph: String): ArticleBlock {
    val trimmed = paragraph.trim()

    // Markdown headings
    if (trimmed.startsWith("# ")) {
        return ArticleBlock.Heading(level = 1, text = AnnotatedString(trimmed.removePrefix("# ").trim()))
    }
    if (trimmed.startsWith("## ")) {
        return ArticleBlock.Heading(level = 2, text = AnnotatedString(trimmed.removePrefix("## ").trim()))
    }
    if (trimmed.startsWith("### ")) {
        return ArticleBlock.Heading(level = 3, text = AnnotatedString(trimmed.removePrefix("### ").trim()))
    }

    // Bullet point starting with • or - or *
    val bulletMatch = Regex("""^([•\-\*])\s+(.+)$""", RegexOption.DOT_MATCHES_ALL).find(trimmed)
    if (bulletMatch != null) {
        val body = bulletMatch.groupValues[2].trim()
        return ArticleBlock.ListItem(
            depth = 0,
            prefix = "•",
            text = AnnotatedString(body)
        )
    }

    // Numbered list starting with e.g. "1. " or "1) "
    val numberMatch = Regex("""^(\d+)[\.\)]\s+(.+)$""", RegexOption.DOT_MATCHES_ALL).find(trimmed)
    if (numberMatch != null) {
        val num = numberMatch.groupValues[1]
        val body = numberMatch.groupValues[2].trim()
        return ArticleBlock.ListItem(
            depth = 0,
            prefix = "$num.",
            text = AnnotatedString(body)
        )
    }

    return ArticleBlock.Paragraph(text = AnnotatedString(trimmed))
}

/**
 * Renders a prompt box matching the reference visual style:
 * - Clean card container with rounded corners and subtle border
 * - Optional title (e.g. "Summary" or "Prompt")
 * - Prompt text arranged line by line, preserving line breaks and wrapping without overflow
 * - "Copy prompt" button placed below the prompt at the very end of the box
 * - No overlap, clean spacing, responsive across screens
 * - Copies complete prompt text preserving line breaks without extra actions
 */
@Composable
fun PromptBox(
    promptText: String,
    title: String = "",
    showCopyButton: Boolean = true,
    startIndent: Dp = 0.dp,
    baseFontSize: TextUnit = 16.sp,
    lineSpacing: TextUnit = 24.sp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember(promptText) { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    // Isolate strictly this specific code block's text, completely exempt from article title, body, or other code blocks
    val performCopy: () -> Unit = remember(promptText, title, context) {
        {
            val isolatedText = promptText
                .replace('\u00A0', ' ')
                .replace("\u200B", "")
                .trim()
            if (isolatedText.isNotEmpty()) {
                val clipLabel = if (title.isNotBlank()) title else "Prompt"
                val success = IntentUtils.copyToClipboard(context, isolatedText, clipLabel)
                if (success) {
                    isCopied = true
                }
            } else {
                Toast.makeText(context, "Cannot copy: Code block is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val isDark = isSystemInDarkTheme()
    val containerBg = if (isDark) Color(0xFF222622) else Color(0xFFEFF2EC)
    val textFg = if (isDark) Color(0xFFE2E4DE) else Color(0xFF1E1E1E)
    val borderColor = if (isDark) Color(0xFF333D33) else Color(0xFFE0E4DC)

    val fontSize = (baseFontSize.value * 0.92f).coerceIn(14f, 15.5f).sp
    val lineHeight = (fontSize.value * 1.65f).sp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startIndent, top = 10.dp, bottom = 12.dp)
            .testTag("prompt_box"),
        shape = RoundedCornerShape(16.dp),
        color = containerBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = textFg,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // Prompt text arranged line by line, preserving whitespace, indentation, and line breaks
            Text(
                text = promptText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontFamily = FontFamily.Monospace
                ),
                color = textFg,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prompt_box_text")
            )

            // Independent Copy prompt button placed at the bottom of the code block
            if (showCopyButton) {
                Spacer(modifier = Modifier.height(18.dp))
                val buttonLabel = if (title.isNotBlank()) "Copy $title" else "Copy prompt"
                Surface(
                    onClick = { performCopy() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0D1117),
                    contentColor = Color.White,
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("copy_prompt_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCopied) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Copied",
                                tint = Color(0xFF3DDC84),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Copied!",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = buttonLabel,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = buttonLabel,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

