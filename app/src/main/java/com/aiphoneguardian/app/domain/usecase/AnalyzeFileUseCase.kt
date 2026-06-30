package com.aiphoneguardian.app.domain.usecase

import com.aiphoneguardian.app.domain.model.FileAiAnalysis
import com.aiphoneguardian.app.domain.repository.FileRepository
import javax.inject.Inject

class AnalyzeFileUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(filePath: String): FileAiAnalysis {
        return fileRepository.analyzeFileWithAI(filePath)
    }
}
