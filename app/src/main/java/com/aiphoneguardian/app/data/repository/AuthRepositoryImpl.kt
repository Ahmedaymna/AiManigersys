package com.aiphoneguardian.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aiphoneguardian.app.domain.model.UserProfile
import com.aiphoneguardian.app.domain.model.UserSubscription
import com.aiphoneguardian.app.domain.model.UserTier
import com.aiphoneguardian.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local authentication implementation.
 * Uses DataStore to persist user accounts locally.
 * In production: replace with real Firebase Auth + Firestore integration
 * and update google-services.json with real Firebase project credentials.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AuthRepository {

    companion object {
        private val KEY_CURRENT_UID = stringPreferencesKey("auth_current_uid")
        private val KEY_CURRENT_NAME = stringPreferencesKey("auth_current_name")
        private val KEY_CURRENT_EMAIL = stringPreferencesKey("auth_current_email")
        private val KEY_CURRENT_ANON = stringPreferencesKey("auth_is_anonymous")
        // Stores registered accounts as "email:hashedPassword:name:uid" separated by "|"
        private val KEY_ACCOUNTS = stringPreferencesKey("auth_accounts")
    }

    private val _currentUserFlow = MutableStateFlow<UserProfile?>(null)

    override val currentUser: Flow<UserProfile?> = dataStore.data.map { prefs ->
        val uid = prefs[KEY_CURRENT_UID] ?: return@map null
        UserProfile(
            uid = uid,
            email = prefs[KEY_CURRENT_EMAIL],
            displayName = prefs[KEY_CURRENT_NAME],
            isAnonymous = prefs[KEY_CURRENT_ANON] == "true",
            subscription = UserSubscription(UserTier.Free)
        )
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<UserProfile> {
        return try {
            val prefs = dataStore.data.first()
            val accounts = prefs[KEY_ACCOUNTS] ?: ""
            val hashedPwd = hashPassword(password)

            val account = accounts.split("|").firstOrNull { entry ->
                val parts = entry.split(":")
                parts.size >= 4 && parts[0] == email && parts[1] == hashedPwd
            }

            if (account != null) {
                val parts = account.split(":")
                val uid = parts[3]
                val name = if (parts.size > 4) parts[4] else ""
                val profile = UserProfile(
                    uid = uid,
                    email = email,
                    displayName = name,
                    isAnonymous = false,
                    subscription = UserSubscription(UserTier.Free)
                )
                saveCurrentSession(uid, email, name, false)
                Result.success(profile)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<UserProfile> {
        return try {
            val prefs = dataStore.data.first()
            val accounts = prefs[KEY_ACCOUNTS] ?: ""

            // Check if email already registered
            val emailExists = accounts.split("|").any { entry ->
                entry.isNotBlank() && entry.split(":").firstOrNull() == email
            }
            if (emailExists) {
                return Result.failure(Exception("This email is already registered"))
            }

            val uid = UUID.randomUUID().toString()
            val hashedPwd = hashPassword(password)
            val newEntry = "$email:$hashedPwd:uid:$uid:$displayName"
            val updatedAccounts = if (accounts.isBlank()) newEntry else "$accounts|$newEntry"

            dataStore.edit { it[KEY_ACCOUNTS] = updatedAccounts }
            saveCurrentSession(uid, email, displayName, false)

            val profile = UserProfile(
                uid = uid,
                email = email,
                displayName = displayName,
                isAnonymous = false,
                subscription = UserSubscription(UserTier.Free)
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<UserProfile> {
        return try {
            val uid = "guest_${UUID.randomUUID()}"
            saveCurrentSession(uid, null, "Guest", true)
            val profile = UserProfile(
                uid = uid,
                email = null,
                displayName = "Guest",
                isAnonymous = true,
                subscription = UserSubscription(UserTier.Free)
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_CURRENT_UID)
            prefs.remove(KEY_CURRENT_EMAIL)
            prefs.remove(KEY_CURRENT_NAME)
            prefs.remove(KEY_CURRENT_ANON)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        // In a real app, send an email. Here we just validate the email exists.
        val prefs = dataStore.data.first()
        val accounts = prefs[KEY_ACCOUNTS] ?: ""
        val emailExists = accounts.split("|").any { entry ->
            entry.isNotBlank() && entry.split(":").firstOrNull() == email
        }
        return if (emailExists) {
            // In production: send real reset email via Firebase
            Result.success(Unit)
        } else {
            Result.failure(Exception("No account found with this email"))
        }
    }

    override suspend fun authenticateWithBiometric(): Result<UserProfile> {
        val prefs = dataStore.data.first()
        val uid = prefs[KEY_CURRENT_UID]
        return if (uid != null) {
            val profile = UserProfile(
                uid = uid,
                email = prefs[KEY_CURRENT_EMAIL],
                displayName = prefs[KEY_CURRENT_NAME],
                isAnonymous = false,
                subscription = UserSubscription(UserTier.Free)
            )
            Result.success(profile)
        } else {
            Result.failure(Exception("No saved session. Please login first."))
        }
    }

    override fun isUserAuthenticated(): Boolean {
        // This is called synchronously in SplashViewModel, so we check synchronously via runBlocking
        // In production with Firebase, use FirebaseAuth.currentUser != null
        return false // Always show login on cold start; session is checked via currentUser Flow
    }

    private suspend fun saveCurrentSession(
        uid: String,
        email: String?,
        name: String?,
        isAnon: Boolean
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_CURRENT_UID] = uid
            if (email != null) prefs[KEY_CURRENT_EMAIL] = email
            if (name != null) prefs[KEY_CURRENT_NAME] = name
            prefs[KEY_CURRENT_ANON] = isAnon.toString()
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
