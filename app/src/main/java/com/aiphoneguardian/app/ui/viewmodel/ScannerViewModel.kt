package com.aiphoneguardian.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiphoneguardian.app.domain.model.ScanResult
import com.aiphoneguardian.app.domain.model.ScanType
import com.aiphoneguardian.app.domain.model.ThreatItem
import com.aiphoneguardian.app.domain.model.ThreatLevel
import com.aiphoneguardian.app.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _currentScanningItem = MutableStateFlow("Initializing...")
    val currentScanningItem: StateFlow<String> = _currentScanningItem.asStateFlow()

    fun startQuickScan() {
        viewModelScope.launch {
            _scanState.value = ScanState.Scanning(ScanType.QUICK)
            _scanProgress.value = 0f
            _currentScanningItem.value = "Starting quick scan..."

            try {
                scanRepository.performQuickScan().collect { result ->
                    _scanProgress.value = when {
                        result.durationMs < 20 -> result.durationMs * 5f
                        result.durationMs < 50 -> result.durationMs * 2f
                        else -> result.durationMs.coerceIn(0f, 100f)
                    }

                    if (result.threats.isNotEmpty()) {
                        _currentScanningItem.value = "Analyzing ${result.threats.last().name}..."
                    }

                    if (result.aiReport != null || _scanProgress.value >= 99f) {
                        _scanResult.value = result
                    }
                }
                _scanState.value = ScanState.Complete
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Scan failed")
            }
        }
    }

    fun startFullScan() {
        viewModelScope.launch {
            _scanState.value = ScanState.Scanning(ScanType.FULL)
            _scanProgress.value = 0f
            _currentScanningItem.value = "Starting full system scan..."

            try {
                scanRepository.performFullScan().collect { result ->
                    _scanProgress.value = when {
                        result.durationMs < 15 -> result.durationMs.toFloat() * 6f
                        result.durationMs < 40 -> result.durationMs.toFloat() * 2.5f
                        else -> result.durationMs.toFloat().coerceIn(0f, 100f)
                    }

                    if (result.threats.isNotEmpty()) {
                        _currentScanningItem.value = "Scanning: ${result.threats.last().name}"
                    } else {
                        _currentScanningItem.value = when (_scanProgress.value) {
                            in 0f..25f -> "Scanning installed applications..."
                            in 25f..50f -> "Scanning file system..."
                            in 50f..75f -> "Scanning running processes..."
                            else -> "AI analyzing results..."
                        }
                    }

                    if (result.aiReport != null || _scanProgress.value >= 99f) {
                        _scanResult.value = result
                    }
                }
                _scanState.value = ScanState.Complete
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Full scan failed")
            }
        }
    }

    fun fixAllThreats() {
        viewModelScope.launch {
            // Implement threat fixing logic
            _scanResult.value = _scanResult.value?.copy(
                overallStatus = ThreatLevel.SAFE,
                threats = emptyList()
            )
        }
    }

    fun resetScan() {
        _scanState.value = ScanState.Idle
        _scanResult.value = null
        _scanProgress.value = 0f
        _currentScanningItem.value = ""
    }

    sealed class ScanState {
        object Idle : ScanState()
        data class Scanning(val scanType: ScanType) : ScanState()
        object Complete : ScanState()
        data class Error(val message: String) : ScanState()
    }
}
