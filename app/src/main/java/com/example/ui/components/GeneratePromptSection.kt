package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SceneItem
import com.example.data.model.TechArticle
import com.example.utils.IntentUtils
import com.example.utils.SceneSplitter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Interactive "Generate" and "All Scenes" Prompt section in Opal Tech article details screen.
 * Matches connected Admin Panel schema and the reference design screenshots.
 *
 * Rules:
 * 1. Initial State:
 *    - Shown only if generatePromptEnabled is true and valid prompt/scenes data exists.
 *    - Displays a single full-width dark pill button "▶  Generate".
 *    - Never displays placeholder buttons or empty containers if disabled/empty.
 * 2. Animated Prompt Generation:
 *    - On tapping "Generate", smoothly animates into a fixed-height (210dp) scrollable card.
 *    - Uses Monospace font, off-white text (#E6EDF3), and deep charcoal (#1E232B) background.
 *    - Streaming / typewriter animation progressively reveals text and auto-scrolls down.
 * 3. Two Action Buttons below prompt card:
 *    - Left: "Copy" (dark button, copies entire prompt, toast: "Prompt copied to clipboard").
 *    - Right: "All Scenes" / "Hide Scenes" (toggles blue #0B57D0 when active).
 * 4. Multi-Part Scenes Stack:
 *    - When "All Scenes" is tapped, shows "Hide Scenes" and reveals individual cards for Part 1, Part 2, etc.
 *    - Each card has an independent "Copy Part X" button copying strictly that part's text with toast: "Copied Part X to clipboard".
 */
@Composable
fun GeneratePromptSection(
    article: TechArticle,
    baseFontSize: TextUnit,
    lineSpacing: TextUnit,
    modifier: Modifier = Modifier
) {
    // Check if prompt is enabled and non-empty
    val hasContent = article.generatePrompt.isNotBlank() ||
        article.prompt.isNotBlank() ||
        article.scenes.isNotEmpty() ||
        article.sceneItems.isNotEmpty()

    if (!article.generatePromptEnabled || !hasContent) {
        return
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Resolve scenes list
    val resolvedScenes: List<SceneItem> = remember(article.id, article.generatePrompt, article.prompt, article.sceneCount, article.scenes, article.sceneItems) {
        val basePrompt = article.generatePrompt.ifBlank { article.prompt }.trim()
        when {
            article.sceneItems.isNotEmpty() -> article.sceneItems
            article.scenes.isNotEmpty() -> {
                article.scenes.mapIndexed { idx, s ->
                    SceneItem(
                        part = idx + 1,
                        label = "Part ${idx + 1}",
                        content = SceneSplitter.cleanPartContent(s)
                    )
                }
            }
            basePrompt.isNotEmpty() -> {
                val natural = SceneSplitter.detectNaturalScenes(basePrompt)
                if (natural.isNotEmpty()) {
                    natural.mapIndexed { idx, s ->
                        SceneItem(
                            part = idx + 1,
                            label = "Part ${idx + 1}",
                            content = SceneSplitter.cleanPartContent(s)
                        )
                    }
                } else if (article.sceneCount > 1) {
                    val partitioned = SceneSplitter.partitionTextBalanced(basePrompt, article.sceneCount)
                    partitioned.mapIndexed { idx, s ->
                        SceneItem(
                            part = idx + 1,
                            label = "Part ${idx + 1}",
                            content = SceneSplitter.cleanPartContent(s)
                        )
                    }
                } else {
                    listOf(
                        SceneItem(
                            part = 1,
                            label = "Part 1",
                            content = SceneSplitter.cleanPartContent(basePrompt)
                        )
                    )
                }
            }
            else -> emptyList()
        }
    }

    // Resolve full prompt text for the main box and copy
    val fullPrompt = remember(article.generatePrompt, article.prompt, resolvedScenes) {
        val candidate = article.generatePrompt.ifBlank { article.prompt }.trim()
        if (candidate.isNotEmpty()) {
            candidate
        } else if (resolvedScenes.isNotEmpty()) {
            resolvedScenes.joinToString("\n\n") { "${it.label}:\n${it.content}" }
        } else {
            ""
        }
    }

    if (fullPrompt.isBlank() && resolvedScenes.isEmpty()) {
        return
    }

    var hasStarted by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var displayedPrompt by remember { mutableStateOf("") }
    var showAllScenes by remember { mutableStateOf(false) }
    var isFullCopied by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Reset full copied indicator after 2 seconds
    LaunchedEffect(isFullCopied) {
        if (isFullCopied) {
            delay(2000)
            isFullCopied = false
        }
    }

    // Auto-scroll the fixed-height container as new words and lines appear
    LaunchedEffect(displayedPrompt) {
        if (isGenerating) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Split text into progressive word boundaries for typewriter effect
    val tokenIndices = remember(fullPrompt) {
        val indices = mutableListOf<Int>()
        var inWhitespace = false
        for (i in fullPrompt.indices) {
            val c = fullPrompt[i]
            if (c.isWhitespace()) {
                if (!inWhitespace) {
                    indices.add(i)
                    inWhitespace = true
                }
            } else {
                inWhitespace = false
            }
        }
        if (indices.isEmpty() || indices.last() != fullPrompt.length) {
            indices.add(fullPrompt.length)
        }
        indices
    }

    val stepDelay = remember(tokenIndices.size) {
        (1100L / tokenIndices.size.coerceAtLeast(1)).coerceIn(8L, 22L)
    }

    // Styling constants matching reference screenshots
    val darkCharcoalCardBg = Color(0xFF1E232B)
    val cardBorderColor = Color(0xFF2E3440)
    val monospaceTextColor = Color(0xFFE6EDF3)
    val buttonDarkBg = Color(0xFF121620)
    val googleBlueColor = Color(0xFF0B57D0)

    val promptFontSize = (baseFontSize.value * 0.92f).coerceIn(14f, 15.5f).sp
    val promptLineHeight = (promptFontSize.value * 1.6f).sp

    fun onGenerateClick() {
        if (hasStarted || isGenerating || isCompleted) return
        hasStarted = true
        isGenerating = true
        coroutineScope.launch {
            for (idx in tokenIndices) {
                displayedPrompt = fullPrompt.substring(0, idx)
                delay(stepDelay)
            }
            displayedPrompt = fullPrompt
            isGenerating = false
            isCompleted = true
        }
    }

    fun onCopyFullPrompt() {
        val success = IntentUtils.copyToClipboard(
            context = context,
            text = fullPrompt,
            label = "Prompt"
        )
        if (success) {
            isFullCopied = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp)
            .testTag("generate_prompt_section")
    ) {
        AnimatedContent(
            targetState = hasStarted,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
            },
            label = "generate_transition"
        ) { started ->
            if (!started) {
                // Initial State: Single full-width dark pill button (Reference Screenshot 1)
                Surface(
                    onClick = { onGenerateClick() },
                    shape = RoundedCornerShape(14.dp),
                    color = buttonDarkBg,
                    contentColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("generate_prompt_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Generate",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            } else {
                // Active / Completed State: Prompt Box in fixed-height scrollable container (Reference Screenshot 2)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = darkCharcoalCardBg,
                        border = BorderStroke(1.dp, cardBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .testTag("generated_prompt_box")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(scrollState)
                        ) {
                            Text(
                                text = if (isGenerating) "$displayedPrompt ▊" else displayedPrompt,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = promptFontSize,
                                    lineHeight = promptLineHeight,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = monospaceTextColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generated_prompt_text")
                            )
                        }
                    }

                    // Show action buttons below the prompt card
                    if (isCompleted) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("prompt_action_buttons_row"),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Copy Complete Prompt Button
                            Surface(
                                onClick = { onCopyFullPrompt() },
                                shape = RoundedCornerShape(12.dp),
                                color = buttonDarkBg,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("copy_full_prompt_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isFullCopied) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Copied",
                                            tint = Color(0xFF3DDC84),
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
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
                                            contentDescription = "Copy prompt",
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Copy",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            // 2. All Scenes / Hide Scenes Button (turns blue when active)
                            Surface(
                                onClick = { showAllScenes = !showAllScenes },
                                shape = RoundedCornerShape(12.dp),
                                color = if (showAllScenes) googleBlueColor else buttonDarkBg,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("all_scenes_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Layers,
                                        contentDescription = if (showAllScenes) "Hide Scenes" else "All Scenes",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (showAllScenes) "Hide Scenes" else "All Scenes",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        // All Scenes Section: Multi-Part Stacked Cards (Reference Screenshot 3)
                        AnimatedVisibility(
                            visible = showAllScenes,
                            enter = fadeIn(tween(250)) + expandVertically(tween(350))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .testTag("scenes_container"),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                resolvedScenes.forEach { sceneItem ->
                                    ScenePartCard(
                                        sceneItem = sceneItem,
                                        cardBg = darkCharcoalCardBg,
                                        borderColor = cardBorderColor,
                                        buttonBg = buttonDarkBg,
                                        textFg = monospaceTextColor,
                                        fontSize = promptFontSize,
                                        lineHeight = promptLineHeight
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders an individual scene part (Part 1, Part 2, etc.) as an independent card with its own Copy button.
 * Matches Reference Screenshot 3.
 */
@Composable
private fun ScenePartCard(
    sceneItem: SceneItem,
    cardBg: Color,
    borderColor: Color,
    buttonBg: Color,
    textFg: Color,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPartCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isPartCopied) {
        if (isPartCopied) {
            delay(2000)
            isPartCopied = false
        }
    }

    val isolatedText = remember(sceneItem.content) {
        SceneSplitter.cleanPartContent(sceneItem.content)
    }

    val displayLabel = sceneItem.label.ifBlank { "Part ${sceneItem.part}" }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("scene_part_${sceneItem.part}_box")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Part title and independent Copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = displayLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.testTag("scene_part_${sceneItem.part}_title")
                )

                // Dedicated Copy button for this specific part
                Surface(
                    onClick = {
                        val success = IntentUtils.copyToClipboard(
                            context = context,
                            text = isolatedText,
                            label = displayLabel
                        )
                        if (success) {
                            isPartCopied = true
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = buttonBg,
                    contentColor = Color.White,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("copy_part_${sceneItem.part}_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPartCopied) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Copied",
                                tint = Color(0xFF3DDC84),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copied!",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy $displayLabel",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copy $displayLabel",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scene text content (monospace, preserving exact wording, code, whitespace)
            Text(
                text = isolatedText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontFamily = FontFamily.Monospace
                ),
                color = textFg,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scene_part_${sceneItem.part}_text")
            )
        }
    }
}
