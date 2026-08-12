package com.github.naz013.appwidgets

interface AppWidgetPreferences {
  val isBirthdayInWidgetEnabled: Boolean
  val isRemindersInCalendarEnabled: Boolean
  val isFutureEventEnabled: Boolean
  val isHapticFeedbackEnabled: Boolean
  val startDay: Int
  val reminderColor: Int
  val birthdayColor: Int
  val todayColor: Int
}
