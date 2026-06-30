package com.aiphoneguardian.app.data.remote.model

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig = GenerationConfig(),
    val safetySettings: List<SafetySetting> = defaultSafetySettings
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

data class GeminiPart(
    val text: String
)

data class GenerationConfig(
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxOutputTokens: Int = 2048,
    val responseMimeType: String = "application/json"
)

data class SafetySetting(
    val category: String,
    val threshold: String
)

val defaultSafetySettings = listOf(
    SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_MEDIUM_AND_ABOVE"),
    SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_MEDIUM_AND_ABOVE"),
    SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_MEDIUM_AND_ABOVE"),
    SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_MEDIUM_AND_ABOVE")
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: GeminiContent?,
    val finishReason: String?,
    val index: Int?
)
