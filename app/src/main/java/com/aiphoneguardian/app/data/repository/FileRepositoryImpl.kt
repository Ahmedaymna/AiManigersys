package com.aiphoneguardian.app.data.repository

import android.content.Context
import android.os.Build
import android.os.Environment
import com.aiphoneguardian.app.data.local.dao.QuarantinedFileDao
import com.aiphoneguardian.app.data.remote.api.GeminiApiService
import com.aiphoneguardian.app.data.remote.model.GeminiContent
import com.aiphoneguardian.app.data.remote.model.GeminiPart
import com.aiphoneguardian.app.data.remote.model.GeminiRequest
import com.aiphoneguardian.app.domain.model.FileAiAnalysis
import com.aiphoneguardian.app.domain.model.FileItem
import com.aiphoneguardian.app.domain.model.FileRiskLevel
import com.aiphoneguardian.app.domain.repository.FileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quarantinedFileDao: QuarantinedFileDao,
    private val geminiApiService: GeminiApiService
) : FileRepository {

    companion object {
        // API key from BuildConfig (set GEMINI_API_KEY in local.properties)
        val GEMINI_API_KEY get() = com.aiphoneguardian.app.BuildConfig.GEMINI_API_KEY
    }

    override suspend fun browseDirectory(path: String): List<FileItem> {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir.listFiles()?.map { file ->
            FileItem(
                path = file.absolutePath,
                name = file.name,
                size = file.length(),
                lastModified = file.lastModified(),
                isDirectory = file.isDirectory,
                extension = file.extension,
                riskLevel = assessRisk(file)
            )
        }?.sortedWith(compareByDescending<FileItem> { it.isDirectory }.thenBy { it.name }) ?: emptyList()
    }

    override suspend fun analyzeFileWithAI(filePath: String): FileAiAnalysis {
        val file = File(filePath)
        if (!file.exists()) {
            return FileAiAnalysis(
                riskLevel = FileRiskLevel.UNKNOWN,
                analysis = "File not found",
                fileHash = null
            )
        }

        val fileHash = getFileHash(filePath)
        val extension = file.extension.lowercase()
        val size = file.length()

        return try {
            val prompt = buildString {
                appendLine("Analyze this file for security risks:")
                appendLine("- File name: ${file.name}")
                appendLine("- Extension: $extension")
                appendLine("- Size: $size bytes")
                appendLine("- Hash (SHA-256): $fileHash")
                appendLine("\nRespond with ONLY a JSON object in this format:")
                appendLine("{\"riskLevel\": \"SAFE|LOW|MEDIUM|HIGH|CRITICAL\", \"analysis\": \"your analysis\", \"behaviors\": [\"behavior1\", \"behavior2\"]}")
            }

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            )

            val response = geminiApiService.generateContent(GEMINI_API_KEY, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (responseText != null) {
                parseAiAnalysis(responseText, fileHash)
            } else {
                createHeuristicAnalysis(file.name, extension, size, fileHash)
            }
        } catch (e: Exception) {
            createHeuristicAnalysis(file.name, extension, size, fileHash)
        }
    }

    override suspend fun markFileAsTrusted(filePath: String) {
        quarantinedFileDao.removeFile(filePath)
    }

    override suspend fun markFileAsSuspicious(filePath: String) {
        // Mark in local database
    }

    override suspend fun quarantineFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) return false

            val quarantineDir = File(context.filesDir, "quarantine")
            if (!quarantineDir.exists()) quarantineDir.mkdirs()

            val quarantineFile = File(quarantineDir, file.name + "_" + System.currentTimeMillis())
            file.renameTo(quarantineFile)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun restoreFromQuarantine(filePath: String): Boolean {
        return try {
            val quarantineDir = File(context.filesDir, "quarantine")
            val quarantineFile = quarantineDir.listFiles()?.find { it.name.contains(File(filePath).name) }
            if (quarantineFile != null) {
                val originalDir = File(filePath).parentFile
                if (originalDir != null && !originalDir.exists()) originalDir.mkdirs()
                quarantineFile.renameTo(File(filePath))
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override fun getQuarantinedFiles(): Flow<List<FileItem>> {
        return quarantinedFileDao.getAllQuarantinedFiles().map { entities ->
            entities.map { entity ->
                FileItem(
                    path = entity.originalPath,
                    name = entity.fileName,
                    size = entity.fileSize,
                    lastModified = entity.quarantineDate,
                    isDirectory = false,
                    extension = entity.fileName.substringAfterLast(".", ""),
                    riskLevel = FileRiskLevel.valueOf(entity.riskLevel),
                    isQuarantined = true,
                    quarantineDate = entity.quarantineDate
                )
            }
        }
    }

    override suspend fun getTrustedFiles(): List<String> {
        return emptyList() // Implement as needed
    }

    override suspend fun getFileHash(filePath: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(filePath).use { fis ->
                val buffer = ByteArray(8192)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "unknown"
        }
    }

    override suspend fun deleteFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getFileDetails(filePath: String): FileItem? {
        val file = File(filePath)
        if (!file.exists()) return null
        return FileItem(
            path = file.absolutePath,
            name = file.name,
            size = file.length(),
            lastModified = file.lastModified(),
            isDirectory = file.isDirectory,
            extension = file.extension,
            riskLevel = assessRisk(file)
        )
    }

    private fun assessRisk(file: File): FileRiskLevel {
        val suspiciousExts = setOf("apk", "exe", "bat", "sh", "bin", "dex", "so", "dll")
        val ext = file.extension.lowercase()

        return when {
            file.name.startsWith(".") && file.length() > 1024 * 1024 -> FileRiskLevel.MEDIUM
            suspiciousExts.contains(ext) -> FileRiskLevel.HIGH
            file.name.contains("trojan", true) || file.name.contains("virus", true) -> FileRiskLevel.CRITICAL
            file.length() > 100 * 1024 * 1024 && !file.isDirectory -> FileRiskLevel.LOW
            else -> FileRiskLevel.UNKNOWN
        }
    }

    private fun parseAiAnalysis(responseText: String, fileHash: String?): FileAiAnalysis {
        return try {
            val cleanJson = responseText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val regex = Regex("\"riskLevel\"\\s*:\\s*\"([^\"]+)\"").find(cleanJson)
            val riskLevelStr = regex?.groupValues?.get(1) ?: "UNKNOWN"
            val analysisRegex = Regex("\"analysis\"\\s*:\\s*\"([^\"]+)\"").find(cleanJson)
            val analysis = analysisRegex?.groupValues?.get(1) ?: "No analysis available"
            val behaviorsRegex = Regex("\"behaviors\"\\s*:\\s*\\[([^\\]]*)\\]").find(cleanJson)
            val behaviors = behaviorsRegex?.groupValues?.get(1)?.split(",")?.map { it.trim().removeSurrounding("\"") }?.filter { it.isNotEmpty() } ?: emptyList()

            FileAiAnalysis(
                riskLevel = FileRiskLevel.valueOf(riskLevelStr.uppercase()),
                analysis = analysis,
                fileHash = fileHash,
                behaviors = behaviors
            )
        } catch (e: Exception) {
            FileAiAnalysis(
                riskLevel = FileRiskLevel.UNKNOWN,
                analysis = "Failed to parse AI analysis",
                fileHash = fileHash
            )
        }
    }

    private fun createHeuristicAnalysis(fileName: String, extension: String, size: Long, fileHash: String?): FileAiAnalysis {
        val (risk, analysis, behaviors) = when {
            extension in setOf("apk", "exe", "bat", "sh") ->
                Triple(FileRiskLevel.HIGH, "Executable file type detected. Manual review recommended.", listOf("Executable format", "Potential code execution"))
            fileName.contains("hack", true) || fileName.contains("crack", true) || fileName.contains("keygen", true) ->
                Triple(FileRiskLevel.CRITICAL, "Suspicious naming pattern detected. High risk file.", listOf("Suspicious naming", "Potential malware indicator"))
            size > 500 * 1024 * 1024 ->
                Triple(FileRiskLevel.MEDIUM, "Unusually large file. Verify source.", listOf("Large file size"))
            else ->
                Triple(FileRiskLevel.LOW, "No obvious risk indicators found. Appears safe.", listOf("No suspicious patterns"))
        }

        return FileAiAnalysis(risk, analysis, fileHash, behaviors)
    }
}
