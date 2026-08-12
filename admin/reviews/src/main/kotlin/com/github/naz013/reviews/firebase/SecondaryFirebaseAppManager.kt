package com.github.naz013.reviews.firebase

import android.content.Context
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.config.SecondaryFirebaseConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Manager for handling secondary Firebase app instance.
 * This class manages a separate Firebase project for reviews and logs,
 * consolidating data from both Free and Pro app versions.
 *
 * The secondary Firebase app is initialized with its own configuration and
 * provides isolated Firestore, Storage, and Auth instances.
 */
internal class SecondaryFirebaseAppManager {

  companion object {
    private const val TAG = "SecondaryFirebaseAppManager"
    private const val SECONDARY_APP_NAME = "reviews_app"

    @Volatile
    private var secondaryFirebaseApp: FirebaseApp? = null

    @Volatile
    private var isInitialized = false

    /**
     * Initializes the secondary Firebase app instance with the provided configuration.
     * This should be called once during app initialization.
     *
     * @param context Application context
     * @param config Configuration for the secondary Firebase project
     * @return Result indicating success or failure
     */
    fun initialize(context: Context, config: SecondaryFirebaseConfig): Result<Unit> {
      // Early return if already initialized
      if (isInitialized) {
        Logger.w(TAG, "Secondary Firebase app already initialized")
        return Result.success(Unit)
      }

      // Validate configuration
      if (!config.isValid()) {
        val error = IllegalArgumentException("Invalid secondary Firebase configuration")
        Logger.e(TAG, "Failed to initialize: invalid configuration", error)
        return Result.failure(error)
      }

      return try {
        // Ensure we use application context
        val appContext = context.applicationContext

        // Check if app with this name already exists
        val existingApps = FirebaseApp.getApps(appContext)
        val existingApp = existingApps.find { it.name == SECONDARY_APP_NAME }

        if (existingApp != null) {
          Logger.i(TAG, "Secondary Firebase app already exists, reusing it")
          secondaryFirebaseApp = existingApp
          isInitialized = true
          return Result.success(Unit)
        }

        // Initialize new Firebase app with secondary configuration
        val firebaseOptions = config.toFirebaseOptions()
        secondaryFirebaseApp = FirebaseApp.initializeApp(
          appContext,
          firebaseOptions,
          SECONDARY_APP_NAME
        )

        isInitialized = true
        Logger.i(TAG, "Secondary Firebase app initialized successfully")
        Logger.d(TAG, "Project ID: ${config.projectId}, App Name: $SECONDARY_APP_NAME")

        Result.success(Unit)
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to initialize secondary Firebase app", e)
        isInitialized = false
        secondaryFirebaseApp = null
        Result.failure(e)
      }
    }

    /**
     * Gets the secondary FirebaseApp instance.
     *
     * @return FirebaseApp instance if initialized, null otherwise
     */
    fun getApp(): FirebaseApp? {
      if (!isInitialized) {
        Logger.w(TAG, "Secondary Firebase app not initialized")
      }
      return secondaryFirebaseApp
    }

    /**
     * Gets Firestore instance for the secondary Firebase app.
     * Configures Firestore settings on first access.
     *
     * @return FirebaseFirestore instance if initialized, null otherwise
     */
    fun getFirestore(): FirebaseFirestore? {
      return try {
        secondaryFirebaseApp?.let { app ->
          val firestore = FirebaseFirestore.getInstance(app)

          // Configure Firestore settings to prevent internal errors
          try {
            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
              // Note: Persistence is enabled by default in modern Firebase SDK
              .build()
            firestore.firestoreSettings = settings
            Logger.d(TAG, "Firestore settings configured successfully")
          } catch (settingsError: Exception) {
            // Settings might already be configured, log but continue
            Logger.d(TAG, "Firestore settings already configured: ${settingsError.message}")
          }

          Logger.d(TAG, "Firestore instance obtained")
          firestore
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to get Firestore instance: ${e.message}")
        Logger.e(TAG, "Exception details", e)
        null
      }
    }

    /**
     * Gets Storage instance for the secondary Firebase app.
     *
     * @return FirebaseStorage instance if initialized, null otherwise
     */
    fun getStorage(): FirebaseStorage? {
      return try {
        secondaryFirebaseApp?.let { app ->
          FirebaseStorage.getInstance(app)
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to get Storage instance", e)
        null
      }
    }

    /**
     * Gets Auth instance for the secondary Firebase app.
     *
     * @return FirebaseAuth instance if initialized, null otherwise
     */
    fun getAuth(): FirebaseAuth? {
      return try {
        secondaryFirebaseApp?.let { app ->
          FirebaseAuth.getInstance(app)
        }
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to get Auth instance", e)
        null
      }
    }

    /**
     * Checks if the secondary Firebase app is initialized.
     *
     * @return true if initialized, false otherwise
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * Gets the name of the secondary Firebase app.
     *
     * @return The app name
     */
    fun getAppName(): String = SECONDARY_APP_NAME

    /**
     * Deletes the secondary Firebase app instance.
     * This should only be called during testing or app cleanup.
     *
     * @return Result indicating success or failure
     */
    fun delete(): Result<Unit> {
      return try {
        secondaryFirebaseApp?.delete()
        secondaryFirebaseApp = null
        isInitialized = false
        Logger.i(TAG, "Secondary Firebase app deleted")
        Result.success(Unit)
      } catch (e: Exception) {
        Logger.e(TAG, "Failed to delete secondary Firebase app", e)
        Result.failure(e)
      }
    }
  }
}

