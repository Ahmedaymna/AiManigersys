package com.aiphoneguardian.app.domain.model

enum class ThreatLevel {
    SAFE,
    WARNING,
    CRITICAL;

    fun toLocalizedString(): String = when (this) {
        SAFE -> "Safe"
        WARNING -> "Warning"
        CRITICAL -> "Critical"
    }

    companion object {
        fun fromString(value: String): ThreatLevel = when (value.lowercase()) {
            "safe" -> SAFE
            "warning" -> WARNING
            "critical" -> CRITICAL
            else -> SAFE
        }
    }
}
