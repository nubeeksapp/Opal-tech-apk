package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import android.widget.Toast
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ads.AdManager
import com.example.ads.BannerAdView
import com.example.ads.NativeAdCard
import com.example.data.model.TechArticle
import com.example.data.model.TextSizePreference
import com.example.utils.IntentUtils
import com.example.utils.ResourceHelper

@Composable
fun DetailScreen(
    article: TechArticle,
    textSizePreference: TextSizePreference,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCycleTextSize: () -> Unit,
    recommendedArticles: List<TechArticle> = emptyList(),
    onArticleClick: (String) -> Unit = {},
    onToggleFavoriteArticle: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageDrawableId = ResourceHelper.getDrawableId(context, article.imageResName)

    // Dynamic text sizing
    val baseFontSize = 16.sp * textSizePreference.scale
    val lineSpacing = 24.sp * textSizePreference.scale

    val displayTitle: String = androidx.compose.runtime.remember(article.title) {
        com.example.utils.ArticleSanitizer.cleanPrefix(article.title)
    }
    val displaySummary: String = androidx.compose.runtime.remember(article.summary) {
        com.example.utils.ArticleSanitizer.cleanPrefix(article.summary)
    }
    val displayDescription: List<String> = androidx.compose.runtime.remember(article.fullDescription) {
        com.example.utils.ArticleSanitizer.cleanList(article.fullDescription)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("detail_screen")
    ) {
        // Top Navigation Bar (matches Screenshot 1)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // TT text size toggle icon (with current size indication)
                IconButton(
                    onClick = onCycleTextSize,
                    modifier = Modifier.testTag("detail_text_size_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.TextFields,
                            contentDescription = "Adjust text size (Current: ${textSizePreference.displayName})",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Favorite Heart icon
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("detail_favorite_button")
                ) {
                    Icon(
                        imageVector = if (article.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (article.isFavorite) "Remove Bookmark" else "Bookmark",
                        tint = if (article.isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Scrollable Article Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // High-impact 16:9 Thumbnail Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                val imageModel = if (!article.thumbnailUrl.isNullOrBlank()) {
                    ImageRequest.Builder(context)
                        .data(article.thumbnailUrl)
                        .crossfade(true)
                        .placeholder(imageDrawableId)
                        .error(imageDrawableId)
                        .build()
                } else {
                    imageDrawableId
                }

                AsyncImage(
                    model = imageModel,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                // Headline
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 30.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date, Exact Time, View Count, and Share Row (matches Screenshots 1 & 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Clock Icon + Date with Exact Time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = "Post time",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = article.publishDate,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    // Right: View Count (Eye Icon + Count) and Share Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.R.drawable.ic_eye),
                            contentDescription = "Views",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = article.views,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        IconButton(
                            onClick = {
                                IntentUtils.shareArticle(context, displayTitle, displaySummary, article.playStoreUrl)
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("detail_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(18.dp))

                // Rich-Text Body Content (renders headings, bold, nested lists, code blocks, tables, blockquotes, and plain text)
                com.example.ui.components.RichArticleContent(
                    article = article,
                    baseFontSize = baseFontSize,
                    lineSpacing = lineSpacing
                )

                // Optional Generate Prompt Section (placed at the end of the normal body content)
                val effectivePrompt = article.generatePrompt.ifBlank { article.prompt }.trim()
                if (effectivePrompt.isNotBlank() && (article.generatePromptEnabled || article.generatePrompt.isNotBlank())) {
                    val promptArticle = if (article.generatePrompt.isBlank()) {
                        article.copy(generatePrompt = effectivePrompt, generatePromptEnabled = true)
                    } else {
                        article
                    }
                    com.example.ui.components.GeneratePromptSection(
                        article = promptArticle,
                        baseFontSize = baseFontSize,
                        lineSpacing = lineSpacing
                    )
                }

                // Breakdown Section: "🏆 Who It's For" (as requested in prompt & Screenshot 1)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "🏆 Who It's For",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (baseFontSize.value + 2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                article.whoItsFor.forEach { point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "✔",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = baseFontSize,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = baseFontSize,
                                lineHeight = lineSpacing
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Key Features Section
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "⭐ Key Features",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (baseFontSize.value + 2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                article.keyFeatures.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp, end = 10.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = baseFontSize,
                                lineHeight = lineSpacing
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Sleek Native Ad placed naturally at the end of the article immediately above "Suggested"
                Spacer(modifier = Modifier.height(20.dp))
                NativeAdCard(
                    modifier = Modifier.fillMaxWidth(),
                    showMedia = true
                )

                // Recommendation articles under "Suggested" (matches reference screenshot: "| Suggested")
                if (recommendedArticles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("detail_suggested_header")
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(18.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Suggested",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (baseFontSize.value + 2).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    recommendedArticles.forEach { recommended ->
                        com.example.ui.components.ArticleFeedCard(
                            article = recommended,
                            onClick = { onArticleClick(recommended.id) },
                            onToggleFavorite = { onToggleFavoriteArticle(recommended.id) },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
