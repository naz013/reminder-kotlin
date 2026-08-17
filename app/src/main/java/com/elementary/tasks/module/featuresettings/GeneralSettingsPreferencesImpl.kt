package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.settings.general.GeneralSettingsPreferences

class GeneralSettingsPreferencesImpl(
  private val prefs: Prefs,
) : GeneralSettingsPreferences {
  override var appLanguage: Int
    get() = prefs.appLanguage
    set(value) { prefs.appLanguage = value }

  override var nightMode: Int
    get() = prefs.nightMode
    set(value) { prefs.nightMode = value }

  override var hourFormat: Int
    get() = prefs.hourFormat
    set(value) { prefs.hourFormat = value }

  override var useMetric: Boolean
    get() = prefs.useMetric
    set(value) { prefs.useMetric = value }

  override var hapticsEnabled: Boolean
    get() = prefs.hapticsEnabled
    set(value) { prefs.hapticsEnabled = value }

  override var analyticsEnabled: Boolean
    get() = prefs.analyticsEnabled
    set(value) { prefs.analyticsEnabled = value }
}
