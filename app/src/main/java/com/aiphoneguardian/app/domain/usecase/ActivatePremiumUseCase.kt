package com.aiphoneguardian.app.domain.usecase

import com.aiphoneguardian.app.domain.repository.SubscriptionRepository
import javax.inject.Inject

class ActivatePremiumUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(code: String): Boolean {
        return subscriptionRepository.activatePremium(code)
    }
}
