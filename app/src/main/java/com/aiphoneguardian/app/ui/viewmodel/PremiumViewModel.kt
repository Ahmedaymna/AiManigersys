package com.aiphoneguardian.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiphoneguardian.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _activationState = MutableStateFlow<ActivationState>(ActivationState.Idle)
    val activationState: StateFlow<ActivationState> = _activationState.asStateFlow()

    init {
        loadSubscription()
    }

    private fun loadSubscription() {
        viewModelScope.launch {
            subscriptionRepository.getSubscription().collect { subscription ->
                _isPremium.value = subscription.tier.isPremium
            }
        }
    }

    fun activatePremium(code: String) {
        viewModelScope.launch {
            _activationState.value = ActivationState.Loading
            val success = subscriptionRepository.activatePremium(code)
            _activationState.value = if (success) {
                _isPremium.value = true
                ActivationState.Success
            } else {
                ActivationState.Error("Invalid activation code. Please check and try again.")
            }
        }
    }

    sealed class ActivationState {
        object Idle : ActivationState()
        object Loading : ActivationState()
        object Success : ActivationState()
        data class Error(val message: String) : ActivationState()
    }
}
