package com.github.naz013.feature.settings.calendar

interface CalendarSettingsPreferences {
  var startDay: Int
  var todayColor: Int
  var reminderColor: Int
  var birthdayColor: Int
  var googleCalendarReminderId: Long
  var addRemindersToGoogleCalendar: Boolean
  var scanGoogleCalendarEvents: Boolean
  var publicHolidaysEnabled: Boolean
  var holidayCountryCode: String
  val hapticsEnabled: Boolean
}
