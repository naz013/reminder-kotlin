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
        override fun info(message: String) {
          Log.i("Logger", message)
        }

        override fun debug(message: String) {
          Log.d("Logger", message)
        }

        override fun error(message: String) {
          Log.e("Logger", message)
        }

        override fun error(message: String, throwable: Throwable) {
          Log.e("Logger", message, throwable)
        }

        override fun warning(message: String) {
          Log.w("Logger", message)
        }

        override fun warning(message: String, throwable: Throwable) {
          Log.w("Logger", message, throwable)
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

