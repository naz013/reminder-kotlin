package com.github.naz013.reviews.config

/**
 * Example configuration values for secondary Firebase setup.
 *
 * IMPORTANT: DO NOT USE THESE VALUES IN PRODUCTION!
 * These are placeholders to show you where to put your actual values.
 *
 * Replace these with your actual Firebase project values from:
 * Firebase Console → Project Settings → General → Your apps
 */
object SecondaryFirebaseConfigExample {

  /**
   * Example for creating a secondary Firebase configuration.
   *
   * Steps to get your values:
   * 1. Go to Firebase Console: https://console.firebase.google.com/
   * 2. Select your reviews project
   * 3. Go to Project Settings (gear icon)
   * 4. Scroll to "Your apps" section
   * 5. Select your app (Free or Pro)
   * 6. Copy the configuration values
   */
  fun createExample(): SecondaryFirebaseConfig {
    return SecondaryFirebaseConfig(
      // Your Firebase project ID (e.g., "reminder-reviews-2024")
      projectId = "YOUR_PROJECT_ID_HERE",

      // Your Firebase application ID (format: "1:123456789:android:abc123")
      // Different for Free and Pro versions!
      applicationId = "YOUR_APPLICATION_ID_HERE",

      // Your Firebase API key (e.g., "AIzaSyA...")
      apiKey = "YOUR_API_KEY_HERE",

      // Your Firebase Storage bucket (e.g., "reminder-reviews-2024.appspot.com")
      storageBucket = "YOUR_STORAGE_BUCKET_HERE",

      // Optional: Realtime Database URL
      databaseUrl = null
    )
  }

  /**
   * Example for Free version configuration.
   * Package name: com.cray.software.justreminder
   */
  fun createFreeVersionExample(): SecondaryFirebaseConfig {
    return SecondaryFirebaseConfig(
      projectId = "reminder-reviews-2024",
      applicationId = "1:123456789:android:abc123", // Free version app ID
      apiKey = "AIzaSyA...",
      storageBucket = "reminder-reviews-2024.appspot.com"
    )
  }

  /**
   * Example for Pro version configuration.
   * Package name: com.cray.software.justreminderpro
   */
  fun createProVersionExample(): SecondaryFirebaseConfig {
    return SecondaryFirebaseConfig(
      projectId = "reminder-reviews-2024",
      applicationId = "1:987654321:android:def456", // Pro version app ID
      apiKey = "AIzaSyA...",
      storageBucket = "reminder-reviews-2024.appspot.com"
    )
  }
}

