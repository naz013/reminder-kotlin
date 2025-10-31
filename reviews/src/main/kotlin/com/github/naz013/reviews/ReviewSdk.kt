package com.github.naz013.reviews

import android.content.Context
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.auth.FirebaseAppCheckManager
import com.github.naz013.reviews.config.SecondaryFirebaseConfig
import com.github.naz013.reviews.firebase.SecondaryFirebaseAppManager

/**
 * SDK for initializing the Reviews module with Firebase support.
 * This SDK manages a dedicated Firebase app for consolidated reviews and logs
 * across app variants (Free and Pro versions).
 */
object ReviewSdk {

  private const val TAG = "ReviewSdk"

  /**
   * Initializes the Reviews SDK with the Firebase app for reviews.
   * The Firebase app is used for consolidated reviews and logs storage,
   * allowing both Free and Pro versions to share the same reviews database.
   *
   * @param context Application context
   * @param config Configuration for the Firebase project (required)
   * @param enableAppCheck Enable App Check for additional security (default: false)
   * @return Result indicating success or failure
   */
  fun initialize(
    context: Context,
    config: SecondaryFirebaseConfig,
    enableAppCheck: Boolean = false,
  ): Result<Unit> {
    // Validate configuration
    if (!config.isValid()) {
      val error = IllegalArgumentException("Invalid Firebase configuration provided")
      Logger.e(TAG, "Failed to initialize: invalid configuration", error)
      return Result.failure(error)
    }

    // Initialize Firebase app
    val initResult = SecondaryFirebaseAppManager.initialize(context, config)

    return initResult.fold(
      onSuccess = {
        val app = SecondaryFirebaseAppManager.getApp()
        if (app != null) {
          // Initialize App Check if enabled
          if (enableAppCheck) {
            try {
              val appCheckManager = FirebaseAppCheckManager(app)
              appCheckManager.initialize()
              Logger.i(TAG, "Reviews Firebase app initialized with App Check enabled")
            } catch (e: Exception) {
              Logger.w(TAG, "App Check initialization failed: ${e.message}, continuing without it")
              Logger.i(TAG, "Reviews Firebase app initialized (App Check disabled due to error)")
            }
          } else {
            Logger.i(TAG, "Reviews Firebase app initialized (App Check disabled)")
          }
          Result.success(Unit)
        } else {
          val error = IllegalStateException("Firebase app initialized but instance is null")
          Logger.e(TAG, error.message ?: "Unknown error")
          Result.failure(error)
        }
      },
      onFailure = { error ->
        Logger.e(TAG, "Failed to initialize Reviews Firebase app", error)
        Result.failure(error)
      }
    )
  }

  /**
   * Checks if the Reviews Firebase app is initialized.
   *
   * @return true if initialized, false otherwise
   */
  fun isInitialized(): Boolean {
    return SecondaryFirebaseAppManager.isInitialized()
  }
}
