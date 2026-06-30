package com.aiphoneguardian.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiphoneguardian.app.domain.model.*
import com.aiphoneguardian.app.domain.model.ScanRepository
import com.aiphoneguardian.app.domain.model.SystemMonitorRepository
import com.aiphoneguardian.app.domain.repository.SettingsRepository
import com.aiphoneguardian.app.domain.repository.SubscriptionRepository
import com.aiphoneguardian.app.ui.screens.dashboard.AlertItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val systemMonitorRepository: SystemMonitorRepository,
    private val scanRepository: ScanRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _systemStatus = MutableStateFlow<SystemStatus?>(null)
    val systemStatus: StateFlow<SystemStatus?> = _systemStatus.asStateFlow()

    private val _lastScanResult = MutableStateFlow<ScanResult?>(null)
    val lastScanResult: StateFlow<ScanResult?> = _lastScanResult.asStateFlow()

    private val _isProtectionEnabled = MutableStateFlow(false)
    val isProtectionEnabled: StateFlow<Boolean> = _isProtectionEnabled.asStateFlow()

    private val _recentAlerts = MutableStateFlow<List<AlertItem>>(emptyList())
    val recentAlerts: StateFlow<List<AlertItem>> = _recentAlerts.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    init {
        loadSystemStatus()
        loadLastScan()
        loadProtectionStatus()
        loadSubscription()
        generateSampleAlerts()
    }

    private fun loadSystemStatus() {
        viewModelScope.launch {
            systemMonitorRepository.getSystemStatus().collect { status ->
                _systemStatus.value = status
            }
        }
    }

    private fun loadLastScan() {
        viewModelScope.launch {
            val result = scanRepository.getLastScanResult()
            _lastScanResult.value = result
        }
    }

    private fun loadProtectionStatus() {
        viewModelScope.launch {
            systemMonitorRepository.getRealTimeProtectionStatus().collect { enabled ->
                _isProtectionEnabled.value = enabled
            }
        }
    }

    private fun loadSubscription() {
        viewModelScope.launch {
            subscriptionRepository.getSubscription().collect { subscription ->
                _isPremium.value = subscription.tier.isPremium
            }
        }
    }

    fun toggleProtection(enabled: Boolean) {
        viewModelScope.launch {
            systemMonitorRepository.setRealTimeProtection(enabled)
            _isProtectionEnabled.value = enabled
        }
    }

    private fun generateSampleAlerts() {
        _recentAlerts.value = listOf(
            AlertItem(
                title = "Suspicious App Detected",
                description = "App with excessive permissions found",
                severity = ThreatLevel.WARNING,
                timestamp = "2h ago"
            ),
            AlertItem(
                title = "Scan Complete",
                description = "No threats found during quick scan",
                severity = ThreatLevel.SAFE,
                timestamp = "5h ago"
            )
        )
    }
}
