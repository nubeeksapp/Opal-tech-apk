package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.example.ads.AdManager
import com.example.ads.BannerAdView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.config.AppConfig
import com.example.data.model.SettingsSection
import com.example.data.model.TextSizePreference
import com.example.data.model.UserPreferences
import com.example.data.remote.SupabaseClient
import com.example.data.repository.ArticleRepository
import java.util.Locale

private enum class SettingsSubPage {
    NONE,
    PRIVACY_POLICY,
    PUBLISHER_INFO
}

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onBack: () -> Unit,
    onToggleSection: (SettingsSection) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectTextSize: (TextSizePreference) -> Unit,
    onTogglePushNotifications: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onClearSearchHistory: () -> Unit,
    modifier: Modifier = Modifier,
    isSupabaseConfigured: Boolean = SupabaseClient.isConfigured,
    syncStatus: ArticleRepository.SyncStatus = ArticleRepository.SyncStatus.NOT_CONFIGURED,
    onSendTestNotification: () -> Unit = {},
    fcmToken: String? = null
) {
    val context = LocalContext.current
    var currentSubPage by rememberSaveable { mutableStateOf(SettingsSubPage.NONE) }

    // Intercept back navigation when on a sub-page
    BackHandler(enabled = true) {
        if (currentSubPage != SettingsSubPage.NONE) {
            currentSubPage = SettingsSubPage.NONE
        } else {
            onBack()
        }
    }

    when (currentSubPage) {
        SettingsSubPage.PRIVACY_POLICY -> {
            PrivacyPolicyPage(
                onBack = { currentSubPage = SettingsSubPage.NONE },
                modifier = modifier
            )
        }
        SettingsSubPage.PUBLISHER_INFO -> {
            PublisherInfoPage(
                onBack = { currentSubPage = SettingsSubPage.NONE },
                modifier = modifier
            )
        }
        SettingsSubPage.NONE -> {
            SettingsMainLayout(
                preferences = preferences,
                onBack = onBack,
                onToggleDarkMode = onToggleDarkMode,
                onSelectTextSize = onSelectTextSize,
                onClearCache = onClearCache,
                onClearSearchHistory = onClearSearchHistory,
                onOpenPrivacyPolicy = { currentSubPage = SettingsSubPage.PRIVACY_POLICY },
                onOpenPublisherInfo = { currentSubPage = SettingsSubPage.PUBLISHER_INFO },
                isSupabaseConfigured = isSupabaseConfigured,
                syncStatus = syncStatus,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun SettingsMainLayout(
    preferences: UserPreferences,
    onBack: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectTextSize: (TextSizePreference) -> Unit,
    onClearCache: () -> Unit,
    onClearSearchHistory: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenPublisherInfo: () -> Unit,
    isSupabaseConfigured: Boolean,
    syncStatus: ArticleRepository.SyncStatus,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Several sections can stay open at once
    var openSections by rememberSaveable {
        mutableStateOf(setOf("General"))
    }
    var showTextSizeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val prefs = remember { context.getSharedPreferences("opal_tech_prefs", Context.MODE_PRIVATE) }
    var isAdminUnlocked by remember {
        mutableStateOf(prefs.getBoolean("admin_access_unlocked", false))
    }
    var versionTapCount by remember { mutableStateOf(0) }

    fun toggleSection(sectionKey: String) {
        openSections = if (openSections.contains(sectionKey)) {
            openSections - sectionKey
        } else {
            openSections + sectionKey
        }
    }

    val isGeneralOpen = openSections.contains("General")
    val isCacheOpen = openSections.contains("Cache")
    val isPrivacyOpen = openSections.contains("Privacy")
    val isAboutOpen = openSections.contains("About")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen")
    ) {
        // Top Bar: follows theme background and onSurface
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.5.dp,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Four expandable sections separated by thin gray dividers
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. GENERAL | subtitle "Theme, text size and notifications"
            ExpandableSectionHeader(
                title = "General",
                subtitle = "Theme, text size and notifications",
                isOpen = isGeneralOpen,
                onClick = { toggleSection("General") }
            )

            AnimatedVisibility(
                visible = isGeneralOpen,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Dark Mode | subtitle "Better eyesight and power saving" | Material 3 switch on the right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleDarkMode(!preferences.isDarkMode) }
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dark Mode",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Better eyesight and power saving",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = preferences.isDarkMode,
                            onCheckedChange = onToggleDarkMode,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                checkedBorderColor = Color.Transparent,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }

                    // Text size | subtitle "Preferred text size for content description"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTextSizeDialog = true }
                            .padding(horizontal = 4.dp)
                            .testTag("text_size_setting_row")
                    ) {
                        Text(
                            text = "Text size",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Preferred text size for content description",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Push Notification | subtitle "Manage push notification settings"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openAppNotificationSettings(context) }
                            .padding(horizontal = 4.dp)
                            .testTag("push_notification_setting_row")
                    ) {
                        Text(
                            text = "Push Notification",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Manage push notification settings",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            ThinGrayDivider()

            // 2. CACHE | subtitle "Clear caches, search history"
            ExpandableSectionHeader(
                title = "Cache",
                subtitle = "Clear caches, search history",
                isOpen = isCacheOpen,
                onClick = { toggleSection("Cache") }
            )

            AnimatedVisibility(
                visible = isCacheOpen,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    val cacheText = String.format(Locale.US, "%.1f", preferences.cacheSizeMB)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onClearCache()
                                Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 4.dp)
                            .testTag("clear_cache_row")
                    ) {
                        Text(
                            text = "Clear Cache",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Free up $cacheText MB of space",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Clear search history | no subtitle
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onClearSearchHistory()
                                Toast.makeText(context, "Search history cleared", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 4.dp)
                            .testTag("clear_search_history_row")
                    ) {
                        Text(
                            text = "Clear search history",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            ThinGrayDivider()

            // 3. PRIVACY | subtitle "Privacy policy, publisher info"
            ExpandableSectionHeader(
                title = "Privacy",
                subtitle = "Privacy policy, publisher info",
                isOpen = isPrivacyOpen,
                onClick = { toggleSection("Privacy") }
            )

            AnimatedVisibility(
                visible = isPrivacyOpen,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Privacy policy | opens in-app page
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPrivacyPolicy() }
                            .padding(horizontal = 4.dp)
                            .testTag("privacy_policy_row")
                    ) {
                        Text(
                            text = "Privacy policy",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Publisher info | opens in-app page
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPublisherInfo() }
                            .padding(horizontal = 4.dp)
                            .testTag("publisher_info_row")
                    ) {
                        Text(
                            text = "Publisher info",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            ThinGrayDivider()

            // 4. ABOUT | subtitle "Build version, rate, share"
            ExpandableSectionHeader(
                title = "About",
                subtitle = "Build version, rate, share",
                isOpen = isAboutOpen,
                onClick = { toggleSection("About") }
            )

            AnimatedVisibility(
                visible = isAboutOpen,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // About | opens centered Material 3 dialog
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAboutDialog = true }
                            .padding(horizontal = 4.dp)
                            .testTag("about_dialog_row")
                    ) {
                        Text(
                            text = "About",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Rate us | opens Google Play listing
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openRateUs(context) }
                            .padding(horizontal = 4.dp)
                            .testTag("rate_us_row")
                    ) {
                        Text(
                            text = "Rate us",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Share to friends | opens system share sheet
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { shareToFriends(context) }
                            .padding(horizontal = 4.dp)
                            .testTag("share_friends_row")
                    ) {
                        Text(
                            text = "Share to friends",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // More apps
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openMoreApps(context) }
                            .padding(horizontal = 4.dp)
                            .testTag("more_apps_row")
                    ) {
                        Text(
                            text = "More apps",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Admin Panel & Cloud Sync Status (Hidden from regular users, available to authorized administrators)
                    if (isAdminUnlocked) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .testTag("admin_sync_status_row")
                        ) {
                            Text(
                                text = "Admin Panel Sync",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val statusLabel = when (syncStatus) {
                                ArticleRepository.SyncStatus.SYNCED_LIVE -> "Connected: Live Sync Active"
                                ArticleRepository.SyncStatus.PENDING_TABLES -> "Connected: Tables ready in Supabase"
                                ArticleRepository.SyncStatus.OFFLINE_FALLBACK -> if (isSupabaseConfigured) "Configured (Offline cache active)" else "Local offline mode"
                                ArticleRepository.SyncStatus.NOT_CONFIGURED -> "Local mode (Ready for Supabase credentials)"
                            }
                            Text(
                                text = statusLabel,
                                fontSize = 14.sp,
                                color = if (syncStatus == ArticleRepository.SyncStatus.SYNCED_LIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Text Size Dialog
    if (showTextSizeDialog) {
        AlertDialog(
            onDismissRequest = { showTextSizeDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Text size",
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    TextSizePreference.entries.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectTextSize(option)
                                    showTextSizeDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = preferences.textSize == option,
                                onClick = {
                                    onSelectTextSize(option)
                                    showTextSizeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option.displayName,
                                fontSize = 16.sp * option.scale,
                                fontWeight = if (preferences.textSize == option) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTextSizeDialog = false }) {
                    Text("Close", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Centered Material 3 About Dialog
    if (showAboutDialog) {
        Dialog(
            onDismissRequest = { showAboutDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(horizontal = 16.dp)
                    .testTag("about_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App icon squircle
                    Image(
                        painter = painterResource(id = R.drawable.opal_tech_icon_1791412564103),
                        contentDescription = AppConfig.APP_NAME,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // App Title
                    Text(
                        text = AppConfig.APP_NAME,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Version (tap 5 times for administrator options)
                    Text(
                        text = AppConfig.APP_VERSION,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable {
                            versionTapCount++
                            if (versionTapCount >= 5) {
                                versionTapCount = 0
                                isAdminUnlocked = !isAdminUnlocked
                                prefs.edit().putBoolean("admin_access_unlocked", isAdminUnlocked).apply()
                                Toast.makeText(
                                    context,
                                    if (isAdminUnlocked) "Admin Panel unlocked" else "Admin Panel hidden",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Copyright lines
                    Text(
                        text = "Copyright © 2026 ${AppConfig.PUBLISHER_NAME}.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "All right reserved",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // "OK" text button at bottom right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showAboutDialog = false },
                            modifier = Modifier.testTag("about_ok_btn")
                        ) {
                            Text(
                                text = "OK",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableSectionHeader(
    title: String,
    subtitle: String,
    isOpen: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = if (isOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isOpen) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ThinGrayDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp
    )
}

/**
 * Privacy policy opens a new in-app page with a back arrow and the title "Privacy Policy".
 * It shows placeholder text "Privacy policy will be added here."
 * The text comes from AppConfig.PRIVACY_POLICY_TEXT.
 */
@Composable
private fun PrivacyPolicyPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("privacy_policy_page")
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.5.dp,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Privacy Policy",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = AppConfig.PRIVACY_POLICY_TEXT,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Publisher info opens a new in-app page with a back arrow and the title "Publisher info".
 * It shows one column with three rows from top to bottom:
 * Publisher name, Email, Phone number.
 * Each row has a small label and the value under it in 16sp.
 * Uses values from AppConfig.
 */
@Composable
private fun PublisherInfoPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("publisher_info_page")
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.5.dp,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Publisher info",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Row 1: Publisher name
            Column {
                Text(
                    text = "Publisher name",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppConfig.PUBLISHER_NAME,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Row 2: Email
            Column {
                Text(
                    text = "Email",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppConfig.PUBLISHER_EMAIL,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Row 3: Phone number
            Column {
                Text(
                    text = "Phone number",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppConfig.PUBLISHER_PHONE,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Tapping Push Notification opens the phone's system notification settings for this app,
 * falling back to app details settings if needed, or toast.
 */
private fun openAppNotificationSettings(context: Context) {
    val packageName = context.packageName
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            putExtra("android.provider.extra.APP_PACKAGE", packageName)
            putExtra("app_package", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    } else {
        Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
            putExtra("app_package", packageName)
            putExtra("app_uid", context.applicationInfo.uid)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Open Settings > Notifications on your phone to manage notifications.", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Rate us opens this app's Google Play listing using market:// URI with web fallback.
 */
private fun openRateUs(context: Context) {
    val pkg = AppConfig.PLAY_STORE_PACKAGE_ID
    val marketUri = Uri.parse("market://details?id=$pkg")
    val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
    try {
        val intent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "Could not open Google Play Store", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Share to friends opens the system share sheet with text and Play Store link.
 * If unavailable, copies the link and shows the toast "Link copied".
 */
private fun shareToFriends(context: Context) {
    val pkg = AppConfig.PLAY_STORE_PACKAGE_ID
    val playStoreUrl = "https://play.google.com/store/apps/details?id=$pkg"
    val shareText = "Check out Opal Tech for daily tech tips and news\n$playStoreUrl"
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, AppConfig.APP_NAME)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    try {
        val chooser = Intent.createChooser(shareIntent, "Share Opal Tech").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Opal Tech Link", playStoreUrl)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
        } catch (e2: Exception) {
            Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * More apps keeps current behavior.
 */
private fun openMoreApps(context: Context) {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/search?q=pub%3AOpal+Tech&c=apps")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open Google Play Store", Toast.LENGTH_SHORT).show()
    }
}
