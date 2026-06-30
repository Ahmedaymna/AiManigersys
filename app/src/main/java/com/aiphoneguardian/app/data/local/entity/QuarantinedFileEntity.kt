package com.aiphoneguardian.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quarantined_files")
data class QuarantinedFileEntity(
    @PrimaryKey
    val originalPath: String,
    val quarantinePath: String,
    val fileName: String,
    val fileSize: Long,
    val quarantineDate: Long,
    val riskLevel: String
)
