@file:Suppress("DEPRECATION")

package com.example.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class OpalFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "OpalFcmService"
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "Refreshed FCM registration token: $token")
        FcmNotificationManager.onTokenRefresh(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Extract title and body from notification payload or data payload
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "💡 Top Daily Tech Tip"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "Check out today's recommended tech tip and productivity guide on Opal Tech!"

        val articleId = remoteMessage.data["article_id"]
            ?: remoteMessage.data["articleId"]
            ?: remoteMessage.data["id"]

        Log.d(TAG, "Message received - Title: $title, ArticleId: $articleId")

        // Display the system notification
        FcmNotificationManager.showTechTipNotification(
            context = applicationContext,
            title = title,
            body = body,
            articleId = articleId
        )
    }
}
