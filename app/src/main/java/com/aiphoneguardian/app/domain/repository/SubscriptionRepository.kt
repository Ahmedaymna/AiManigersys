package com.aiphoneguardian.app.domain.repository

import com.aiphoneguardian.app.domain.model.ScanSchedule
import com.aiphoneguardian.app.domain.model.UserSubscription
import com.aiphoneguardian.app.domain.model.UserTier
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getSubscription(): Flow<UserSubscription>
    suspend fun activatePremium(code: String): Boolean
    suspend fun deactivatePremium()
    suspend fun resetDailyLimits()
    suspend fun incrementScanCount()
    suspend fun incrementChatCount()
    fun canPerformScan(): Boolean
    fun canSendChatMessage(): Boolean
    suspend fun setScanSchedule(schedule: ScanSchedule)
    fun getScanSchedule(): Flow<ScanSchedule>
    fun validateActivationCode(code: String): Boolean
}
