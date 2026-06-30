package com.aiphoneguardian.app.domain.model

data class ScanResult(
    val id: String,
    val scanType: ScanType,
    val timestamp: Long,
    val overallStatus: ThreatLevel,
    val threats: List<ThreatItem>,
    val appsScanned: Int,
    val filesScanned: Int,
    val durationMs: Long,
    val aiReport: AiThreatReport? = null
)

enum class ScanType {
    QUICK,
    FULL,
    CUSTOM,
    REALTIME
}

data class ThreatItem(
    val id: String,
    val name: String,
    val type: ThreatType,
    val severity: ThreatLevel,
    val description: String,
    val recommendation: String,
    val filePath: String? = null,
    val packageName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ThreatType {
    MALWARE,
    SUSPICIOUS_APP,
    SUSPICIOUS_FILE,
    NETWORK_INTRUSION,
    PRIVACY_RISK,
    ADWARE,
    TROJAN,
    RANSOMWARE,
    PHISHING,
    UNKNOWN
}

data class AiThreatReport(
    val summary: String,
    val detailedAnalysis: String,
    val recommendedActions: List<String>,
    val confidenceScore: Float,
    val isInArabic: Boolean = false
)
