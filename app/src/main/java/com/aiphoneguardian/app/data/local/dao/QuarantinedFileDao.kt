package com.aiphoneguardian.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aiphoneguardian.app.data.local.entity.QuarantinedFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuarantinedFileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: QuarantinedFileEntity)

    @Query("SELECT * FROM quarantined_files ORDER BY quarantineDate DESC")
    fun getAllQuarantinedFiles(): Flow<List<QuarantinedFileEntity>>

    @Query("DELETE FROM quarantined_files WHERE originalPath = :path")
    suspend fun removeFile(path: String)

    @Query("SELECT * FROM quarantined_files WHERE originalPath = :path LIMIT 1")
    suspend fun getFile(path: String): QuarantinedFileEntity?
}
