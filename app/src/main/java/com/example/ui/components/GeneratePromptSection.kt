package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.data.model.TechArticle
import com.example.utils.SceneSplitter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Optional Generate Prompt Section managed by the Opal Tech admin panel.
 *
 * Rules:
 * 1. Only displayed if article has an enabled, valid prewritten prompt.
 * 2. Before user clicks Generate:
 *    - Show ONLY one Generate button.
 *    - Do not show prewritten prompt, scenes, raw data, or empty placeholder containers.
 * 3. Animated Prompt Generation:
 *    - Generate button smoothly animates into a fixed-height, scrollable prompt box.
 *    - The original button is hidden so it is not duplicated on page.
 *    - Prewritten prompt is progressively revealed line-by-line / word-by-word.
 *    - Fixed-height container prevents page layout jumping or pushing the page down.
 *    - Auto-scrolls as new text appears to keep latest content visible.
 * 4. Show Copy only after generation finishes:
 *    - Two adjacent buttons directly below prompt box: "Copy" and "All Scenes".
 *    - "Copy" copies strictly the entire prewritten prompt without article body or headers.
 * 5. All Scenes:
 *    - Expands cards labeled Part 1, Part 2, ...
 *    - Each card has an independent Copy button copying strictly its own text.
 * 6. No ads, no "Watch ad to copy", no thumbnail banners, no external AI launcher links.
 */
@Composable
fun GeneratePromptSection(
    article: TechArticle,
    baseFontSize: TextUnit,
    lineSpacing: TextUnit,
    modifier: Modifier = Modifier
) {
    val fullPrompt = article.generatePrompt.trim()
    if (!article.generatePromptEnabled || fullPrompt.isBlank()) {
        return
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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

    // Auto-scroll the fixed-height container as new words and lines are revealed
    LaunchedEffect(displayedPrompt) {
        if (isGenerating) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Split text into progressive token indices (by word boundary)
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

    // Calculate step delay based on prompt length (balanced for mobile smoothness)
    val stepDelay = remember(tokenIndices.size) {
        (1200L / tokenIndices.size.coerceAtLeast(1)).coerceIn(8L, 24L)
    }

    val isDark = isSystemInDarkTheme()
    val containerBg = if (isDark) Color(0xFF222622) else Color(0xFFEFF2EC)
    val textFg = if (isDark) Color(0xFFE2E4DE) else Color(0xFF1E1E1E)
    val borderColor = if (isDark) Color(0xFF333D33) else Color(0xFFE0E4DC)

    val promptFontSize = (baseFontSize.value * 0.92f).coerceIn(14f, 15.5f).sp
    val promptLineHeight = (promptFontSize.value * 1.65f).sp

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
        copyTextToClipboard(
            context = context,
            text = fullPrompt,
            label = "Complete Prompt",
            onSuccess = { isFullCopied = true }
        )
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
                // Initial State: ONLY one Generate button displayed at end of article
                Surface(
                    onClick = { onGenerateClick() },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0D1117),
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
                // Active / Completed State: Prompt Box in fixed-height scrollable container
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = containerBg,
                        border = BorderStroke(1.dp, borderColor),
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
                                color = textFg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generated_prompt_text")
                            )
                        }
                    }

                    // Show action buttons ONLY after generation finishes
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
                                color = Color(0xFF0D1117),
                                contentColor = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
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

                            // 2. All Scenes Button
                            Surface(
                                onClick = { showAllScenes = !showAllScenes },
                                shape = RoundedCornerShape(12.dp),
                                color = if (showAllScenes) MaterialTheme.colorScheme.primary else Color(0xFF0D1117),
                                contentColor = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("all_scenes_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Layers,
                                        contentDescription = "All Scenes",
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
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

                        // All Scenes Section (Part 1, Part 2, ... with independent Copy buttons)
                        AnimatedVisibility(
                            visible = showAllScenes,
                            enter = fadeIn(tween(250)) + expandVertically(tween(350))
                        ) {
                            val scenes = remember(fullPrompt, article.sceneCount, article.scenes) {
                                SceneSplitter.splitIntoScenes(
                                    prompt = fullPrompt,
                                    targetCount = article.sceneCount,
                                    explicitScenes = article.scenes
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .testTag("scenes_container"),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                scenes.forEachIndexed { index, sceneContent ->
                                    ScenePartBox(
                                        partNumber = index + 1,
                                        sceneText = sceneContent,
                                        containerBg = containerBg,
                                        borderColor = borderColor,
                                        textFg = textFg,
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
 * Renders an individual scene part (Part 1, Part 2, etc.) as a separate card with its own independent Copy button.
 */
@Composable
private fun ScenePartBox(
    partNumber: Int,
    sceneText: String,
    containerBg: Color,
    borderColor: Color,
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

    val isolatedText = remember(sceneText) {
        com.example.utils.SceneSplitter.cleanPartContent(sceneText)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerBg,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("scene_part_${partNumber}_box")
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
                    text = "Part $partNumber",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = textFg,
                    modifier = Modifier.testTag("scene_part_${partNumber}_title")
                )

                // Independent Copy button for this specific part
                Surface(
                    onClick = {
                        copyTextToClipboard(
                            context = context,
                            text = isolatedText,
                            label = "Part $partNumber",
                            onSuccess = { isPartCopied = true }
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0D1117),
                    contentColor = Color.White,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("copy_part_${partNumber}_button")
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
                                contentDescription = "Copy Part $partNumber",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copy Part $partNumber",
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

            // Scene text content (monospace, preserving line breaks, indentation, and wording)
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
                    .testTag("scene_part_${partNumber}_text")
            )
        }
    }
}

/**
 * Copies isolated text directly to Android ClipboardManager with exact whitespace & formatting.
 */
private fun copyTextToClipboard(
    context: Context,
    text: String,
    label: String,
    onSuccess: () -> Unit
) {
    val cleanText = text.trim()
    if (cleanText.isEmpty()) {
        Toast.makeText(context, "Cannot copy: $label is empty", Toast.LENGTH_SHORT).show()
        return
    }
    val success = com.example.utils.IntentUtils.copyToClipboard(
        context = context,
        text = cleanText,
        label = label
    )
    if (success) {
        onSuccess()
    }
}
