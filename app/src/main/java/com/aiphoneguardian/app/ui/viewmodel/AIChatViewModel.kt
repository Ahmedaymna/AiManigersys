package com.aiphoneguardian.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiphoneguardian.app.domain.model.ChatMessage
import com.aiphoneguardian.app.domain.repository.ChatRepository
import com.aiphoneguardian.app.domain.repository.SettingsRepository
import com.aiphoneguardian.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _canSendMessage = MutableStateFlow(true)
    val canSendMessage: StateFlow<Boolean> = _canSendMessage.asStateFlow()

    private val _remainingMessages = MutableStateFlow(5)
    val remainingMessages: StateFlow<Int> = _remainingMessages.asStateFlow()

    private val _language = MutableStateFlow("en")

    init {
        loadMessages()
        loadSubscription()
        loadLanguage()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getChatHistory().collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    private fun loadSubscription() {
        viewModelScope.launch {
            subscriptionRepository.getSubscription().collect { subscription ->
                _isPremium.value = subscription.tier.isPremium
                _canSendMessage.value = subscription.tier.isPremium || subscription.chatMessagesToday < subscription.dailyChatLimit
                _remainingMessages.value = (subscription.dailyChatLimit - subscription.chatMessagesToday).coerceAtLeast(0)
            }
        }
    }

    private fun loadLanguage() {
        viewModelScope.launch {
            settingsRepository.getLanguage().collect { lang ->
                _language.value = lang
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                chatRepository.sendMessage(content, _language.value).collect { message ->
                    _messages.value = _messages.value + message
                }

                // Update remaining count
                val subscription = subscriptionRepository.getSubscription().first()
                _remainingMessages.value = (subscription.dailyChatLimit - subscription.chatMessagesToday).coerceAtLeast(0)
                _canSendMessage.value = _isPremium.value || _remainingMessages.value > 0
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    content = "Error: ${e.message}",
                    isFromUser = false,
                    isError = true
                )
                _messages.value = _messages.value + errorMessage
            }

            _isLoading.value = false
        }
    }

    fun watchAdForMessage() {
        viewModelScope.launch {
            chatRepository.addExtraMessageFromAd()
            _remainingMessages.value += 1
            _canSendMessage.value = true
        }
    }
}
