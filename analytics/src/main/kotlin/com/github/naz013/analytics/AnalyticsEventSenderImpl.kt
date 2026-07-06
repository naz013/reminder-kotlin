package com.github.naz013.analytics

import android.content.Context
import com.github.naz013.logging.Logger
import com.google.firebase.analytics.FirebaseAnalytics

internal class AnalyticsEventSenderImpl(
  context: Context,
  private val analyticsStateProvider: AnalyticsStateProvider
) : AnalyticsEventSender {

  private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

  init {
    analytics.setAnalyticsCollectionEnabled(analyticsStateProvider.analyticsEnabled)
  }

  override fun send(event: AnalyticEvent) {
    if (analyticsStateProvider.analyticsEnabled) {
      val bundle = event.getParams()
      Logger.d(TAG, "Send event name=${event.getName()}, params=$bundle")
      analytics.logEvent(event.getName(), bundle)
    } else {
      Logger.d(TAG, "Send event: analytics disabled")
    }
  }

  override fun setCollectionEnabled(enabled: Boolean) {
    Logger.d(TAG, "Set Firebase analytics collection enabled=$enabled")
    analytics.setAnalyticsCollectionEnabled(enabled)
  }

  companion object {
    private const val TAG = "AnalyticsEventSender"
  }
}
