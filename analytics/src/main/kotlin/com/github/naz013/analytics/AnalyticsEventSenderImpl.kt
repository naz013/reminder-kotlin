package com.github.naz013.analytics

import android.content.Context
import com.github.naz013.logging.Logger
import com.google.firebase.analytics.FirebaseAnalytics

internal class AnalyticsEventSenderImpl(
  context: Context,
  private val analyticsStateProvider: AnalyticsStateProvider
) : AnalyticsEventSender {

  private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

  override fun send(event: AnalyticEvent) {
    if (analyticsStateProvider.analyticsEnabled) {
      val bundle = event.getParams()
      Logger.d("Send event name=${event.getName()}, params=$bundle")
      analytics.logEvent(event.getName(), bundle)
    } else {
      Logger.d("Send event: analytics disabled")
    }
  }
}
