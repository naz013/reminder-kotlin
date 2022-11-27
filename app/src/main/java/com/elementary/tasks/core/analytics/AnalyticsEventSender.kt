package com.elementary.tasks.core.analytics

import android.os.Bundle
import com.elementary.tasks.core.utils.Logger
import com.google.firebase.analytics.FirebaseAnalytics

open class AnalyticsEventSender(
  private val analytics: FirebaseAnalytics
) {

  fun send(event: AnalyticEvent) {
    val bundle = Bundle().apply {
      event.getParams(this)
    }
    Logger.d("AnalyticsEventSender: send name=${event.name}, params=$bundle")
    analytics.logEvent(event.name, bundle)
  }
}
