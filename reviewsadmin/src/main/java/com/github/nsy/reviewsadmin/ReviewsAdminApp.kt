package com.github.nsy.reviewsadmin

import android.app.Application
import android.util.Log
import com.github.naz013.logging.FirebaseLogger
import com.github.naz013.logging.Logger
import com.github.naz013.logging.LoggerProvider
import com.github.naz013.logging.initLogging
import com.github.naz013.reviews.ReviewSdk
import com.github.naz013.reviews.config.SecondaryFirebaseConfig
import com.github.naz013.reviews.reviewsKoinModule
import com.github.nsy.reviewsadmin.di.reviewsAdminModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application class for Reviews Admin app.
 *
 * Initializes Koin dependency injection and required libraries.
 */
class ReviewsAdminApp : Application() {

  override fun onCreate() {
    super.onCreate()
    initLogging(
      isDebug = true,
      loggerProvider = object : LoggerProvider {
        override fun info(tag: String, message: String) {
          Log.i(tag, message)
        }

        override fun debug(tag: String, message: String) {
          Log.d(tag, message)
        }

        override fun error(tag: String, message: String) {
          Log.e(tag, message)
        }

        override fun error(tag: String, message: String, throwable: Throwable) {
          Log.e(tag, message, throwable)
        }

        override fun warning(tag: String, message: String) {
          Log.w(tag, message)
        }

        override fun warning(tag: String, message: String, throwable: Throwable) {
          Log.w(tag, message, throwable)
        }
      },
      firebaseLogger = object : FirebaseLogger {
        override fun logEvent(event: String) {
          // No-op
        }

        override fun logEvent(
          event: String,
          params: Map<String, Any>
        ) {
          // No-op
        }
      }
    )

    // Initialize Reviews SDK with Firebase configuration
    val firebaseConfig = SecondaryFirebaseConfig(
      projectId = BuildConfig.REVIEWS_PROJECT_ID,
      applicationId = BuildConfig.REVIEWS_APP_ID,
      apiKey = BuildConfig.REVIEWS_API_KEY,
      storageBucket = BuildConfig.REVIEWS_STORAGE_BUCKET
    )

    ReviewSdk.initialize(
      context = this,
      config = firebaseConfig,
      enableAppCheck = false
    ).fold(
      onSuccess = {
        Logger.i("App", "✅ Reviews Firebase initialized")
      },
      onFailure = { error ->
        Logger.e("App", "❌ Reviews init failed", error)
      }
    )

    // Initialize Koin
    startKoin {
      androidLogger()
      androidContext(this@ReviewsAdminApp)
      modules(
        reviewsAdminModule,
        reviewsKoinModule
      )
    }

    Logger.i(TAG, "Reviews Admin App initialized")
  }

  companion object {
    private const val TAG = "ReviewsAdminApp"
  }
}
