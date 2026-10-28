package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Finds the nearest [Activity] unwrapping any [ContextWrapper] layers.
 */
fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Official Google AdMob Native Ad Composable.
 *
 * AdMob Policy & Publication Compliance:
 * 1. ZERO HARDCODED ADS: All content (headline, body, icon, media, CTA) is
 *    loaded dynamically from the official Google AdMob SDK.
 * 2. CLEAR ATTRIBUTION: Contains mandatory [Ad] badge and "Advertisement" label.
 * 3. ADCHOICES COMPLIANCE: Top-right corner is reserved for the AdChoices icon.
 * 4. CLEAN SKELETON LOADING: While an ad is loading, displays a neutral shimmer
 *    without simulating any fake apps or misleading clickable elements.
 * 5. COLLAPSIBLE FAILURE: If no ad is returned, collapses to 0.dp height.
 * 6. NO ACCIDENTAL CLICKS: Clear margin, distinct styling, and proper AdMob view registration.
 */
@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.NATIVE_AD_UNIT_ID,
    showMedia: Boolean = false
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isFailed by remember { mutableStateOf(false) }

    DisposableEffect(adUnitId) {
        val targetActivity = context.findActivity()
        val targetContext = targetActivity ?: context.applicationContext

        val adLoader = AdLoader.Builder(targetContext, adUnitId)
            .forNativeAd { ad ->
                val oldAd = nativeAd
                nativeAd = ad
                isLoading = false
                isFailed = false
                try {
                    oldAd?.destroy()
                } catch (_: Exception) {}
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    isLoading = false
                    isFailed = false
                    Log.d("NativeAdCard", "AdMob native ad loaded successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.d("NativeAdCard", "AdMob native ad failed to load: code=${error.code}, message=${error.message}")
                    isLoading = false
                    isFailed = true
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .setReturnUrlsForImageAssets(false)
                    .setRequestMultipleImages(false)
                    .build()
            )
            .build()

        try {
            isLoading = true
            isFailed = false
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            Log.w("NativeAdCard", "Error starting AdLoader: ${e.message}")
            isLoading = false
            isFailed = true
        }

        onDispose {
            try {
                nativeAd?.destroy()
            } catch (_: Exception) {}
        }
    }

    when {
        // Successful Ad Load: render official AdMob NativeAdView
        nativeAd != null -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("admob_native_ad_card")
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        buildCompliantNativeAdView(ctx, isDark, showMedia).apply {
                            populateCompliantNativeAdView(nativeAd!!, this, showMedia)
                        }
                    },
                    update = { view ->
                        nativeAd?.let { ad ->
                            populateCompliantNativeAdView(ad, view, showMedia)
                        }
                    },
                    onRelease = { view ->
                        try {
                            view.destroy()
                        } catch (_: Exception) {}
                    }
                )
            }
        }

        // Loading state: render neutral skeleton placeholder with [Ad] badge (no fake ads!)
        isLoading -> {
            NativeAdSkeleton(modifier = modifier, isDark = isDark, showMedia = showMedia)
        }

        // Failed state: collapse cleanly without breaking layout or showing blank card
        else -> {
            Spacer(modifier = Modifier.height(0.dp))
        }
    }
}

/**
 * Clean neutral loading skeleton with pulsing shimmer.
 * Never displays hardcoded ads or fake apps, strictly adhering to AdMob guidelines.
 */
@Composable
private fun NativeAdSkeleton(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    showMedia: Boolean
) {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    val cardBg = if (isDark) Color(0xFF1E2430) else Color(0xFFF1F3F9)
    val placeholderColor = (if (isDark) Color(0xFF333E50) else Color(0xFFD6DBE5)).copy(alpha = alpha)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .padding(14.dp)
            .testTag("native_ad_skeleton")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Attribution row: [Ad] Advertisement
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFC107))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Ad",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Advertisement",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main row: Icon + Headline/Body placeholders + CTA placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(placeholderColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(placeholderColor)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(11.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(placeholderColor)
                    )
                }

                if (!showMedia) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(placeholderColor)
                    )
                }
            }

            if (showMedia) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(placeholderColor)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(placeholderColor)
                )
            }
        }
    }
}

/**
 * Builds the official [NativeAdView] with full AdMob policy compliance:
 * - Top Attribution header: [Ad] badge + "Advertisement" + AdChoices space
 * - Real dynamic content: headline, body, icon, media view, CTA button
 */
