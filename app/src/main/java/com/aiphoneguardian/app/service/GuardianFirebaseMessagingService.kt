package com.aiphoneguardian.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aiphoneguardian.app.AIPhoneGuardianApp
import com.aiphoneguardian.app.R
import com.aiphoneguardian.app.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GuardianFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to server if needed
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.threat_detected)
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val severity = message.data["severity"] ?: "warning"

        showNotification(title, body, severity)
    }

    private fun showNotification(title: String, body: String, severity: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from_notification", true)
            putExtra("severity", severity)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = when (severity.lowercase()) {
            "critical" -> NotificationCompat.PRIORITY_MAX
            "warning" -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(this, AIPhoneGuardianApp.CHANNEL_THREATS)
            .setSmallIcon(R.drawable.ic_splash)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
