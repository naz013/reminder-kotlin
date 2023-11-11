package com.elementary.tasks.core.analytics

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.SuperUtil
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import timber.log.Timber

object Traces {

  private var reportingEnabled: Boolean = true

  fun d(tag: String, message: String) {
    Timber.tag(tag).d(message)
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
    if (reportingEnabled) {
      Firebase.crashlytics.log(message)
    }
  }
}
