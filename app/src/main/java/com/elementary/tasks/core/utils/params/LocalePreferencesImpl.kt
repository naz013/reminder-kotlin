package com.elementary.tasks.core.utils.params

import com.github.naz013.ui.common.locale.LocalePreferences

class LocalePreferencesImpl(
  private val prefs: Prefs
) : LocalePreferences {
  override val appLanguage: Int
    get() = prefs.appLanguage
  override val voiceLocale: Int
    get() = prefs.voiceLocale
}
