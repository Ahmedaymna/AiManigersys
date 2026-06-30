package com.aiphoneguardian.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aiphoneguardian.app.data.local.entity.ScanResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanResult(result: ScanResultEntity)

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    fun getAllScanResults(): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastScanResult(): ScanResultEntity?

    @Query("DELETE FROM scan_results")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM scan_results")
    suspend fun getScanCount(): Int
}
