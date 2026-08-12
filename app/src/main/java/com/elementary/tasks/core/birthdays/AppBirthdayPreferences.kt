package com.elementary.tasks.core.birthdays

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logic.birthday.BirthdayPreferences

/**
 * `logic-birthday`/`feature-birthday` can't depend on `app`, so this wraps the birthday-related
 * subset of app's monolithic `Prefs` SharedPreferences store behind [BirthdayPreferences] instead.
 */
class AppBirthdayPreferences(
  private val prefs: Prefs,
) : BirthdayPreferences {
  override val is24HourFormat: Boolean
    get() = prefs.is24HourFormat
  override val hapticsEnabled: Boolean
    get() = prefs.hapticsEnabled

  override var numberOfBirthdayOccurrences: Int
    get() = prefs.numberOfBirthdayOccurrences
    set(value) { prefs.numberOfBirthdayOccurrences = value }
  override var isBirthdayReminderEnabled: Boolean
    get() = prefs.isBirthdayReminderEnabled
    set(value) { prefs.isBirthdayReminderEnabled = value }
  override var birthdayTime: String
    get() = prefs.birthdayTime
    set(value) { prefs.birthdayTime = value }
  override var isBirthdayInWidgetEnabled: Boolean
    get() = prefs.isBirthdayInWidgetEnabled
    set(value) { prefs.isBirthdayInWidgetEnabled = value }
  override var isBirthdayPermanentEnabled: Boolean
    get() = prefs.isBirthdayPermanentEnabled
    set(value) { prefs.isBirthdayPermanentEnabled = value }
  override var daysToBirthday: Int
    get() = prefs.daysToBirthday
    set(value) { prefs.daysToBirthday = value }
  override var birthdayDurationInDays: Int
    get() = prefs.birthdayDurationInDays
    set(value) { prefs.birthdayDurationInDays = value }
  override var isContactBirthdaysEnabled: Boolean
    get() = prefs.isContactBirthdaysEnabled
    set(value) { prefs.isContactBirthdaysEnabled = value }
  override var isContactAutoCheckEnabled: Boolean
    get() = prefs.isContactAutoCheckEnabled
    set(value) { prefs.isContactAutoCheckEnabled = value }
  override var isBirthdayGlobalEnabled: Boolean
    get() = prefs.isBirthdayGlobalEnabled
    set(value) { prefs.isBirthdayGlobalEnabled = value }
  override var isBirthdayLedEnabled: Boolean
    get() = prefs.isBirthdayLedEnabled
    set(value) { prefs.isBirthdayLedEnabled = value }
  override var birthdayLedColor: Int
    get() = prefs.birthdayLedColor
    set(value) { prefs.birthdayLedColor = value }
  override var birthdayPriority: Int
    get() = prefs.birthdayPriority
    set(value) { prefs.birthdayPriority = value }
}
