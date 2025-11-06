package com.elementary.tasks.core.utils.params

import com.github.naz013.appwidgets.AppWidgetPreferences

class AppWidgetPreferencesImpl(
  private val prefs: Prefs
) : AppWidgetPreferences {
  override val isBirthdayInWidgetEnabled: Boolean
    get() = prefs.isBirthdayInWidgetEnabled
  override val isRemindersInCalendarEnabled: Boolean
    get() = true
  override val isFutureEventEnabled: Boolean
    get() = true
  override val startDay: Int
    get() = prefs.startDay
  override val reminderColor: Int
    get() = prefs.reminderColor
  override val birthdayColor: Int
    get() = prefs.birthdayColor
  override val todayColor: Int
    get() = prefs.todayColor
}
