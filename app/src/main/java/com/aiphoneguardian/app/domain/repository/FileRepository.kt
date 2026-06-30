package com.aiphoneguardian.app.domain.repository

import com.aiphoneguardian.app.domain.model.FileAiAnalysis
import com.aiphoneguardian.app.domain.model.FileItem
import com.aiphoneguardian.app.domain.model.FileRiskLevel
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    suspend fun browseDirectory(path: String): List<FileItem>
    suspend fun analyzeFileWithAI(filePath: String): FileAiAnalysis
    suspend fun markFileAsTrusted(filePath: String)
    suspend fun markFileAsSuspicious(filePath: String)
    suspend fun quarantineFile(filePath: String): Boolean
    suspend fun restoreFromQuarantine(filePath: String): Boolean
    fun getQuarantinedFiles(): Flow<List<FileItem>>
    suspend fun getTrustedFiles(): List<String>
    suspend fun getFileHash(filePath: String): String
    suspend fun deleteFile(filePath: String): Boolean
    suspend fun getFileDetails(filePath: String): FileItem?
}
