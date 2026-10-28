package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.BannerAdView
import com.example.ads.NativeAdCard
import com.example.data.model.AppCategory
import com.example.data.model.TechArticle
import com.example.ui.components.ArticleFeedCard
import com.example.ui.components.RecentFeedSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentFeedScreen(
    articles: List<TechArticle>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onArticleClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    categories: List<AppCategory> = emptyList(),
    selectedCategory: String? = null,
    onSelectCategory: (String?) -> Unit = {},
    searchQuery: String = "",
    onClearSearchAndReturnHome: () -> Unit = {}
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .testTag("recent_feed_screen")
    ) {
        Crossfade(
            targetState = isLoading && !isRefreshing,
            label = "feed_loading_crossfade"
        ) { loading ->
            if (loading) {
                RecentFeedSkeleton()
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Active Notification & Quick Back-to-Home Banner
                    if (searchQuery.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("search_status_banner"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Search results for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 13.5.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Back to Home",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.5.sp
                                ),
                                modifier = Modifier
                                    .clickable { onClearSearchAndReturnHome() }
                                    .testTag("search_banner_back_home_btn")
                            )
                        }
                    }

                    if (articles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty())
                                        "No articles found for \"$searchQuery\""
                                    else
                                        "No articles found",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty())
                                        "Remove your search term to see all latest news and stories."
                                    else
                                        "Pull down to refresh the feed or check back later.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                if (searchQuery.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = onClearSearchAndReturnHome,
                                        modifier = Modifier.testTag("empty_clear_search_btn")
                                    ) {
                                        Text("Remove search & Back to Home")
                                    }
                                }
                            }
                        }
                    } else {
                        // Article Feed: First article is Hero if on default home feed; all others are compact list cards
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(articles, key = { _, article -> article.id }) { index, article ->
                                val isHero = index == 0 && searchQuery.isEmpty() && selectedCategory == null
                                ArticleFeedCard(
                                    article = article,
                                    heroLayout = isHero,
                                    onClick = { onArticleClick(article.id) },
                                    onToggleFavorite = { onToggleFavorite(article.id) }
                                )
                                // Insert native ad naturally between articles (after 2nd article or every 5th)
                                if (index == 1 || (articles.size == 1 && index == 0) || (index > 1 && (index + 1) % 5 == 0)) {
                                    NativeAdCard(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
