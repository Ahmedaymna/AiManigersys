package com.aiphoneguardian.app.domain.model

data class SystemStatus(
    val cpuUsagePercent: Float,
    val ramUsagePercent: Float,
    val ramUsedMb: Long,
    val ramTotalMb: Long,
    val batteryPercent: Int,
    val batteryTemperature: Float,
    val isCharging: Boolean,
    val storageUsedGb: Float,
    val storageTotalGb: Float,
    val networkStatus: NetworkStatus,
    val uptimeMs: Long,
    val runningProcesses: Int
)

data class NetworkStatus(
    val isConnected: Boolean,
    val connectionType: ConnectionType,
    val uploadSpeedBps: Long,
    val downloadSpeedBps: Long,
    val activeConnections: List<NetworkConnection>,
    val suspiciousConnections: Int = 0
)

enum class ConnectionType {
    WIFI,
    MOBILE,
    VPN,
    NONE
}

data class NetworkConnection(
    val id: String,
    val remoteAddress: String,
    val remotePort: Int,
    val localPort: Int,
    val protocol: String,
    val state: ConnectionState,
    val appName: String?,
    val countryCode: String? = null,
    val isSuspicious: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ConnectionState {
    ESTABLISHED,
    LISTENING,
    TIME_WAIT,
    CLOSE_WAIT,
    SYN_SENT
}