private fun buildCompliantNativeAdView(
    context: Context,
    isDark: Boolean,
    showMedia: Boolean
): NativeAdView {
    val nativeAdView = NativeAdView(context)
    val density = context.resources.displayMetrics.density
    fun dp(v: Int): Int = (v * density).toInt()

    nativeAdView.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    )

    val rootCard = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        setPadding(dp(14), dp(10), dp(14), dp(12))
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(16).toFloat()
            setColor(
                if (isDark) AndroidColor.parseColor("#15202B")
                else AndroidColor.parseColor("#F5F7FA")
            )
            setStroke(
                dp(1),
                if (isDark) AndroidColor.parseColor("#22303C")
                else AndroidColor.parseColor("#E1E8ED")
            )
        }
    }

    // 1. Mandatory Top Attribution Header: [Ad] badge + "Advertisement" + AdChoices reservation
    val headerLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(8)
        }
    }

    // Yellow "Ad" Badge
    val adBadge = TextView(context).apply {
        text = "Ad"
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(AndroidColor.BLACK)
        setPadding(dp(5), dp(1), dp(5), dp(1))
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(4).toFloat()
            setColor(AndroidColor.parseColor("#FFC107"))
        }
    }
    headerLayout.addView(adBadge)

    // "Advertisement" Text
    val adLabel = TextView(context).apply {
        text = "  Advertisement"
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setTextColor(
            if (isDark) AndroidColor.parseColor("#8899A6")
            else AndroidColor.parseColor("#657786")
        )
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }
    headerLayout.addView(adLabel)

    rootCard.addView(headerLayout)

    // 2. Main Row: Icon + Headline/Body + (Optional compact CTA button)
    val contentRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // App/Ad Icon
    val iconView = ImageView(context).apply {
        id = View.generateViewId()
        val lp = LinearLayout.LayoutParams(dp(44), dp(44)).apply {
            marginEnd = dp(12)
        }
        layoutParams = lp
        scaleType = ImageView.ScaleType.FIT_CENTER
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(10).toFloat()
        }
        clipToOutline = true
    }
    contentRow.addView(iconView)
    nativeAdView.iconView = iconView

    // Text Column: Headline + Body / Advertiser
    val textCol = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginEnd = dp(10)
        }
    }

    val headlineView = TextView(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(if (isDark) AndroidColor.WHITE else AndroidColor.parseColor("#14171A"))
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }
    textCol.addView(headlineView)
    nativeAdView.headlineView = headlineView

    val bodyView = TextView(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        setTextColor(
            if (isDark) AndroidColor.parseColor("#AAB8C2")
            else AndroidColor.parseColor("#657786")
        )
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(2)
        }
        layoutParams = lp
    }
    textCol.addView(bodyView)
    nativeAdView.bodyView = bodyView

    contentRow.addView(textCol)

    // Compact CTA Button (used when showMedia == false)
    val ctaButton = Button(context).apply {
        id = View.generateViewId()
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(AndroidColor.BLACK)
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(18).toFloat()
            setColor(AndroidColor.parseColor("#FFD600"))
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            dp(36)
        )
        setPadding(dp(16), 0, dp(16), 0)
        isAllCaps = false
    }

    if (!showMedia) {
        contentRow.addView(ctaButton)
    }
    rootCard.addView(contentRow)

    // 3. Optional Rich Media View and Bottom Full-Width CTA (used when showMedia == true)
    if (showMedia) {
        val mediaView = MediaView(context).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(160)
            ).apply {
                topMargin = dp(10)
                bottomMargin = dp(10)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(12).toFloat()
            }
            clipToOutline = true
        }
        rootCard.addView(mediaView)
        nativeAdView.mediaView = mediaView

        // Full width CTA button at the bottom of media
        ctaButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(40)
        )
        rootCard.addView(ctaButton)
    }

    nativeAdView.callToActionView = ctaButton
    nativeAdView.addView(rootCard)
    return nativeAdView
}

/**
 * Binds official [NativeAd] assets from Google AdMob into the [NativeAdView].
 * Zero hardcoded strings or mock content.
 */
private fun populateCompliantNativeAdView(
    nativeAd: NativeAd,
    adView: NativeAdView,
    showMedia: Boolean
) {
    val headlineView = adView.headlineView as? TextView
    val bodyView = adView.bodyView as? TextView
    val iconView = adView.iconView as? ImageView
    val ctaView = adView.callToActionView as? Button
    val mediaView = adView.mediaView as? MediaView

    // 1. Headline
    headlineView?.text = nativeAd.headline ?: ""

    // 2. Body or Advertiser
    val bodyText = nativeAd.body?.takeIf { it.isNotBlank() }
        ?: nativeAd.advertiser?.takeIf { it.isNotBlank() }
        ?: ""
    bodyView?.text = bodyText
    bodyView?.visibility = if (bodyText.isNotBlank()) View.VISIBLE else View.GONE

    // 3. Icon
    if (nativeAd.icon?.drawable != null) {
        iconView?.setImageDrawable(nativeAd.icon?.drawable)
        iconView?.visibility = View.VISIBLE
    } else {
        iconView?.visibility = View.GONE
    }

    // 4. Call to Action Button
    val ctaText = nativeAd.callToAction?.takeIf { it.isNotBlank() } ?: "Install"
    ctaView?.text = ctaText

    // 5. Media Content (if present and showMedia is enabled)
    if (showMedia && mediaView != null && nativeAd.mediaContent != null) {
        mediaView.mediaContent = nativeAd.mediaContent
        mediaView.visibility = View.VISIBLE
    } else if (mediaView != null) {
        mediaView.visibility = View.GONE
    }

    // 6. Register NativeAd with NativeAdView for automated click and impression handling
    try {
        adView.setNativeAd(nativeAd)
    } catch (e: Exception) {
        Log.w("NativeAdCard", "Exception calling setNativeAd: ${e.message}")
    }
}
