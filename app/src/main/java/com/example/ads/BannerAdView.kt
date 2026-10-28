package com.example.ads

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Fixed size Banner Ad Composable using Google AdMob Banner Ad Unit:
 * "ca-app-pub-3940256099942544/6300978111"
 *
 * Publication & Policy Compliance:
 * - Docks cleanly at 50dp banner standard height.
 * - If ad fails to load (e.g. offline/no fill), collapses gracefully to 0dp height
 *   to avoid showing blank empty ad containers.
 * - Lifecycle cleanup: destroys AdView upon composition release.
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.BANNER_AD_UNIT_ID
) {
    val context = LocalContext.current
    val targetActivity = context.findActivity() ?: context

    var isAdLoaded by remember { mutableStateOf(false) }
    var isAdFailed by remember { mutableStateOf(false) }

    if (isAdFailed) {
        Spacer(modifier = Modifier.height(0.dp))
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(MaterialTheme.colorScheme.surface)
                .testTag("banner_ad_container"),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admob_banner_view"),
                factory = { ctx ->
                    AdView(targetActivity).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                Log.d("BannerAdView", "Banner ad loaded successfully.")
                                isAdLoaded = true
                                isAdFailed = false
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                Log.d("BannerAdView", "Banner ad failed to load: ${error.message}")
                                isAdLoaded = false
                                isAdFailed = true
                            }
                        }
                        try {
                            loadAd(AdRequest.Builder().build())
                        } catch (e: Exception) {
                            Log.w("BannerAdView", "Exception loading banner: ${e.message}")
                            isAdFailed = true
                        }
                    }
                },
                update = {},
                onRelease = { view ->
                    try {
                        view.destroy()
                    } catch (_: Exception) {}
                }
            )
        }
    }
}
