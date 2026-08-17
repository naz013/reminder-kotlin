package com.elementary.tasks.core.calendar

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.calendar.CalendarPreferences

/**
 * `feature-calendar` can't depend on `app`, so this wraps the calendar-related subset of app's
 * monolithic `Prefs` SharedPreferences store behind [CalendarPreferences] instead.
 */
class AppCalendarPreferences(
  private val prefs: Prefs,
) : CalendarPreferences {
  override val startDay: Int
    get() = prefs.startDay
  override val birthdayLedColor: Int
    get() = prefs.birthdayLedColor
  override val reminderColor: Int
    get() = prefs.reminderColor
}
