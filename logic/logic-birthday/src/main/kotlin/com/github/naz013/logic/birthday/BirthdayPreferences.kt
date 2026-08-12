package com.github.naz013.logic.birthday

/**
 * Seam over the birthday-related subset of app's monolithic `Prefs` SharedPreferences wrapper,
 * which lives in `app` and can't be depended on from `logic-birthday`/`feature-birthday`.
 * Implemented in `app` by wrapping `Prefs` and bound via Koin there - see `AppBirthdayPreferences`.
 */
interface BirthdayPreferences {
  val is24HourFormat: Boolean
  val hapticsEnabled: Boolean

  var numberOfBirthdayOccurrences: Int
  var isBirthdayReminderEnabled: Boolean
  var birthdayTime: String
  var isBirthdayInWidgetEnabled: Boolean
  var isBirthdayPermanentEnabled: Boolean
  var daysToBirthday: Int
  var birthdayDurationInDays: Int
  var isContactBirthdaysEnabled: Boolean
  var isContactAutoCheckEnabled: Boolean
  var isBirthdayGlobalEnabled: Boolean
  var isBirthdayLedEnabled: Boolean
  var birthdayLedColor: Int
  var birthdayPriority: Int
}
