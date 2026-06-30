package com.aiphoneguardian.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiphoneguardian.app.domain.model.ScanSchedule
import com.aiphoneguardian.app.domain.repository.AuthRepository
import com.aiphoneguardian.app.domain.repository.SettingsRepository
import com.aiphoneguardian.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val isDarkTheme: Flow<Boolean> = settingsRepository.isDarkTheme()
    val language: Flow<String> = settingsRepository.getLanguage()
    val notificationsEnabled: Flow<Boolean> = settingsRepository.areNotificationsEnabled()
    val scanSchedule: Flow<ScanSchedule> = subscriptionRepository.getScanSchedule()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    init {
        loadSubscription()
    }

    private fun loadSubscription() {
        viewModelScope.launch {
            subscriptionRepository.getSubscription().collect { subscription ->
                _isPremium.value = subscription.tier.isPremium
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(enabled)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setScanSchedule(schedule: ScanSchedule) {
        viewModelScope.launch {
            subscriptionRepository.setScanSchedule(schedule)
        }
    }

    fun showLogoutDialog() {
        _showLogoutDialog.value = true
    }

    fun hideLogoutDialog() {
        _showLogoutDialog.value = false
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
