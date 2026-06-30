package com.aiphoneguardian.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SystemMonitorRepository {
    fun getSystemStatus(): Flow<SystemStatus>
    fun getRealTimeProtectionStatus(): Flow<Boolean>
    suspend fun setRealTimeProtection(enabled: Boolean)
    fun getActiveConnections(): Flow<List<NetworkConnection>>
    suspend fun getInstalledApps(): List<AppInfo>
    suspend fun getRunningProcesses(): List<ProcessInfo>
    fun getCpuUsage(): Float
    fun getRamUsage(): Pair<Long, Long>
    fun getBatteryStatus(): BatteryInfo
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val installDate: Long,
    val lastUpdateDate: Long,
    val permissions: List<String>,
    val isSystemApp: Boolean,
    val isEnabled: Boolean
)

data class ProcessInfo(
    val pid: Int,
    val name: String,
    val packageName: String?,
    val cpuUsage: Float,
    val memoryUsageKb: Long,
    val isSystem: Boolean
)

data class BatteryInfo(
    val level: Int,
    val temperature: Float,
    val isCharging: Boolean,
    val voltage: Int,
    val technology: String
)
