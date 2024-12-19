package com.elementary.tasks.core.analytics

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.AnalyticsReminderType
import com.github.naz013.analytics.AnalyticsTracker
import com.github.naz013.analytics.Event
import com.github.naz013.analytics.ReminderFeatureUsedEvent

class ReminderAnalyticsTracker(
  private val analyticsEventSender: AnalyticsEventSender
) : AnalyticsTracker() {

  fun startTracking() {
    trackEvent(Event.REMINDER_USED)
  }

  fun sendEvent(type: AnalyticsReminderType) {
    analyticsEventSender.send(
      ReminderFeatureUsedEvent(
        type,
        getTimeInSeconds(Event.REMINDER_USED)
      )
    )
  }
}
