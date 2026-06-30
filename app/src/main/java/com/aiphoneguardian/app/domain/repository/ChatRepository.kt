package com.aiphoneguardian.app.domain.repository

import com.aiphoneguardian.app.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatHistory(): Flow<List<ChatMessage>>
    suspend fun sendMessage(message: String, language: String): Flow<ChatMessage>
    suspend fun clearHistory()
    fun getRemainingMessagesCount(): Int
    suspend fun incrementMessageCount()
    fun canSendMessage(): Boolean
    suspend fun addExtraMessageFromAd()
    suspend fun saveMessage(message: ChatMessage)
}
