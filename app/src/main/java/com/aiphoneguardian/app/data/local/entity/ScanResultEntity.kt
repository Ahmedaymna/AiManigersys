package com.aiphoneguardian.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey
    val id: String,
    val scanType: String,
    val timestamp: Long,
    val overallStatus: String,
    val threatsJson: String,
    val appsScanned: Int,
    val filesScanned: Int,
    val durationMs: Long,
    val aiReportJson: String? = null
)
