package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.example.R

@Composable
fun OpalTopHeader(
    title: String,
    searchQuery: String,
    isSearchActive: Boolean,
    searchHistory: List<String>,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onSubmitSearch: (String) -> Unit,
    onClearSearchAndReturnHome: () -> Unit,
    onSelectHistoryItem: (String) -> Unit,
    onRemoveHistoryItem: (String) -> Unit,
    onClearSearchHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    isInitialLoading: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // Infinite transition for initial loading bar animation (Screenshot 3 inspiration)
    val infiniteTransition = rememberInfiniteTransition(label = "top_load_bar_anim")
    val loadProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "load_bar_progress"
    )

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                // Focus request safeguard
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rounded Pill Search/Load Bar with Dropdown Anchor
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                if (isInitialLoading)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                onSearchActiveChange(true)
                            }
                            .padding(horizontal = 16.dp)
                            .testTag("search_pill_bar"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Stable leading search icon
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isInitialLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable { onSearchActiveChange(true) }
                                    .testTag("search_icon")
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            if (isInitialLoading) {
                                // Animated Search/Load State on initial app launch (Inspired by Screenshot 3)
                                Text(
                                    text = "Searching latest tech news...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("initial_search_load_text")
                                )
                            } else {
                                // Live, persistent Search Input Field with low opacity "Opal Tech" placeholder
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Opal Tech",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                                fontSize = 15.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { newQuery ->
                                            onSearchQueryChange(newQuery)
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(
                                            onSearch = {
                                                if (searchQuery.isNotBlank()) {
                                                    onSubmitSearch(searchQuery)
                                                }
                                                focusManager.clearFocus()
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { focusState ->
                                                if (focusState.isFocused) {
                                                    onSearchActiveChange(true)
                                                }
                                            }
                                            .focusRequester(focusRequester)
                                            .testTag("search_input")
                                    )
                                }

                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            onSearchQueryChange("")
                                        },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("clear_search_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Brand Badge on the right of the pill - clicking opens settings (General, Privacy, etc.)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        focusManager.clearFocus()
                                        onSearchActiveChange(false)
                                        onOpenSettings()
                                    }
                                    .testTag("brand_badge_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.opal_tech_icon_1791412564103),
                                    contentDescription = "Opal Tech",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        // Embedded animated gradient load bar inside bottom of search pill during initial load
                        if (isInitialLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .align(Alignment.BottomCenter)
                                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                                    .testTag("initial_search_progress_bar")
                            ) {
                                val loadBarBrush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    ),
                                    startX = loadProgress * 700f - 350f,
                                    endX = loadProgress * 700f + 350f
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(loadBarBrush)
                                )
                            }
                        }
                    }

                    // Dropdown Menu displaying Search History when search bar is tapped or interacted with
                    DropdownMenu(
                        expanded = isSearchActive && searchHistory.isNotEmpty(),
                        onDismissRequest = {
                            onSearchActiveChange(false)
                        },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier
                            .widthIn(min = 280.dp, max = 360.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                RoundedCornerShape(14.dp)
                            )
                            .testTag("search_history_dropdown")
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            if (searchHistory.isNotEmpty()) {
                                Text(
                                    text = "Clear all",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier
                                        .clickable { onClearSearchHistory() }
                                        .padding(start = 8.dp)
                                        .testTag("clear_all_history_btn")
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        if (searchHistory.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recent searches yet",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            val visibleItems = if (searchQuery.isNotBlank()) {
                                searchHistory.filter { it.contains(searchQuery, ignoreCase = true) }.take(6)
                            } else {
                                searchHistory.take(7)
                            }

                            if (visibleItems.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "No matching history for \"$searchQuery\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                visibleItems.forEach { queryItem ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = queryItem,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 14.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.History,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { onRemoveHistoryItem(queryItem) },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .testTag("remove_history_${queryItem}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove search",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            onSelectHistoryItem(queryItem)
                                            focusManager.clearFocus()
                                        },
                                        modifier = Modifier.testTag("history_item_${queryItem}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sleek accent load bar below header on initial opening
            if (isInitialLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary,
                                    Color.Transparent
                                ),
                                startX = loadProgress * 800f - 400f,
                                endX = loadProgress * 800f + 400f
                            )
                        )
                )
            }
        }
    }
}
