package com.elementary.tasks.core.utils.params

import com.github.naz013.common.datetime.DateTimePreferences
import com.github.naz013.ui.common.locale.Language
import java.util.Locale

class DateTimePreferencesImpl(
  private val prefs: Prefs
) : DateTimePreferences {
  override val is24HourFormat: Boolean
    get() {
      return prefs.is24HourFormat
    }
  override val birthdayTime: String
    get() {
      return prefs.birthdayTime
    }
  override val locale: Locale
    get() {
      return Language.getScreenLanguage(prefs.appLanguage)
    }
}
