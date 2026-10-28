package com.example.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainTab

@Composable
fun OpalBottomNav(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 6.dp,
        modifier = modifier
            .navigationBarsPadding()
            .testTag("opal_bottom_navigation")
    ) {
        // Tab 1: Recent
        val isRecentSelected = selectedTab == MainTab.RECENT
        NavigationBarItem(
            selected = isRecentSelected,
            onClick = { onTabSelected(MainTab.RECENT) },
            icon = {
                Icon(
                    imageVector = if (isRecentSelected) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = "Recent"
                )
            },
            label = {
                Text(
                    text = "Recent",
                    fontSize = 12.sp,
                    fontWeight = if (isRecentSelected) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("tab_recent")
        )

        // Tab 2: Category
        val isCategorySelected = selectedTab == MainTab.CATEGORY
        NavigationBarItem(
            selected = isCategorySelected,
            onClick = { onTabSelected(MainTab.CATEGORY) },
            icon = {
                Icon(
                    imageVector = if (isCategorySelected) Icons.Default.GridView else Icons.Outlined.GridView,
                    contentDescription = "Category"
                )
            },
            label = {
                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    fontWeight = if (isCategorySelected) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("tab_category")
        )

        // Tab 3: Favorite
        val isFavoriteSelected = selectedTab == MainTab.FAVORITE
        NavigationBarItem(
            selected = isFavoriteSelected,
            onClick = { onTabSelected(MainTab.FAVORITE) },
            icon = {
                Icon(
                    imageVector = if (isFavoriteSelected) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite"
                )
            },
            label = {
                Text(
                    text = "Favorite",
                    fontSize = 12.sp,
                    fontWeight = if (isFavoriteSelected) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("tab_favorite")
        )
    }
}
