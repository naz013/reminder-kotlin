package com.github.naz013.feature.reminder.analytics

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.AnalyticsReminderType
import com.github.naz013.analytics.AnalyticsTracker
import com.github.naz013.analytics.Event
import com.github.naz013.analytics.ReminderFeatureUsedEvent

internal class ReminderAnalyticsTracker(
  private val analyticsEventSender: AnalyticsEventSender,
) : AnalyticsTracker() {
  fun startTracking() {
    trackEvent(Event.REMINDER_USED)
  }

  fun sendEvent(type: AnalyticsReminderType) {
    analyticsEventSender.send(
      ReminderFeatureUsedEvent(
        type,
        getTimeInSeconds(Event.REMINDER_USED),
      ),
    )
  }
}
