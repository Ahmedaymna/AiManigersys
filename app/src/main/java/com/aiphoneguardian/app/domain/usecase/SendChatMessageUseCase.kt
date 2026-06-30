package com.aiphoneguardian.app.domain.usecase

import com.aiphoneguardian.app.domain.model.ChatMessage
import com.aiphoneguardian.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(message: String, language: String): Flow<ChatMessage> {
        return chatRepository.sendMessage(message, language)
    }
}
