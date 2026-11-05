package com.example.ads

/**
 * Google AdMob Configuration.
 * Configured with real production AdMob IDs.
 */
object AdConfig {
    /**
     * Real Google Mobile Ads SDK App ID and Ad Unit IDs
     */
    const val APP_ID = "ca-app-pub-4170476681702895~1697066259"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-4170476681702895/1042460139"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-4170476681702895/7695498394"
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-4170476681702895/2520686627"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-4170476681702895/7695498394"

    // Production alias mappings
    const val PROD_APP_ID = APP_ID
    const val PROD_BANNER_AD_UNIT_ID = BANNER_AD_UNIT_ID
    const val PROD_INTERSTITIAL_AD_UNIT_ID = INTERSTITIAL_AD_UNIT_ID
    const val PROD_NATIVE_AD_UNIT_ID = NATIVE_AD_UNIT_ID
    const val PROD_REWARDED_AD_UNIT_ID = REWARDED_AD_UNIT_ID

    /**
     * Active configuration flag: strictly false so real production IDs are active
     */
    const val USE_TEST_ADS = false
}
