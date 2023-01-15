package com.elementary.tasks.core.analytics

open class AnalyticsTracker {

  private val trackers = mutableMapOf<String, Long>()

  fun trackEvent(event: Event) {
    trackers[event.value] = System.currentTimeMillis()
  }

  fun getTimeInSeconds(event: Event): Long {
    val startTime = trackers[event.value] ?: return 0
    return (System.currentTimeMillis() - startTime) / 1000L
  }
}
