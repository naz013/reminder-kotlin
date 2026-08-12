package com.github.naz013.reviews.config

import com.google.firebase.FirebaseOptions

/**
 * Configuration data class for secondary Firebase app initialization.
 * This configuration is used to initialize a separate Firebase project for reviews and logs,
 * consolidating data from both Free and Pro app versions.
 *
 * @property projectId The Firebase project ID (e.g., "reviews-project-123")
 * @property applicationId The Firebase application ID (format: "1:123456789:android:abc123def456")
 * @property apiKey The Firebase API key
 * @property storageBucket The Firebase Storage bucket (e.g., "reviews-project-123.appspot.com")
 * @property databaseUrl Optional Realtime Database URL
 */
data class SecondaryFirebaseConfig(
  val projectId: String,
  val applicationId: String,
  val apiKey: String,
  val storageBucket: String,
  val databaseUrl: String? = null
) {

  /**
   * Validates that all required configuration fields are provided.
   *
   * @return true if configuration is valid, false otherwise
   */
  fun isValid(): Boolean {
    return projectId.isNotBlank() &&
      applicationId.isNotBlank() &&
      apiKey.isNotBlank() &&
      storageBucket.isNotBlank()
  }

  /**
   * Converts this configuration to FirebaseOptions for app initialization.
   *
   * @return FirebaseOptions instance
   */
  fun toFirebaseOptions(): FirebaseOptions {
    return FirebaseOptions.Builder()
      .setProjectId(projectId)
      .setApplicationId(applicationId)
      .setApiKey(apiKey)
      .setStorageBucket(storageBucket)
      .apply {
        databaseUrl?.let { setDatabaseUrl(it) }
      }
      .build()
  }
}

