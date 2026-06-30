package com.aiphoneguardian.app.domain.model

data class FileItem(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val extension: String,
    val riskLevel: FileRiskLevel = FileRiskLevel.UNKNOWN,
    val aiAnalysis: FileAiAnalysis? = null,
    val isQuarantined: Boolean = false,
    val quarantineDate: Long? = null
)

enum class FileRiskLevel {
    SAFE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
    UNKNOWN;

    fun colorHex(): String = when (this) {
        SAFE -> "#00FF88"
        LOW -> "#88FF00"
        MEDIUM -> "#FFCC00"
        HIGH -> "#FF6600"
        CRITICAL -> "#FF0044"
        UNKNOWN -> "#888888"
    }
}

data class FileAiAnalysis(
    val riskLevel: FileRiskLevel,
    val analysis: String,
    fileHash: String? = null,
    val behaviors: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
