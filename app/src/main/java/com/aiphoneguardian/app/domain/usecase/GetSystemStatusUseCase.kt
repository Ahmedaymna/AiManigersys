package com.aiphoneguardian.app.domain.usecase

import com.aiphoneguardian.app.domain.model.SystemMonitorRepository
import com.aiphoneguardian.app.domain.model.SystemStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSystemStatusUseCase @Inject constructor(
    private val systemMonitorRepository: SystemMonitorRepository
) {
    operator fun invoke(): Flow<SystemStatus> {
        return systemMonitorRepository.getSystemStatus()
    }
}
