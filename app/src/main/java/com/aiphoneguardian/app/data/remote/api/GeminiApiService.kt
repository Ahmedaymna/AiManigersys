package com.aiphoneguardian.app.data.remote.api

import com.aiphoneguardian.app.data.remote.model.GeminiRequest
import com.aiphoneguardian.app.data.remote.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("v1beta/models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
