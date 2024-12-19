package com.github.naz013.analytics

interface AnalyticsEventSender {
  fun send(event: AnalyticEvent)
}
