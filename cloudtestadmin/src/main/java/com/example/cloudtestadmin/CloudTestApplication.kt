package com.example.cloudtestadmin

import android.app.Application
import android.util.Log
import com.github.naz013.cloudapi.cloudApiModule
import com.github.naz013.logging.FirebaseLogger
import com.github.naz013.logging.LoggerProvider
import com.github.naz013.logging.initLogging
import com.github.naz013.ui.common.uiCommonModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for Cloud Test Admin.
 *
 * Initializes Koin dependency injection.
 */
class CloudTestApplication : Application() {

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

    // Initialize Koin
    startKoin {
      androidLogger(Level.ERROR)
      androidContext(this@CloudTestApplication)
      modules(
        cloudApiModule,
        uiCommonModule,
        cloudTestAdminModule
      )
    }
  }
}

