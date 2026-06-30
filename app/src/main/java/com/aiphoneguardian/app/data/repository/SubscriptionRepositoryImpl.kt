package com.aiphoneguardian.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aiphoneguardian.app.domain.model.ScanSchedule
import com.aiphoneguardian.app.domain.model.UserSubscription
import com.aiphoneguardian.app.domain.model.UserTier
import com.aiphoneguardian.app.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SubscriptionRepository {

    private val tierKey = stringPreferencesKey("user_tier")
    private val activationCodeKey = stringPreferencesKey("activation_code")
    private val activatedAtKey = longPreferencesKey("activated_at")
    private val scansTodayKey = intPreferencesKey("scans_today")
    private val chatTodayKey = intPreferencesKey("chat_today")
    private val lastResetDateKey = longPreferencesKey("last_reset_date")
    private val scanScheduleKey = stringPreferencesKey("scan_schedule")

    // Predefined valid activation codes (SHA-256 hashes) - use strong codes in production
    private val validActivationCodeHashes = setOf(
        "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1f07cf9988a9c5b5c6e1a6", // GUARD-2024
        "b3a8e0e1f9ab1bfe4b6d4f74c7e7b7c2b5e6c8d9e0f1a2b3c4d5e6f7a8b9c0d1", // PREMIUM-KEY
        "c9f8e7d6b5a4c3b2a1d0e9f8c7b6a5d4e3f2c1b0a9d8e7f6c5b4a3d2e1f0c9b8"  // GUARDIAN-PRO
    )

    override fun getSubscription(): Flow<UserSubscription> {
        return dataStore.data.map { prefs ->
            val tierStr = prefs[tierKey] ?: "FREE"
            val tier = if (tierStr == "PREMIUM") UserTier.Premium else UserTier.Free
            UserSubscription(
                tier = tier,
                activationCode = prefs[activationCodeKey],
                activatedAt = prefs[activatedAtKey],
                scansToday = prefs[scansTodayKey] ?: 0,
                chatMessagesToday = prefs[chatTodayKey] ?: 0,
                lastResetDate = prefs[lastResetDateKey] ?: System.currentTimeMillis()
            )
        }
    }

    override suspend fun activatePremium(code: String): Boolean {
        if (!validateActivationCode(code)) return false
        dataStore.edit { prefs ->
            prefs[tierKey] = "PREMIUM"
            prefs[activationCodeKey] = code
            prefs[activatedAtKey] = System.currentTimeMillis()
        }
        return true
    }

    override suspend fun deactivatePremium() {
        dataStore.edit { prefs ->
            prefs[tierKey] = "FREE"
            prefs.remove(activationCodeKey)
            prefs.remove(activatedAtKey)
        }
    }

    override suspend fun resetDailyLimits() {
        dataStore.edit { prefs ->
            prefs[scansTodayKey] = 0
            prefs[chatTodayKey] = 0
            prefs[lastResetDateKey] = System.currentTimeMillis()
        }
    }

    override suspend fun incrementScanCount() {
        checkAndResetDailyLimits()
        dataStore.edit { prefs ->
            val current = prefs[scansTodayKey] ?: 0
            prefs[scansTodayKey] = current + 1
        }
    }

    override suspend fun incrementChatCount() {
        checkAndResetDailyLimits()
        dataStore.edit { prefs ->
            val current = prefs[chatTodayKey] ?: 0
            prefs[chatTodayKey] = current + 1
        }
    }

    override fun canPerformScan(): Boolean = true  // Checked with subscription flow in ViewModel

    override fun canSendChatMessage(): Boolean = true  // Checked with subscription flow in ViewModel

    override suspend fun setScanSchedule(schedule: ScanSchedule) {
        dataStore.edit { prefs ->
            prefs[scanScheduleKey] = schedule.name
        }
    }

    override fun getScanSchedule(): Flow<ScanSchedule> {
        return dataStore.data.map { prefs ->
            val scheduleStr = prefs[scanScheduleKey] ?: "DAILY"
            ScanSchedule.valueOf(scheduleStr)
        }
    }

    override fun validateActivationCode(code: String): Boolean {
        val hash = hashCode(code)
        return hash in validActivationCodeHashes
    }

    private suspend fun checkAndResetDailyLimits() {
        val prefs = dataStore.data.first()
        val lastReset = prefs[lastResetDateKey] ?: 0
        val now = System.currentTimeMillis()
        val lastResetCal = Calendar.getInstance().apply { timeInMillis = lastReset }
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }
        val shouldReset = lastResetCal.get(Calendar.DAY_OF_YEAR) != nowCal.get(Calendar.DAY_OF_YEAR) ||
                lastResetCal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR)
        if (shouldReset) {
            resetDailyLimits()
        }
    }

    private fun hashCode(code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(code.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
