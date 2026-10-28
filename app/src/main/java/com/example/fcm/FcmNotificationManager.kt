@file:Suppress("DEPRECATION")

package com.example.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FcmNotificationManager {
    private const val TAG = "FcmNotificationManager"

    const val CHANNEL_ID = "daily_tech_tips_channel"
    const val CHANNEL_NAME = "Top Daily Tech Tips"
    const val CHANNEL_DESCRIPTION = "Daily curated tech insights, app recommendations, and digital guides"
    const val TOPIC_DAILY_TIPS = "daily_tech_tips"

    const val EXTRA_ARTICLE_ID = "extra_article_id"
    const val EXTRA_TIP_TITLE = "extra_tip_title"

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    private val _isSubscribed = MutableStateFlow(true)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    // Sample collection of daily tech tips based on Opal Tech guides
    private val sampleDailyTips = listOf(
        DailyTip(
            id = "supabase_app_1",
            title = "💡 eSIM vs Local SIM: Save 70% Abroad",
            body = "Never pay exorbitant roaming fees again. Discover the best international eSIM setup for seamless travel connectivity."
        ),
        DailyTip(
            id = "supabase_app_2",
            title = "⚡ Make Unlimited Calls Over WiFi for Free",
            body = "Top hidden apps that let you call landlines and mobiles worldwide without carrier charges."
        ),
        DailyTip(
            id = "supabase_app_3",
            title = "🔍 Scan & Identify Any Object Instantly",
            body = "Turn your phone camera into a powerful AI scanner for ingredients, documents, and real-world tools."
        ),
        DailyTip(
            id = "supabase_app_4",
            title = "📸 Passport Photo at Home in 2 Minutes",
            body = "Official biometric guidelines and free background removal tricks straight from your smartphone."
        ),
        DailyTip(
            id = "supabase_app_5",
            title = "🗺️ Complete 3D Offline Navigation",
            body = "Download offline topographical maps and avoid getting stranded with no cellular signal."
        )
    )

    data class DailyTip(
        val id: String,
        val title: String,
        val body: String
    )

    /**
     * Initializes notification channels and FCM setup. Safe to call multiple times.
     */
    fun initialize(context: Context) {
        createNotificationChannel(context)
        ensureFirebaseApp(context)
        fetchTokenAndSubscribe(context, subscribe = true)
    }

    private fun ensureFirebaseApp(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(context.packageName)
                    .setApiKey("AIzaSyOpalTechFcmPlaceholderKey1234567")
                    .setProjectId("opal-tech-app")
                    .setGcmSenderId("895772387847")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "Initialized default FirebaseApp with fallback options")
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp initialization note: ${e.message}")
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun updateSubscription(context: Context, enabled: Boolean) {
        _isSubscribed.value = enabled
        fetchTokenAndSubscribe(context, subscribe = enabled)
    }

    private fun fetchTokenAndSubscribe(context: Context, subscribe: Boolean) {
        try {
            ensureFirebaseApp(context)
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val token = task.result
                    _fcmToken.value = token
                    Log.d(TAG, "FCM Registration Token: $token")
                } else {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                }
            }

            if (subscribe) {
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_DAILY_TIPS)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Subscribed successfully to FCM topic: $TOPIC_DAILY_TIPS")
                        }
                    }
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_DAILY_TIPS)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Unsubscribed from FCM topic: $TOPIC_DAILY_TIPS")
                        }
                    }
            }
        } catch (e: Exception) {
            Log.w(TAG, "FCM Topic subscription note: ${e.message}")
        }
    }

    /**
     * Posts a notification for Top Daily Tech Tips on the device.
     */
    fun showTechTipNotification(
        context: Context,
        title: String,
        body: String,
        articleId: String? = null
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!articleId.isNullOrBlank()) {
                putExtra(EXTRA_ARTICLE_ID, articleId)
            }
            putExtra(EXTRA_TIP_TITLE, title)
        }

        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            pendingIntentFlag
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.opal_tech_icon_1791412564103)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.opal_tech_icon_1791412564103,
                "Read Tip",
                pendingIntent
            )

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = (System.currentTimeMillis() % 100000).toInt()
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "Displayed Daily Tech Tip notification: $title")
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot post notification: missing POST_NOTIFICATIONS permission", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display notification", e)
        }
    }

    /**
     * Triggers a random Top Daily Tech Tip notification for on-device testing.
     */
    fun triggerSampleDailyTipNotification(context: Context): DailyTip {
        val tip = sampleDailyTips.random()
        showTechTipNotification(
            context = context,
            title = tip.title,
            body = tip.body,
            articleId = tip.id
        )
        return tip
    }

    fun onTokenRefresh(token: String) {
        _fcmToken.value = token
        Log.d(TAG, "New FCM Token received: $token")
    }
}
