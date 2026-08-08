package com.github.naz013.logic.reminder

interface ReminderPreferences {
  val numberOfReminderOccurrences: Int
  val isSbNotificationEnabled: Boolean
  val isCalendarEnabled: Boolean
}
