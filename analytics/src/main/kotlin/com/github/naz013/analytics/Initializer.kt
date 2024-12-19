package com.github.naz013.analytics

import android.content.Context

fun initializeAnalytics(
  context: Context,
  analyticsStateProvider: AnalyticsStateProvider
): AnalyticsEventSender {
  return AnalyticsEventSenderImpl(
    context = context,
    analyticsStateProvider = analyticsStateProvider
  )
}
