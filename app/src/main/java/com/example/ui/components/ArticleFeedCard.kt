package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.TechArticle
import com.example.utils.IntentUtils
import com.example.utils.ResourceHelper

@Composable
fun ArticleFeedCard(
    article: TechArticle,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    heroLayout: Boolean = false
) {
    val context = LocalContext.current
    val imageDrawableId = ResourceHelper.getDrawableId(context, article.imageResName)
    val displayTitle: String = androidx.compose.runtime.remember(article.title) {
        com.example.utils.ArticleSanitizer.cleanPrefix(article.title)
    }
    val displaySummary: String = androidx.compose.runtime.remember(article.summary) {
        com.example.utils.ArticleSanitizer.cleanPrefix(article.summary)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("article_card_${article.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (heroLayout) {
            // High-impact 16:9 thumbnail image on top
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
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

                // Date and time right under its thumbnail (hero layout, straight arrangement, brand primary color)
                val heroDateColor = MaterialTheme.colorScheme.primary

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "Published date",
                        tint = heroDateColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = article.publishDate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = heroDateColor
                        ),
                        maxLines = 1,
                        softWrap = true
                    )
                }

                // Body Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = displaySummary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Footer Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                IntentUtils.shareArticle(context, article.title, article.summary, article.playStoreUrl)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("share_btn_${article.id}")
                        ) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Share article",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("favorite_btn_${article.id}")
                            ) {
                                Icon(
                                    imageVector = if (article.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (article.isFavorite) "Remove from favorites" else "Add to favorites",
                                    tint = if (article.isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
        } else {
            // Modern News Feed Layout (Screenshot style): Left rounded thumbnail, right content & actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left: 94dp Rounded Thumbnail Image with Date & Time right under it
                Column(
                    modifier = Modifier.width(96.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Date and time right under its thumbnail (straight arrangement, brand primary color)
                    val dateColor = MaterialTheme.colorScheme.primary

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = dateColor,
                            modifier = Modifier
                                .size(11.dp)
                                .padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = article.publishDate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = dateColor,
                                lineHeight = 12.sp
                            ),
                            maxLines = 2,
                            softWrap = true
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right: Title, Summary, and Footer Row
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 94.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Title
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Short Summary
                        Text(
                            text = displaySummary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 16.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Footer Row: Direct Action Buttons (views hidden on front page, shown when article is clicked)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                IntentUtils.shareArticle(context, article.title, article.summary, article.playStoreUrl)
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("share_btn_${article.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share article",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("favorite_btn_${article.id}")
                        ) {
                            Icon(
                                imageVector = if (article.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (article.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (article.isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
