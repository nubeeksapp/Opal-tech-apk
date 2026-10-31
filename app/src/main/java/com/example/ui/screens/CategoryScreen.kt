package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppCategory
import com.example.data.model.TechArticle
import com.example.ui.components.ArticleFeedCard
import com.example.ui.components.CategoryFilteredSkeleton
import com.example.ui.components.CategoryGridSkeleton

@Composable
fun CategoryScreen(
    categories: List<AppCategory>,
    selectedCategory: String?,
    categoryArticles: List<TechArticle>,
    onSelectCategory: (String) -> Unit,
    onBackToCategories: () -> Unit,
    onArticleClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLoadingCategories: Boolean = false,
    isLoadingCategoryArticles: Boolean = false
) {
    AnimatedContent(
        targetState = selectedCategory,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "category_transition"
    ) { categoryName ->
        if (categoryName == null) {
            if (isLoadingCategories) {
                CategoryGridSkeleton(modifier = modifier)
            } else {
                // Category Grid View (as in Screenshot 8)
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("category_grid_screen")
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Browse Topics",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(categories, key = { it.id }) { cat ->
                            CategoryGridItem(
                                category = cat,
                                onClick = { onSelectCategory(cat.name) }
                            )
                        }
                    }
                }
            }
        } else {
            if (isLoadingCategoryArticles) {
                CategoryFilteredSkeleton(
                    categoryName = categoryName,
                    modifier = modifier
                )
            } else {
                // Filtered Category List View
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .testTag("category_filtered_list")
                ) {
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
                            IconButton(
                                onClick = onBackToCategories,
                                modifier = Modifier.testTag("back_to_categories_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to categories",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column {
                                Text(
                                    text = categoryName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${categoryArticles.size} articles",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (categoryArticles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No articles found in this category.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(categoryArticles, key = { it.id }) { article ->
                                ArticleFeedCard(
                                    article = article,
                                    heroLayout = false,
                                    onClick = { onArticleClick(article.id) },
                                    onToggleFavorite = { onToggleFavorite(article.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGridItem(
    category: AppCategory,
    onClick: () -> Unit
) {
    val logoRes = when {
        category.id.contains("opal", ignoreCase = true) || category.name.contains("opal", ignoreCase = true) ->
            R.drawable.cat_icon_opal_tech
        category.id.contains("tips", ignoreCase = true) || category.name.contains("tips", ignoreCase = true) ->
            R.drawable.cat_icon_tech_tips
        category.id.contains("job", ignoreCase = true) || category.name.contains("job", ignoreCase = true) ->
            R.drawable.cat_icon_online_jobs
        category.id.contains("review", ignoreCase = true) || category.name.contains("review", ignoreCase = true) ->
            R.drawable.cat_icon_app_review
        else -> R.drawable.cat_icon_opal_tech
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("category_item_${category.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = category.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Category Label below icon
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}
