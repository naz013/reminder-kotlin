package com.github.naz013.feature.calendar

interface CalendarPreferences {
  val startDay: Int
  val birthdayLedColor: Int
  val reminderColor: Int

  /** The calendar view mode last selected by the user, restored across app restarts. */
  var lastViewMode: CalendarViewMode
}
