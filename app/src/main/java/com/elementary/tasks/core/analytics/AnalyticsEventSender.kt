package com.elementary.tasks.core.analytics

import com.elementary.tasks.core.utils.params.Prefs
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class AnalyticsEventSender(
  private val analytics: FirebaseAnalytics,
  private val prefs: Prefs
) {

  fun send(event: AnalyticEvent) {
    if (prefs.analyticsEnabled) {
      val bundle = event.getParams()
      Timber.d("Send event name=${event.getName()}, params=$bundle")
      analytics.logEvent(event.getName(), bundle)
    } else {
      Timber.d("Send event: analytics disabled")
    }
  }
}
