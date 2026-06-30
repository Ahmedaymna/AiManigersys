package com.aiphoneguardian.app.domain.usecase

import com.aiphoneguardian.app.domain.model.ScanRepository
import com.aiphoneguardian.app.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PerformQuickScanUseCase @Inject constructor(
    private val scanRepository: ScanRepository
) {
    suspend operator fun invoke(): Flow<ScanResult> {
        return scanRepository.performQuickScan()
    }
}
