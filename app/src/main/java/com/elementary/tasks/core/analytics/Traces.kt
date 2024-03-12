package com.elementary.tasks.core.analytics

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.SuperUtil
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.ktx.Firebase
import org.slf4j.LoggerFactory

object Traces {

  var reportingEnabled: Boolean = true
  var logger: Logger = FileLogger()

  fun log(message: String) {
    logger.info(message)
  }

  fun d(message: String) {
    logger.debug(message)
  }

  fun setUpKeys(context: Context) {
    Firebase.crashlytics.setCustomKeys {
      key("has_google_play_services", SuperUtil.isGooglePlayServicesAvailable(context))
      key("is_tablet", context.resources.getBoolean(R.bool.is_tablet))
      key("is_chrome_os", Module.isChromeOs(context))
      key("has_location", Module.hasLocation(context))
      key("has_camera", Module.hasCamera(context))
      key("has_telephony", Module.hasTelephony(context))
    }
  }

  fun logEvent(message: String) {
    logger.info(message)
    if (reportingEnabled) {
      Firebase.crashlytics.log(message)
    }
  }
}

interface Logger {
  fun info(message: String)
  fun debug(message: String)
}

class FileLogger(
  private val logger: org.slf4j.Logger = LoggerFactory.getLogger("FileLogger")
) : Logger {
  override fun info(message: String) {
    logger.info(message)
  }

  override fun debug(message: String) {
    logger.debug(message)
  }
}
