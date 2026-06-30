package com.aiphoneguardian.app.domain.repository

import com.aiphoneguardian.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<UserProfile>
    suspend fun signInAnonymously(): Result<UserProfile>
    suspend fun signOut()
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun authenticateWithBiometric(): Result<UserProfile>
    fun isUserAuthenticated(): Boolean
}
