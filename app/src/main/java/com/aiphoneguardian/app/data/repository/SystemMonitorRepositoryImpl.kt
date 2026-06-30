package com.aiphoneguardian.app.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.aiphoneguardian.app.domain.repository.AppInfo
import com.aiphoneguardian.app.domain.repository.BatteryInfo
import com.aiphoneguardian.app.domain.model.ConnectionState
import com.aiphoneguardian.app.domain.model.ConnectionType
import com.aiphoneguardian.app.domain.model.NetworkConnection
import com.aiphoneguardian.app.domain.model.NetworkStatus
import com.aiphoneguardian.app.domain.model.ProcessInfo
import com.aiphoneguardian.app.domain.repository.SystemMonitorRepository
import com.aiphoneguardian.app.domain.model.SystemStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedReader
import java.io.FileReader
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemMonitorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) : SystemMonitorRepository {

    private val protectionKey = booleanPreferencesKey("real_time_protection")

    override fun getSystemStatus(): Flow<SystemStatus> = flow {
        while (true) {
            val (ramUsed, ramTotal) = getRamUsage()
            val batteryInfo = getBatteryStatus()
            val netStatus = getNetworkStatus()

            emit(SystemStatus(
                cpuUsagePercent = getCpuUsage(),
                ramUsagePercent = if (ramTotal > 0) (ramUsed.toFloat() / ramTotal * 100) else 0f,
                ramUsedMb = ramUsed,
                ramTotalMb = ramTotal,
                batteryPercent = batteryInfo.level,
                batteryTemperature = batteryInfo.temperature,
                isCharging = batteryInfo.isCharging,
                storageUsedGb = getStorageUsed(),
                storageTotalGb = getStorageTotal(),
                networkStatus = netStatus,
                uptimeMs = getSystemUptime(),
                runningProcesses = getRunningProcessCount()
            ))
            delay(2000)
        }
    }

    override fun getRealTimeProtectionStatus(): Flow<Boolean> = flow {
        dataStore.data.collect { prefs ->
            emit(prefs[protectionKey] ?: false)
        }
    }

    override suspend fun setRealTimeProtection(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[protectionKey] = enabled
        }
    }

    override fun getActiveConnections(): Flow<List<NetworkConnection>> = flow {
        while (true) {
            val connections = mutableListOf<NetworkConnection>()
            try {
                val proc = Runtime.getRuntime().exec("cat /proc/net/tcp")
                proc.inputStream.bufferedReader().useLines { lines ->
                    lines.drop(1).forEach { line ->
                        val parts = line.trim().split(Regex("\\s+"))
                        if (parts.size >= 4) {
                            val remoteHex = parts[2]
                            val remoteAddr = parseRemoteAddress(remoteHex)
                            val state = when (parts[3]) {
                                "01" -> ConnectionState.ESTABLISHED
                                "0A" -> ConnectionState.LISTENING
                                "06" -> ConnectionState.TIME_WAIT
                                "0B" -> ConnectionState.CLOSE_WAIT
                                else -> ConnectionState.SYN_SENT
                            }
                            connections.add(NetworkConnection(
                                id = UUID.randomUUID().toString(),
                                remoteAddress = remoteAddr.first,
                                remotePort = remoteAddr.second,
                                localPort = 0,
                                protocol = "TCP",
                                state = state,
                                appName = null,
                                isSuspicious = isSuspiciousPort(remoteAddr.second)
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback connections
            }
            emit(connections.take(10))
            delay(3000)
        }
    }

    override suspend fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(android.content.pm.PackageManager.ApplicationInfoFlags.of(0))
        } else {
            pm.getInstalledApplications(0)
        }

        return apps.map { app ->
            AppInfo(
                packageName = app.packageName,
                appName = pm.getApplicationLabel(app).toString(),
                versionName = try {
                    pm.getPackageInfo(app.packageName, 0).versionName ?: "unknown"
                } catch (e: Exception) { "unknown" },
                installDate = try {
                    pm.getPackageInfo(app.packageName, 0).firstInstallTime
                } catch (e: Exception) { 0L },
                lastUpdateDate = try {
                    pm.getPackageInfo(app.packageName, 0).lastUpdateTime
                } catch (e: Exception) { 0L },
                permissions = try {
                    pm.getPackageInfo(app.packageName, android.content.pm.PackageManager.GET_PERMISSIONS)
                        .requestedPermissions?.toList() ?: emptyList()
                } catch (e: Exception) { emptyList() },
                isSystemApp = app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0,
                isEnabled = app.enabled
            )
        }
    }

    override suspend fun getRunningProcesses(): List<ProcessInfo> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.runningAppProcesses?.map { process ->
            ProcessInfo(
                pid = process.pid,
                name = process.processName,
                packageName = process.pkgList?.firstOrNull(),
                cpuUsage = 0f,
                memoryUsageKb = process.lastTrimLevel.toLong(),
                isSystem = process.processName.startsWith("android") || process.processName.startsWith("com.android")
            )
        } ?: emptyList()
    }

    override fun getCpuUsage(): Float {
        return try {
            val reader = BufferedReader(FileReader("/proc/stat"))
            val line = reader.readLine()
            reader.close()

            if (line != null && line.startsWith("cpu ")) {
                val parts = line.split(Regex("\\s+")).drop(1).map { it.toLongOrNull() ?: 0 }
                val user = parts.getOrElse(0) { 0 }
                val nice = parts.getOrElse(1) { 0 }
                val system = parts.getOrElse(2) { 0 }
                val idle = parts.getOrElse(3) { 0 }
                val total = user + nice + system + idle
                val active = user + nice + system

                if (total > 0) (active.toFloat() / total * 100).coerceIn(0f, 100f) else 0f
            } else 0f
        } catch (e: Exception) {
            0f
        }
    }

    override fun getRamUsage(): Pair<Long, Long> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val totalRam = mi.totalMem / (1024 * 1024)
        val availableRam = mi.availMem / (1024 * 1024)
        return Pair(totalRam - availableRam, totalRam)
    }

    override fun getBatteryStatus(): BatteryInfo {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val tech = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        return BatteryInfo(pct, temp, isCharging, voltage, tech)
    }

    private fun getStorageUsed(): Float {
        val stat = android.os.StatFs(context.filesDir.path)
        val total = stat.totalBytes / (1024f * 1024f * 1024f)
        val free = stat.availableBytes / (1024f * 1024f * 1024f)
        return total - free
    }

    private fun getStorageTotal(): Float {
        val stat = android.os.StatFs(context.filesDir.path)
        return stat.totalBytes / (1024f * 1024f * 1024f)
    }

    private fun getNetworkStatus(): NetworkStatus {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val capabilities = activeNetwork?.let { cm.getNetworkCapabilities(it) }

        val connectionType = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> ConnectionType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> ConnectionType.MOBILE
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> ConnectionType.VPN
            else -> ConnectionType.NONE
        }

        return NetworkStatus(
            isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true,
            connectionType = connectionType,
            uploadSpeedBps = 0,
            downloadSpeedBps = 0,
            activeConnections = emptyList()
        )
    }

    private fun getSystemUptime(): Long {
        return try {
            val uptime = System.currentTimeMillis() - java.io.BufferedReader(
                java.io.FileReader("/proc/uptime")
            ).readLine().split(" ")[0].toDouble() * 1000
            uptime.toLong()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun getRunningProcessCount(): Int {
        return try {
            val proc = Runtime.getRuntime().exec("ps -A")
            proc.inputStream.bufferedReader().useLines { lines -> lines.count() - 1 }
        } catch (e: Exception) { 0 }
    }

    private fun parseRemoteAddress(hex: String): Pair<String, Int> {
        return try {
            val (addrHex, portHex) = hex.split(":")
            val addr = addrHex.chunked(2).reversed().joinToString(".") {
                it.toInt(16).toString()
            }
            val port = portHex.toInt(16)
            addr to port
        } catch (e: Exception) {
            "0.0.0.0" to 0
        }
    }

    private fun isSuspiciousPort(port: Int): Boolean {
        return port in listOf(4444, 5555, 6666, 8888, 9999, 31337)
    }
}
