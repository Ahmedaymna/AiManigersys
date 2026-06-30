package com.aiphoneguardian.app.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.aiphoneguardian.app.AIPhoneGuardianApp
import com.aiphoneguardian.app.R
import com.aiphoneguardian.app.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class GuardianForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var wakeLock: PowerManager.WakeLock? = null

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AIPhoneGuardian::GuardianWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L)
        }
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                monitorSystem()
                delay(5000)
            }
        }

        serviceScope.launch {
            while (isActive) {
                checkNewInstallations()
                delay(30000)
            }
        }

        serviceScope.launch {
            while (isActive) {
                checkFileChanges()
                delay(60000)
            }
        }
    }

    private fun monitorSystem() {
        // Monitor for suspicious activities
    }

    private fun checkNewInstallations() {
        try {
            val pm = packageManager
            val recentApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                pm.getInstalledApplications(0)
            }.filter { app ->
                val installTime = try {
                    pm.getPackageInfo(app.packageName, 0).firstInstallTime
                } catch (e: Exception) { 0L }
                System.currentTimeMillis() - installTime < 3600000 // Within last hour
            }

            if (recentApps.isNotEmpty()) {
                val app = recentApps.first()
                val appName = pm.getApplicationLabel(app).toString()
                sendThreatNotification(
                    "New App Installed",
                    "$appName was installed. Tap to scan.",
                    ThreatSeverity.INFO
                )
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun checkFileChanges() {
        try {
            val downloadDir = getExternalFilesDir(null)?.parentFile?.parentFile
            downloadDir?.listFiles()?.forEach { dir ->
                if (dir.isDirectory && dir.name == "Download") {
                    dir.listFiles()?.forEach { file ->
                        if (isSuspiciousFile(file)) {
                            sendThreatNotification(
                                "Suspicious File Detected",
                                "${file.name} may be a security risk.",
                                ThreatSeverity.WARNING
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun isSuspiciousFile(file: File): Boolean {
        val suspiciousExts = setOf("apk", "exe", "bat", "sh", "bin")
        return suspiciousExts.contains(file.extension.lowercase()) && file.length() > 1024
    }

    private fun sendThreatNotification(title: String, message: String, severity: ThreatSeverity) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "scanner")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, AIPhoneGuardianApp.CHANNEL_THREATS)
            .setSmallIcon(R.drawable.ic_splash)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(severity.notificationPriority)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(if (severity == ThreatSeverity.CRITICAL) longArrayOf(0, 500, 200, 500) else null)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, AIPhoneGuardianApp.CHANNEL_GUARDIAN)
            .setSmallIcon(R.drawable.ic_splash)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.guardian_running))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        super.onDestroy()
    }

    enum class ThreatSeverity(val notificationPriority: Int) {
        INFO(NotificationCompat.PRIORITY_DEFAULT),
        WARNING(NotificationCompat.PRIORITY_HIGH),
        CRITICAL(NotificationCompat.PRIORITY_MAX)
    }

    companion object {
        const val ACTION_START = "com.aiphoneguardian.app.START_GUARDIAN"
        const val ACTION_STOP = "com.aiphoneguardian.app.STOP_GUARDIAN"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, GuardianForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, GuardianForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
