package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.settings.calendar.CalendarSettingsPreferences

class CalendarSettingsPreferencesImpl(
  private val prefs: Prefs,
) : CalendarSettingsPreferences {
  override var startDay: Int
    get() = prefs.startDay
    set(value) { prefs.startDay = value }

  override var todayColor: Int
    get() = prefs.todayColor
    set(value) { prefs.todayColor = value }

  override var reminderColor: Int
    get() = prefs.reminderColor
    set(value) { prefs.reminderColor = value }

  override var birthdayColor: Int
    get() = prefs.birthdayColor
    set(value) { prefs.birthdayColor = value }

  override var calendarEventColor: Int
    get() = prefs.calendarEventColor
    set(value) { prefs.calendarEventColor = value }

  override var selectedGoogleCalendarIds: Set<Long>
    get() = prefs.selectedGoogleCalendarIds
    set(value) { prefs.selectedGoogleCalendarIds = value }

  override var addRemindersToGoogleCalendar: Boolean
    get() = prefs.addRemindersToGoogleCalendar
    set(value) { prefs.addRemindersToGoogleCalendar = value }

  override var scanGoogleCalendarEvents: Boolean
    get() = prefs.scanGoogleCalendarEvents
    set(value) { prefs.scanGoogleCalendarEvents = value }

  override var publicHolidaysEnabled: Boolean
    get() = prefs.publicHolidaysEnabled
    set(value) { prefs.publicHolidaysEnabled = value }

  override var holidayCountryCode: String
    get() = prefs.holidayCountryCode
    set(value) { prefs.holidayCountryCode = value }

  override val hapticsEnabled: Boolean
    get() = prefs.hapticsEnabled
}
