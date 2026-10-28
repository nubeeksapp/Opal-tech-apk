package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Creates a synchronized linear gradient shimmer brush for animated skeleton loading states.
 */
@Composable
fun rememberShimmerBrush(
    targetValue: Float = 1300f,
    durationMillis: Int = 1200
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val highlightColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)

    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim.value - 350f, translateAnim.value - 350f),
        end = Offset(translateAnim.value, translateAnim.value)
    )
}

/**
 * Convenience modifier to clip and apply a shimmer brush placeholder.
 */
fun Modifier.shimmerPlaceholder(
    brush: Brush,
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier = this
    .clip(shape)
    .background(brush)

/**
 * Skeleton placeholder for an article feed card.
 * Supports both high-impact 16:9 hero layout and compact split layout.
 */
@Composable
fun ArticleFeedCardSkeleton(
    modifier: Modifier = Modifier,
    heroLayout: Boolean = true,
    brush: Brush = rememberShimmerBrush()
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(if (heroLayout) "skeleton_feed_card_hero" else "skeleton_feed_card_compact"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (heroLayout) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 16:9 Thumbnail Skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(brush)
                ) {
                    // Category Tag Badge placeholder
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .size(width = 80.dp, height = 22.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
                    )
                }

                // Body Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Headline lines
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(18.dp)
                            .shimmerPlaceholder(brush, RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(18.dp)
                            .shimmerPlaceholder(brush, RoundedCornerShape(6.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary lines
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.98f)
                            .height(13.dp)
                            .shimmerPlaceholder(brush, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .height(13.dp)
                            .shimmerPlaceholder(brush, RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Footer Section (metadata + buttons)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clock + Date placeholder
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .shimmerPlaceholder(brush, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(width = 96.dp, height = 12.dp)
                                    .shimmerPlaceholder(brush, RoundedCornerShape(4.dp))
                            )
                        }

                        // Action placeholders (Favorite + Menu)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .shimmerPlaceholder(brush, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .shimmerPlaceholder(brush, CircleShape)
                            )
                        }
                    }
                }
            }
        } else {
            // Compact News Feed Skeleton (Left Thumbnail, Right Content & Action Placeholders)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left thumbnail placeholder
                Box(
                    modifier = Modifier
                        .size(94.dp)
                        .shimmerPlaceholder(brush, RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Category Tag placeholder
                    Box(
                        modifier = Modifier
                            .size(width = 70.dp, height = 12.dp)
                            .shimmerPlaceholder(brush, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Headline lines
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(16.dp)
                            .shimmerPlaceholder(brush, RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(16.dp)
                            .shimmerPlaceholder(brush, RoundedCornerShape(6.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Summary line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(12.dp)
                            .shimmerPlaceholder(brush, RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Footer Row (Date on left, 3 icon circles on right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Date placeholder
                        Box(
                            modifier = Modifier
                                .size(width = 75.dp, height = 11.dp)
                                .shimmerPlaceholder(brush, RoundedCornerShape(4.dp))
                        )

                        // 2 action buttons placeholder (Share + Favorite)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .shimmerPlaceholder(brush, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .shimmerPlaceholder(brush, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shimmer skeleton list for the Recent Feed screen while articles are being fetched.
 */
@Composable
fun RecentFeedSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    val brush = rememberShimmerBrush()
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("recent_feed_skeleton"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(itemCount) { index ->
            ArticleFeedCardSkeleton(
                heroLayout = index == 0,
                brush = brush
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Single Category Grid item skeleton tile.
 */
@Composable
fun CategoryGridItemSkeleton(
    brush: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_grid_item_skeleton"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tile container skeleton
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush),
                contentAlignment = Alignment.Center
            ) {
                // Inner layered circle badge placeholder
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category label skeleton
        Box(
            modifier = Modifier
                .size(width = 68.dp, height = 13.dp)
                .shimmerPlaceholder(brush, RoundedCornerShape(4.dp))
        )
    }
}

/**
 * Shimmer skeleton grid for the Category "Browse Topics" screen while categories are being fetched.
 */
@Composable
fun CategoryGridSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 6
) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("category_grid_skeleton")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title placeholder
        Box(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .size(width = 120.dp, height = 20.dp)
                .shimmerPlaceholder(brush, RoundedCornerShape(6.dp))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(itemCount) {
                CategoryGridItemSkeleton(brush = brush)
            }
        }
    }
}

/**
 * Shimmer skeleton list for a filtered category screen while category posts are being fetched.
 */
@Composable
fun CategoryFilteredSkeleton(
    categoryName: String,
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("category_filtered_skeleton")
    ) {
        // Top header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 10.dp)
                            .shimmerPlaceholder(brush, RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(itemCount) {
                ArticleFeedCardSkeleton(
                    heroLayout = false,
                    brush = brush
                )
            }
        }
    }
}
