package com.aiphoneguardian.app.domain.model

sealed class UserTier {
    data object Free : UserTier()
    data object Premium : UserTier()

    val isPremium: Boolean get() = this is Premium
    val isFree: Boolean get() = this is Free
}

data class UserSubscription(
    val tier: UserTier,
    val activationCode: String? = null,
    val activatedAt: Long? = null,
    val expiresAt: Long? = null,
    val scansToday: Int = 0,
    val chatMessagesToday: Int = 0,
    val dailyScanLimit: Int = 1,
    val dailyChatLimit: Int = 5,
    val lastResetDate: Long = System.currentTimeMillis()
)

data class UserProfile(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val subscription: UserSubscription = UserSubscription(UserTier.Free),
    val createdAt: Long = System.currentTimeMillis(),
    val preferredLanguage: String = "en",
    val isDarkTheme: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val scanSchedule: ScanSchedule = ScanSchedule.DAILY
)

enum class ScanSchedule {
    DAILY,
    WEEKLY,
    MANUAL
}
