package com.aiphoneguardian.app.data.repository

import com.aiphoneguardian.app.data.local.dao.ChatMessageDao
import com.aiphoneguardian.app.data.local.entity.ChatMessageEntity
import com.aiphoneguardian.app.data.remote.api.GeminiApiService
import com.aiphoneguardian.app.data.remote.model.GeminiContent
import com.aiphoneguardian.app.data.remote.model.GeminiPart
import com.aiphoneguardian.app.data.remote.model.GeminiRequest
import com.aiphoneguardian.app.domain.model.ChatMessage
import com.aiphoneguardian.app.domain.model.UserSubscription
import com.aiphoneguardian.app.domain.model.UserTier
import com.aiphoneguardian.app.domain.repository.ChatRepository
import com.aiphoneguardian.app.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
    private val geminiApiService: GeminiApiService,
    private val subscriptionRepository: SubscriptionRepository
) : ChatRepository {

    companion object {
        // API key from BuildConfig - set GEMINI_API_KEY in local.properties
        val GEMINI_API_KEY get() = com.aiphoneguardian.app.BuildConfig.GEMINI_API_KEY
    }

    override fun getChatHistory(): Flow<List<ChatMessage>> {
        return chatMessageDao.getAllMessages().map { entities ->
            entities.map { it.toChatMessage() }
        }
    }

    override suspend fun sendMessage(message: String, language: String): Flow<ChatMessage> = flow {
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = message,
            isFromUser = true,
            language = language
        )
        saveMessage(userMessage)
        emit(userMessage)

        subscriptionRepository.incrementChatCount()

        val aiResponse = try {
            val systemPrompt = if (language == "ar") {
                "أنت مساعد أمني خبير. أجب على أسئلة الأمن السيبراني والحماية باللغة العربية."
            } else {
                "You are an expert cybersecurity assistant. Answer security and protection questions concisely and professionally."
            }

            val prompt = "$systemPrompt\n\nUser question: $message"

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            )

            val response = geminiApiService.generateContent(GEMINI_API_KEY, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: if (language == "ar") "عذراً، لم أتمكن من معالجة طلبك." else "Sorry, I couldn't process your request."

            ChatMessage(
                id = UUID.randomUUID().toString(),
                content = responseText,
                isFromUser = false,
                language = language
            )
        } catch (e: Exception) {
            ChatMessage(
                id = UUID.randomUUID().toString(),
                content = if (language == "ar") "حدث خطأ. يرجى المحاولة مرة أخرى." else "An error occurred. Please try again.",
                isFromUser = false,
                isError = true,
                language = language
            )
        }

        saveMessage(aiResponse)
        emit(aiResponse)
    }

    override suspend fun clearHistory() {
        chatMessageDao.clearAll()
    }

    override fun getRemainingMessagesCount(): Int {
        // Use subscription flow in ViewModel for accurate count
        // Default free limit is 5 messages/day
        return com.aiphoneguardian.app.domain.model.UserSubscription(
            com.aiphoneguardian.app.domain.model.UserTier.Free
        ).dailyChatLimit
    }

    override suspend fun incrementMessageCount() {
        subscriptionRepository.incrementChatCount()
    }

    override fun canSendMessage(): Boolean {
        // This is checked in ViewModel
        return true
    }

    override suspend fun addExtraMessageFromAd() {
        // Implementation for rewarded ad
    }

    override suspend fun saveMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message.toEntity())
    }

    private fun ChatMessageEntity.toChatMessage(): ChatMessage {
        return ChatMessage(
            id = id,
            content = content,
            isFromUser = isFromUser,
            timestamp = timestamp,
            isError = isError,
            language = language
        )
    }

    private fun ChatMessage.toEntity(): ChatMessageEntity {
        return ChatMessageEntity(
            id = id,
            content = content,
            isFromUser = isFromUser,
            timestamp = timestamp,
            isError = isError,
            language = language
        )
    }
}
