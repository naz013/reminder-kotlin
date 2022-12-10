package com.elementary.tasks.core.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class AnalyticsEventSender(private val analytics: FirebaseAnalytics) {

  fun send(event: AnalyticEvent) {
    val bundle = event.getParams()
    Timber.d("Send event name=${event.getName()}, params=$bundle")
    analytics.logEvent(event.getName(), bundle)
  }
}
