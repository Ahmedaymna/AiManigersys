package com.aiphoneguardian.app.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.aiphoneguardian.app.data.local.dao.ScanResultDao
import com.aiphoneguardian.app.data.local.entity.ScanResultEntity
import com.aiphoneguardian.app.data.remote.api.GeminiApiService
import com.aiphoneguardian.app.data.remote.model.GeminiContent
import com.aiphoneguardian.app.data.remote.model.GeminiPart
import com.aiphoneguardian.app.data.remote.model.GeminiRequest
import com.aiphoneguardian.app.domain.model.AiThreatReport
import com.aiphoneguardian.app.domain.repository.ScanRepository
import com.aiphoneguardian.app.domain.model.ScanResult
import com.aiphoneguardian.app.domain.model.ScanType
import com.aiphoneguardian.app.domain.model.ThreatItem
import com.aiphoneguardian.app.domain.model.ThreatLevel
import com.aiphoneguardian.app.domain.model.ThreatType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanResultDao: ScanResultDao,
    private val geminiApiService: GeminiApiService,
    private val gson: Gson
) : ScanRepository {

    companion object {
        // API key loaded from BuildConfig (set in local.properties, never hardcode)
        val GEMINI_API_KEY get() = com.aiphoneguardian.app.BuildConfig.GEMINI_API_KEY
    }

    override suspend fun performQuickScan(): Flow<ScanResult> = flow {
        val scanId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        emit(createProgressResult(scanId, 10, ScanType.QUICK))
        val appsThreats = scanApps()

        emit(createProgressResult(scanId, 40, ScanType.QUICK, appsThreats))
        val filesThreats = scanFiles().take(50)

        emit(createProgressResult(scanId, 70, ScanType.QUICK, appsThreats + filesThreats))
        val processThreats = scanProcesses().take(20)

        val allThreats = (appsThreats + filesThreats + processThreats)
        val overallStatus = determineOverallStatus(allThreats)

        emit(createProgressResult(scanId, 90, ScanType.QUICK, allThreats))

        val aiReport = analyzeWithAI(allThreats, "en")
        val duration = System.currentTimeMillis() - startTime

        val result = ScanResult(
            id = scanId,
            scanType = ScanType.QUICK,
            timestamp = System.currentTimeMillis(),
            overallStatus = overallStatus,
            threats = allThreats,
            appsScanned = appsThreats.size,
            filesScanned = filesThreats.size,
            durationMs = duration,
            aiReport = aiReport
        )

        saveScanResult(result)
        emit(result)
    }.flowOn(Dispatchers.IO)

    override suspend fun performFullScan(): Flow<ScanResult> = flow {
        val scanId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        emit(createProgressResult(scanId, 5, ScanType.FULL))
        val appsThreats = scanApps()
        emit(createProgressResult(scanId, 25, ScanType.FULL, appsThreats))

        val filesThreats = scanFiles()
        emit(createProgressResult(scanId, 50, ScanType.FULL, appsThreats + filesThreats))

        val processThreats = scanProcesses()
        emit(createProgressResult(scanId, 70, ScanType.FULL, appsThreats + filesThreats + processThreats))

        val networkThreats = scanNetwork()
        val allThreats = (appsThreats + filesThreats + processThreats + networkThreats)
        val overallStatus = determineOverallStatus(allThreats)

        emit(createProgressResult(scanId, 85, ScanType.FULL, allThreats))

        val aiReport = analyzeWithAI(allThreats, "en")
        val duration = System.currentTimeMillis() - startTime

        val result = ScanResult(
            id = scanId,
            scanType = ScanType.FULL,
            timestamp = System.currentTimeMillis(),
            overallStatus = overallStatus,
            threats = allThreats,
            appsScanned = appsThreats.size,
            filesScanned = filesThreats.size,
            durationMs = duration,
            aiReport = aiReport
        )

        saveScanResult(result)
        emit(result)
    }.flowOn(Dispatchers.IO)

    override suspend fun scanApps(): List<ThreatItem> {
        val pm = context.packageManager
        val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            pm.getInstalledApplications(0)
        }

        return apps.mapNotNull { app ->
            val threats = mutableListOf<ThreatItem>()
            val appName = pm.getApplicationLabel(app).toString()

            if (!app.sourceDir.startsWith("/system/") && app.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                if (hasSuspiciousPermissions(pm, app.packageName)) {
                    threats.add(ThreatItem(
                        id = UUID.randomUUID().toString(),
                        name = appName,
                        type = ThreatType.PRIVACY_RISK,
                        severity = ThreatLevel.WARNING,
                        description = "$appName has access to sensitive permissions",
                        recommendation = "Review permissions for $appName",
                        packageName = app.packageName
                    ))
                }
            }
            threats
        }.flatten()
    }

    private fun hasSuspiciousPermissions(pm: PackageManager, packageName: String): Boolean {
        return try {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS))
            } else {
                pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            }?.requestedPermissions ?: return false

            val suspiciousPerms = setOf(
                "android.permission.READ_CONTACTS",
                "android.permission.RECORD_AUDIO",
                "android.permission.CAMERA",
                "android.permission.READ_SMS",
                "android.permission.SEND_SMS",
                "android.permission.READ_PHONE_STATE",
                "android.permission.ACCESS_FINE_LOCATION"
            )
            permissions.any { it in suspiciousPerms }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun scanFiles(): List<ThreatItem> {
        val threats = mutableListOf<ThreatItem>()
        val suspiciousPaths = listOf(
            context.getExternalFilesDir(null)?.parentFile?.parentFile?.absolutePath + "/Download",
            context.cacheDir.absolutePath
        )

        suspiciousPaths.forEach { path ->
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (isSuspiciousFile(file)) {
                        threats.add(ThreatItem(
                            id = UUID.randomUUID().toString(),
                            name = file.name,
                            type = ThreatType.SUSPICIOUS_FILE,
                            severity = ThreatLevel.WARNING,
                            description = "Suspicious file detected: ${file.name}",
                            recommendation = "Review and scan this file with AI",
                            filePath = file.absolutePath
                        ))
                    }
                }
            }
        }
        return threats
    }

    private fun isSuspiciousFile(file: File): Boolean {
        val suspiciousExtensions = setOf("apk", "exe", "bat", "sh", "bin", "dex")
        val ext = file.extension.lowercase()
        return suspiciousExtensions.contains(ext) && file.length() > 1024
    }

    override suspend fun scanProcesses(): List<ThreatItem> {
        val threats = mutableListOf<ThreatItem>()
        try {
            val process = Runtime.getRuntime().exec("ps -A")
            process.inputStream.bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val parts = line.trim().split(Regex("\\s+"))
                    if (parts.size >= 9) {
                        val processName = parts[8]
                        if (isSuspiciousProcess(processName)) {
                            threats.add(ThreatItem(
                                id = UUID.randomUUID().toString(),
                                name = processName,
                                type = ThreatType.MALWARE,
                                severity = ThreatLevel.CRITICAL,
                                description = "Suspicious process detected: $processName",
                                recommendation = "Investigate and terminate this process"
                            ))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Process scanning may fail on some devices
        }
        return threats
    }

    private fun isSuspiciousProcess(name: String): Boolean {
        val suspiciousPatterns = listOf("miner", "trojan", "malware", "spy", "keylogger")
        return suspiciousPatterns.any { name.lowercase().contains(it) }
    }

    override suspend fun scanNetwork(): List<ThreatItem> {
        val threats = mutableListOf<ThreatItem>()
        try {
            val process = Runtime.getRuntime().exec("netstat -tn")
            process.inputStream.bufferedReader().useLines { lines ->
                lines.drop(2).forEach { line ->
                    val parts = line.trim().split(Regex("\\s+"))
                    if (parts.size >= 4) {
                        val foreignAddress = parts[4]
                        if (isSuspiciousConnection(foreignAddress)) {
                            threats.add(ThreatItem(
                                id = UUID.randomUUID().toString(),
                                name = "Suspicious network connection",
                                type = ThreatType.NETWORK_INTRUSION,
                                severity = ThreatLevel.WARNING,
                                description = "Connection to suspicious address: $foreignAddress",
                                recommendation = "Block this connection and scan your device"
                            ))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Network scanning may require root
        }
        return threats
    }

    private fun isSuspiciousConnection(address: String): Boolean {
        val suspiciousPorts = setOf("4444", "5555", "6666", "9999")
        return suspiciousPorts.any { address.endsWith(":$it") }
    }

    override suspend fun getScanHistory(): List<ScanResult> {
        return scanResultDao.getLastScanResult()?.let { listOf(it.toScanResult()) } ?: emptyList()
    }

    override suspend fun getLastScanResult(): ScanResult? {
        return scanResultDao.getLastScanResult()?.toScanResult()
    }

    override suspend fun clearScanHistory() {
        scanResultDao.clearAll()
    }

    override suspend fun analyzeWithAI(threats: List<ThreatItem>, language: String): AiThreatReport {
        return try {
            val prompt = buildString {
                appendLine("You are an expert cybersecurity analyst. Analyze the following security scan results and provide a comprehensive threat assessment.")
                appendLine("Detected ${threats.size} potential threats:")
                threats.forEachIndexed { index, threat ->
                    appendLine("${index + 1}. ${threat.name} - Severity: ${threat.severity}, Type: ${threat.type}")
                    appendLine("   Description: ${threat.description}")
                }
                appendLine("\nProvide your response in the following JSON format:")
                appendLine("{")
                appendLine("  \"summary\": \"Brief summary of findings\",")
                appendLine("  \"detailedAnalysis\": \"Detailed analysis of threats\",")
                appendLine("  \"recommendedActions\": [\"Action 1\", \"Action 2\"],")
                appendLine("  \"confidenceScore\": 0.85")
                appendLine("}")
                if (language == "ar") {
                    appendLine("Respond in Arabic language.")
                }
            }

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            )

            val response = geminiApiService.generateContent(GEMINI_API_KEY, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (responseText != null) {
                parseAIResponse(responseText, language)
            } else {
                createDefaultReport(threats, language)
            }
        } catch (e: Exception) {
            createDefaultReport(threats, language)
        }
    }

    private fun parseAIResponse(responseText: String, language: String): AiThreatReport {
        return try {
            val jsonMap: Map<String, Any> = gson.fromJson(
                responseText.trim(),
                object : TypeToken<Map<String, Any>>() {}.type
            )
            AiThreatReport(
                summary = jsonMap["summary"] as? String ?: "Analysis completed",
                detailedAnalysis = jsonMap["detailedAnalysis"] as? String ?: "Threats have been identified and assessed.",
                recommendedActions = (jsonMap["recommendedActions"] as? List<*>)?.filterIsInstance<String>() ?: listOf("Review detected threats"),
                confidenceScore = (jsonMap["confidenceScore"] as? Number)?.toFloat() ?: 0.8f,
                isInArabic = language == "ar"
            )
        } catch (e: Exception) {
            createDefaultReport(emptyList(), language)
        }
    }

    private fun createDefaultReport(threats: List<ThreatItem>, language: String): AiThreatReport {
        return if (language == "ar") {
            AiThreatReport(
                summary = if (threats.isEmpty()) "لم يتم اكتشاف تهديدات" else "تم اكتشاف ${threats.size} تهديد",
                detailedAnalysis = "تم إكمال الفحص الأمني. ${if (threats.isNotEmpty()) "يرجى مراجعة التهديدات المكتشفة واتخاذ الإجراءات المناسبة." else "لا توجد تهديدات معروفة في الوقت الحالي."}",
                recommendedActions = if (threats.isNotEmpty()) {
                    listOf("مراجعة التطبيقات المشبوهة", "تحديث نظام التشغيل", "تشغيل فحص كامل")
                } else {
                    listOf("الاستمرار في المراقبة", "تحديث التطبيقات بانتظام")
                },
                confidenceScore = 0.85f,
                isInArabic = true
            )
        } else {
            AiThreatReport(
                summary = if (threats.isEmpty()) "No threats detected" else "Found ${threats.size} threats",
                detailedAnalysis = "Security scan completed. ${if (threats.isNotEmpty()) "Please review detected threats and take appropriate actions." else "No known threats at this time."}",
                recommendedActions = if (threats.isNotEmpty()) {
                    listOf("Review suspicious apps", "Update operating system", "Run a full scan")
                } else {
                    listOf("Continue monitoring", "Keep apps updated")
                },
                confidenceScore = 0.85f,
                isInArabic = false
            )
        }
    }

    private fun determineOverallStatus(threats: List<ThreatItem>): ThreatLevel {
        return when {
            threats.any { it.severity == ThreatLevel.CRITICAL } -> ThreatLevel.CRITICAL
            threats.any { it.severity == ThreatLevel.WARNING } -> ThreatLevel.WARNING
            else -> ThreatLevel.SAFE
        }
    }

    private fun createProgressResult(
        id: String,
        progress: Int,
        scanType: ScanType,
        threats: List<ThreatItem> = emptyList()
    ): ScanResult {
        return ScanResult(
            id = id,
            scanType = scanType,
            timestamp = System.currentTimeMillis(),
            overallStatus = ThreatLevel.SAFE,
            threats = threats,
            appsScanned = 0,
            filesScanned = 0,
            durationMs = progress.toLong()
        )
    }

    private suspend fun saveScanResult(result: ScanResult) {
        val entity = ScanResultEntity(
            id = result.id,
            scanType = result.scanType.name,
            timestamp = result.timestamp,
            overallStatus = result.overallStatus.name,
            threatsJson = gson.toJson(result.threats),
            appsScanned = result.appsScanned,
            filesScanned = result.filesScanned,
            durationMs = result.durationMs,
            aiReportJson = result.aiReport?.let { gson.toJson(it) }
        )
        scanResultDao.insertScanResult(entity)
    }

    private fun ScanResultEntity.toScanResult(): ScanResult {
        val threatList: List<ThreatItem> = try {
            gson.fromJson(threatsJson, object : TypeToken<List<ThreatItem>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
        val report: AiThreatReport? = aiReportJson?.let {
            try {
                gson.fromJson(it, AiThreatReport::class.java)
            } catch (e: Exception) {
                null
            }
        }
        return ScanResult(
            id = id,
            scanType = ScanType.valueOf(scanType),
            timestamp = timestamp,
            overallStatus = ThreatLevel.valueOf(overallStatus),
            threats = threatList,
            appsScanned = appsScanned,
            filesScanned = filesScanned,
            durationMs = durationMs,
            aiReport = report
        )
    }
}
