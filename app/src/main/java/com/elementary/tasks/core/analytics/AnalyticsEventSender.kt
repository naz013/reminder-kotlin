package com.elementary.tasks.core.analytics

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logging.Logger
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsEventSender(
  private val analytics: FirebaseAnalytics,
  private val prefs: Prefs
) {

  fun send(event: AnalyticEvent) {
    if (prefs.analyticsEnabled) {
      val bundle = event.getParams()
      Logger.d("Send event name=${event.getName()}, params=$bundle")
      analytics.logEvent(event.getName(), bundle)
    } else {
      Logger.d("Send event: analytics disabled")
    }
  }
}
