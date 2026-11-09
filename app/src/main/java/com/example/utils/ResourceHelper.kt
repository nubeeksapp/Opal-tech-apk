package com.example.utils

import android.content.Context
import com.example.R

object ResourceHelper {
    /**
     * Resolves an article thumbnail drawable ID safely.
     * Guaranteed to return a valid 16:9 content thumbnail and NEVER the app logo.
     */
    fun getDrawableId(context: Context, resName: String?): Int {
        if (!resName.isNullOrBlank()) {
            val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (id != 0 && id != R.drawable.opal_tech_icon_1791412564103) {
                return id
            }
        }
        return R.drawable.thumb_vpn_1791412717985
    }

    /**
     * Resolves the most contextual 16:9 thumbnail based on category or content keyword.
     */
    fun getArticleThumbnail(context: Context, resName: String?, categoryId: String = "", title: String = ""): Int {
        if (!resName.isNullOrBlank()) {
            val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (id != 0 && id != R.drawable.opal_tech_icon_1791412564103) {
                return id
            }
        }
        val lower = (categoryId + " " + title).lowercase()
        return when {
            lower.contains("wifi") || lower.contains("esim") || lower.contains("connect") || lower.contains("network") ->
                R.drawable.thumb_esim_1791412579016
            lower.contains("ai") || lower.contains("gemini") || lower.contains("call") || lower.contains("voice") ->
                R.drawable.thumb_calling_1791412590087
            lower.contains("job") || lower.contains("freelanc") || lower.contains("earn") || lower.contains("wallet") ->
                R.drawable.thumb_wallet_1791412616285
            lower.contains("scan") || lower.contains("clean") || lower.contains("storage") || lower.contains("pdf") ->
                R.drawable.thumb_scanner_1791412603303
            lower.contains("review") || lower.contains("trade") || lower.contains("map") || lower.contains("app") ->
                R.drawable.thumb_map_1791412629351
            else ->
                R.drawable.thumb_vpn_1791412717985
        }
    }
}
