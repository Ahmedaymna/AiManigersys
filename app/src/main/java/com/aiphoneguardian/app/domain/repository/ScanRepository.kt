package com.aiphoneguardian.app.domain.repository

import com.aiphoneguardian.app.domain.model.AiThreatReport
import com.aiphoneguardian.app.domain.model.ScanResult
import com.aiphoneguardian.app.domain.model.ThreatItem
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    suspend fun performQuickScan(): Flow<ScanResult>
    suspend fun performFullScan(): Flow<ScanResult>
    suspend fun scanApps(): List<ThreatItem>
    suspend fun scanFiles(): List<ThreatItem>
    suspend fun scanProcesses(): List<ThreatItem>
    suspend fun scanNetwork(): List<ThreatItem>
    suspend fun getScanHistory(): List<ScanResult>
    suspend fun getLastScanResult(): ScanResult?
    suspend fun clearScanHistory()
    suspend fun analyzeWithAI(threats: List<ThreatItem>, language: String): AiThreatReport
}
