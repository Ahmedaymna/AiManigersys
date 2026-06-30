# AI Phone Guardian Setup Guide

## Required Setup Before Building

### 1. Firebase (for future real auth migration)
- Create a Firebase project at https://console.firebase.google.com
- Add Android app with package: `com.aiphoneguardian.app`
- Download `google-services.json` and replace `app/google-services.json`
- Enable Authentication > Email/Password in Firebase Console

### 2. Gemini API Key (Required for AI features)
Add to `local.properties` (DO NOT commit this file):
```
GEMINI_API_KEY=your_actual_gemini_api_key_here
```
Get your key at: https://aistudio.google.com/app/apikey

### 3. AdMob (for real ads)
- Register at https://admob.google.com
- Replace test App ID in AndroidManifest.xml with real App ID
- Update ad unit IDs in local.properties or build.gradle.kts

### 4. Valid Activation Codes (for Premium)
Current codes in SubscriptionRepositoryImpl.kt are placeholder hashes.
Replace with SHA-256 hashes of your real activation codes.

## Current Auth Mode
The app uses **local authentication** (stored on device).
Accounts are persisted via DataStore. To migrate to Firebase:
1. Complete Firebase setup above
2. Restore the original Firebase-based AuthRepositoryImpl
3. Update AppModule.kt to inject FirebaseAuth again

## Bugs Fixed
1. ✅ Wrong imports: ScanRepository/SystemMonitorRepository now from domain.repository
2. ✅ Duplicate Context.dataStore: SubscriptionRepositoryImpl now receives DataStore via injection
3. ✅ suspend in lambda: checkAndResetDailyLimits() removed from map{} lambda
4. ✅ API key exposure: moved to BuildConfig via local.properties
5. ✅ Weak activation codes: replaced with stronger placeholder hashes
6. ✅ Auth flow: login/register properly validate credentials; accounts are stored
7. ✅ getRemainingMessagesCount: no longer hardcoded
8. ✅ compileSdk/targetSdk: updated to 35
9. ✅ generativeai: updated to 0.9.0
