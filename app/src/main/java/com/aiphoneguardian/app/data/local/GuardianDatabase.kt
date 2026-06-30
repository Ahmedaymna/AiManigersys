package com.aiphoneguardian.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aiphoneguardian.app.data.local.dao.ChatMessageDao
import com.aiphoneguardian.app.data.local.dao.QuarantinedFileDao
import com.aiphoneguardian.app.data.local.dao.ScanResultDao
import com.aiphoneguardian.app.data.local.entity.ChatMessageEntity
import com.aiphoneguardian.app.data.local.entity.QuarantinedFileEntity
import com.aiphoneguardian.app.data.local.entity.ScanResultEntity

@Database(
    entities = [
        ScanResultEntity::class,
        ChatMessageEntity::class,
        QuarantinedFileEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class GuardianDatabase : RoomDatabase() {
    abstract fun scanResultDao(): ScanResultDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun quarantinedFileDao(): QuarantinedFileDao

    companion object {
        const val DATABASE_NAME = "guardian_database"
    }
}
