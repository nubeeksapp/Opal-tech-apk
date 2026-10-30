package com.example.utils

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle
import android.widget.Toast

object IntentUtils {

    fun openPlayStore(context: Context, packageName: String, webUrl: String) {
        val extractedPackage = if (packageName.isNotBlank()) {
            packageName.trim()
        } else if (webUrl.contains("id=")) {
            webUrl.substringAfter("id=").substringBefore("&").trim()
        } else {
            ""
        }

        if (extractedPackage.isNotBlank()) {
            val marketUri = Uri.parse("market://details?id=$extractedPackage")
            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            try {
                context.startActivity(marketIntent)
                return
            } catch (_: ActivityNotFoundException) {
                // Fallback to web link below
            }
        }

        if (webUrl.isNotBlank()) {
            val targetUrl = if (webUrl.startsWith("http://") || webUrl.startsWith("https://")) {
                webUrl.trim()
            } else {
                "https://${webUrl.trim()}"
            }
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(webIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Unable to open Google Play Store", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Play Store link not available", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareArticle(context: Context, title: String, summary: String, playUrl: String) {
        val shareText = buildString {
            appendLine("Check out this tech recommendation from Opal Tech:")
            appendLine()
            appendLine(title)
            appendLine()
            appendLine(summary)
            if (playUrl.isNotBlank()) {
                appendLine()
                appendLine("Download original app: $playUrl")
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, title)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share with friends")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(shareIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "No app available to share", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareApp(context: Context) {
        val shareText = "Download Opal Tech for the best daily tech tips, app recommendations, and digital tools! https://play.google.com/store/apps/details?id=${context.packageName}"
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Opal Tech")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(shareIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "No app available to share", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAdaptiveLink(context: Context, linkUrl: String, packageName: String = "") {
        val trimmed = linkUrl.trim()
        if (trimmed.isEmpty()) return

        val isPlayStore = trimmed.contains("play.google.com", ignoreCase = true) ||
            trimmed.startsWith("market://", ignoreCase = true)

        if (isPlayStore) {
            openPlayStore(context, packageName, trimmed)
        } else {
            openWebUrl(context, trimmed)
        }
    }

    fun openWebUrl(context: Context, url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return
        val targetUrl = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open link in browser", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Prompt"): Boolean {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) {
            Toast.makeText(context, "Cannot copy: Content is empty", Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            val clipboard = (context.getSystemService(Context.CLIPBOARD_SERVICE)
                ?: context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE)) as? ClipboardManager

            if (clipboard == null) {
                Toast.makeText(context, "Clipboard service unavailable", Toast.LENGTH_SHORT).show()
                return false
            }

            val clipLabel = label.ifBlank { "Prompt" }
            val clip = ClipData.newPlainText(clipLabel, cleanText)
            clipboard.setPrimaryClip(clip)

            // Verify clipboard write while adhering to foreground platform restrictions
            var isVerified = false
            try {
                val currentClip = clipboard.primaryClip
                if (currentClip != null && currentClip.itemCount > 0) {
                    val readBack = currentClip.getItemAt(0)?.text?.toString()
                    if (readBack == cleanText) {
                        isVerified = true
                    }
                }
            } catch (_: SecurityException) {
                isVerified = true
            } catch (_: Exception) {
                isVerified = true
            }

            val feedbackText = if (clipLabel.startsWith("Part", ignoreCase = true)) {
                "Copied $clipLabel to clipboard"
            } else if (clipLabel.equals("Prompt", ignoreCase = true) || clipLabel.equals("Complete Prompt", ignoreCase = true)) {
                "Prompt copied to clipboard"
            } else if (clipLabel.isNotBlank() && clipLabel != "Text") {
                "Copied $clipLabel to clipboard"
            } else {
                "Copied to clipboard"
            }
            Toast.makeText(context, feedbackText, Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Copy failed: ${e.localizedMessage ?: e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
