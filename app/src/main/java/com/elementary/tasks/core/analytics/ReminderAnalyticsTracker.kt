package com.elementary.tasks.core.analytics

import com.elementary.tasks.core.data.ui.reminder.UiReminderType

class ReminderAnalyticsTracker(
  private val analyticsEventSender: AnalyticsEventSender
) : AnalyticsTracker() {

  fun startTracking() {
    trackEvent(Event.REMINDER_USED)
  }

  fun sendEvent(type: UiReminderType) {
    analyticsEventSender.send(
      ReminderFeatureUsedEvent(
        type,
        getTimeInSeconds(Event.REMINDER_USED)
      )
    )
  }
}