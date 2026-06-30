package com.aiphoneguardian.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AIPhoneGuardianApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        MobileAds.initialize(this) {}
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_THREATS,
                    getString(R.string.channel_threats),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "High priority threat alerts"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_GUARDIAN,
                    getString(R.string.channel_guardian),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Guardian service running notification"
                },
                NotificationChannel(
                    CHANNEL_GENERAL,
                    getString(R.string.channel_general),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(channels)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    companion object {
        const val CHANNEL_THREATS = "threat_alerts"
        const val CHANNEL_GUARDIAN = "guardian_service"
        const val CHANNEL_GENERAL = "general_notifications"
    }
}
