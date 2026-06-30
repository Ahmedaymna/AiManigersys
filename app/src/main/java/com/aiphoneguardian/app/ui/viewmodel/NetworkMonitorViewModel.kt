package com.aiphoneguardian.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiphoneguardian.app.domain.model.NetworkConnection
import com.aiphoneguardian.app.domain.model.NetworkStatus
import com.aiphoneguardian.app.domain.repository.SystemMonitorRepository
import com.aiphoneguardian.app.domain.model.UserTier
import com.aiphoneguardian.app.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkMonitorViewModel @Inject constructor(
    private val systemMonitorRepository: SystemMonitorRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _networkStatus = MutableStateFlow<NetworkStatus?>(null)
    val networkStatus: StateFlow<NetworkStatus?> = _networkStatus.asStateFlow()

    private val _connections = MutableStateFlow<List<NetworkConnection>>(emptyList())
    val connections: StateFlow<List<NetworkConnection>> = _connections.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    init {
        loadNetworkStatus()
        loadConnections()
        loadSubscription()
    }

    private fun loadNetworkStatus() {
        viewModelScope.launch {
            systemMonitorRepository.getSystemStatus()
                .map { it.networkStatus }
                .collect { status ->
                    _networkStatus.value = status
                }
        }
    }

    private fun loadConnections() {
        viewModelScope.launch {
            systemMonitorRepository.getActiveConnections().collect { conns ->
                _connections.value = conns
            }
        }
    }

    private fun loadSubscription() {
        viewModelScope.launch {
            subscriptionRepository.getSubscription().collect { subscription ->
                _isPremium.value = subscription.tier.isPremium
            }
        }
    }
}
