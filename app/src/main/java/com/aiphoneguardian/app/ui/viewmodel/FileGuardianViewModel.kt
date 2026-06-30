package com.aiphoneguardian.app.ui.viewmodel

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiphoneguardian.app.domain.model.FileAiAnalysis
import com.aiphoneguardian.app.domain.model.FileItem
import com.aiphoneguardian.app.domain.model.FileRiskLevel
import com.aiphoneguardian.app.domain.model.UserTier
import com.aiphoneguardian.app.domain.repository.FileRepository
import com.aiphoneguardian.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileGuardianViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _selectedFile = MutableStateFlow<FileItem?>(null)
    val selectedFile: StateFlow<FileItem?> = _selectedFile.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _aiAnalysis = MutableStateFlow<FileAiAnalysis?>(null)
    val aiAnalysis: StateFlow<FileAiAnalysis?> = _aiAnalysis.asStateFlow()

    private val _currentPath = MutableStateFlow("/storage/emulated/0")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    init {
        loadSubscription()
        loadFiles()
    }

    private fun loadSubscription() {
        viewModelScope.launch {
            subscriptionRepository.getSubscription().collect { subscription ->
                _isPremium.value = subscription.tier.isPremium
            }
        }
    }

    private fun loadFiles() {
        viewModelScope.launch {
            try {
                val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                _currentPath.value = downloadsPath
                val fileList = fileRepository.browseDirectory(downloadsPath)
                _files.value = fileList.take(20) // Limit for free users
            } catch (e: Exception) {
                _files.value = emptyList()
            }
        }
    }

    fun selectFile(file: FileItem) {
        _selectedFile.value = file
        _aiAnalysis.value = file.aiAnalysis
    }

    fun clearSelection() {
        _selectedFile.value = null
        _aiAnalysis.value = null
    }

    fun analyzeFile(filePath: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val analysis = fileRepository.analyzeFileWithAI(filePath)
                _aiAnalysis.value = analysis
            } catch (e: Exception) {
                _aiAnalysis.value = FileAiAnalysis(
                    riskLevel = FileRiskLevel.UNKNOWN,
                    analysis = "Analysis failed: ${e.message}",
                    fileHash = null
                )
            }
            _isAnalyzing.value = false
        }
    }

    fun quarantineFile(filePath: String) {
        viewModelScope.launch {
            fileRepository.quarantineFile(filePath)
            clearSelection()
            loadFiles()
        }
    }

    fun markTrusted(filePath: String) {
        viewModelScope.launch {
            fileRepository.markFileAsTrusted(filePath)
        }
    }
}
