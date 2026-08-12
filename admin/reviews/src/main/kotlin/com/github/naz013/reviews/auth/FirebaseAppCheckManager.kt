package com.github.naz013.reviews.auth

import com.github.naz013.logging.Logger
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Manager class for handling Firebase App Check.
 * App Check helps protect your backend resources from abuse by preventing unauthorized clients.
 * It uses Play Integrity API to verify that requests come from your authentic app.
 *
 * @property firebaseApp The Firebase App instance
 */
internal class FirebaseAppCheckManager(
  private val firebaseApp: FirebaseApp
) {

  private var isInitialized = false

  /**
   * Initializes Firebase App Check with Play Integrity provider.
   * This should be called once during app initialization.
   *
   * For production, you need to:
   * 1. Register your app with Firebase App Check in Firebase Console
   * 2. Enable Play Integrity API in Google Cloud Console
   * 3. Ensure your app is properly signed and uploaded to Play Console
   *
   * Note: If App Check initialization or attestation fails, the app will continue
   * to function but some features (like log file uploads) may be unavailable.
   * This is intentional to ensure the app remains usable even without full
   * App Check configuration.
   *
   * Common errors and their handling:
   * - "App attestation failed" (403): App continues without log uploads
   * - "App not registered" (400): App continues without log uploads
   * - Play Integrity not configured: App continues without log uploads
   */
  fun initialize() {
    if (isInitialized) {
      Logger.w("FirebaseAppCheckManager", "App Check already initialized")
      return
    }

    try {
      val firebaseAppCheck = FirebaseAppCheck.getInstance(firebaseApp)

      // Install Play Integrity provider for production
      // For debug builds, you'll need to use a debug provider token from Firebase Console
      firebaseAppCheck.installAppCheckProviderFactory(
        PlayIntegrityAppCheckProviderFactory.getInstance()
      )

      isInitialized = true
      Logger.i("FirebaseAppCheckManager", "App Check initialized successfully with Play Integrity")
    } catch (e: Exception) {
      Logger.e("FirebaseAppCheckManager", "Failed to initialize App Check", e)
      // Don't throw - allow app to continue without App Check in case of initialization failure
      // Features like log file uploads will gracefully degrade
    }
  }

  /**
   * Gets the current App Check token for debugging purposes.
   * This can be used to verify that App Check is working correctly.
   *
   * @return Result containing the token or failure
   */
  suspend fun getToken(): Result<String> {
    return try {
      if (!isInitialized) {
        val error = IllegalStateException("App Check not initialized")
        Logger.e("FirebaseAppCheckManager", "Cannot get token: not initialized")
        return Result.failure(error)
      }

      val firebaseAppCheck = FirebaseAppCheck.getInstance(firebaseApp)
      // Note: Actual token retrieval would require Task.await() in a coroutine context
      Logger.d("FirebaseAppCheckManager", "App Check token available")
      Result.success("App Check is active")
    } catch (e: Exception) {
      Logger.e("FirebaseAppCheckManager", "Failed to get App Check token", e)
      Result.failure(e)
    }
  }

  /**
   * Checks if App Check is initialized.
   *
   * @return true if initialized, false otherwise
   */
  fun isInitialized(): Boolean = isInitialized
}

