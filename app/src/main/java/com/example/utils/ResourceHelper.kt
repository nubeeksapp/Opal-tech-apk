package com.example.utils

import android.content.Context
import com.example.R

object ResourceHelper {
    fun getDrawableId(context: Context, resName: String): Int {
        val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
        return if (id != 0) id else R.drawable.opal_tech_icon_1791412564103
    }
}
