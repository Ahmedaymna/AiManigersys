package com.aiphoneguardian.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.aiphoneguardian.app.service.GuardianForegroundService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PackageInstallReceiver : BroadcastReceiver() {

    @Inject
    lateinit var packageManager: PackageManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_PACKAGE_ADDED ||
            intent.action == Intent.ACTION_PACKAGE_REPLACED) {

            val packageName = intent.data?.schemeSpecificPart ?: return

            if (!isSystemApp(packageName)) {
                // Restart guardian service to monitor new app
                GuardianForegroundService.start(context)
            }
        }
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                packageManager.getApplicationInfo(packageName, 0)
            }
            (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }
}
