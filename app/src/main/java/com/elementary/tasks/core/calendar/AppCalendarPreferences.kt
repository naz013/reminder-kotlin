package com.elementary.tasks.core.calendar

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.calendar.CalendarPreferences
import com.github.naz013.feature.calendar.CalendarViewMode

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
  override val calendarEventColor: Int
    get() = prefs.calendarEventColor

  // Stored as the enum name (not ordinal) so reordering CalendarViewMode can't silently remap a
  // saved preference; an unknown/empty stored value falls back to MONTH.
  override var lastViewMode: CalendarViewMode
    get() = runCatching { CalendarViewMode.valueOf(prefs.calendarViewMode) }.getOrDefault(CalendarViewMode.MONTH)
    set(value) { prefs.calendarViewMode = value.name }
}
