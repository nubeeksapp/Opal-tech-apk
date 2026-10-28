package com.example.ads

/**
 * Google AdMob Configuration.
 *
 * Provides clean separation between official Google AdMob Test Ad Unit IDs
 * (used during development and testing to prevent policy violations)
 * and Production Ad Unit IDs (used when releasing to Google Play).
 */
object AdConfig {
    /**
     * Set to true to use official Google AdMob test ad units during development.
     * Set to false to switch to production ad unit IDs before release.
     */
    const val USE_TEST_ADS = true

    // =========================================================================
    // Official Google AdMob Test Ad Units (provided by Google and user)
    // =========================================================================
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    // =========================================================================
    // Production Ad Units (configure your verified AdMob account IDs here)
    // =========================================================================
    const val PROD_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val PROD_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    // =========================================================================
    // Active Ad Units dispatched to the Google Mobile Ads SDK
    // =========================================================================
    val APP_ID: String get() = if (USE_TEST_ADS) TEST_APP_ID else PROD_APP_ID
    val BANNER_AD_UNIT_ID: String get() = if (USE_TEST_ADS) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID
    val INTERSTITIAL_AD_UNIT_ID: String get() = if (USE_TEST_ADS) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID
    val REWARDED_AD_UNIT_ID: String get() = if (USE_TEST_ADS) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID
    val NATIVE_AD_UNIT_ID: String get() = if (USE_TEST_ADS) TEST_NATIVE_AD_UNIT_ID else PROD_NATIVE_AD_UNIT_ID
}
