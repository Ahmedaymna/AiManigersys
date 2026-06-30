package com.aiphoneguardian.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun isDarkTheme(): Flow<Boolean>
    suspend fun setDarkTheme(enabled: Boolean)
    fun getLanguage(): Flow<String>
    suspend fun setLanguage(language: String)
    fun areNotificationsEnabled(): Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)
    fun getScanSchedule(): Flow<String>
    suspend fun setScanSchedule(schedule: String)
    suspend fun clearAllData()
}
