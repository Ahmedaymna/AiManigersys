package com.aiphoneguardian.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aiphoneguardian.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val dataStore = context.settingsDataStore

    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val languageKey = stringPreferencesKey("language")
    private val notificationsKey = booleanPreferencesKey("notifications")
    private val scanScheduleKey = stringPreferencesKey("scan_schedule")

    override fun isDarkTheme(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[darkThemeKey] ?: true
        }
    }

    override suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[darkThemeKey] = enabled
        }
    }

    override fun getLanguage(): Flow<String> {
        return dataStore.data.map { prefs ->
            prefs[languageKey] ?: "en"
        }
    }

    override suspend fun setLanguage(language: String) {
        dataStore.edit { prefs ->
            prefs[languageKey] = language
        }
    }

    override fun areNotificationsEnabled(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[notificationsKey] ?: true
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[notificationsKey] = enabled
        }
    }

    override fun getScanSchedule(): Flow<String> {
        return dataStore.data.map { prefs ->
            prefs[scanScheduleKey] ?: "DAILY"
        }
    }

    override suspend fun setScanSchedule(schedule: String) {
        dataStore.edit { prefs ->
            prefs[scanScheduleKey] = schedule
        }
    }

    override suspend fun clearAllData() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
