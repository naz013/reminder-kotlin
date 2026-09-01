package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.settings.digest.DigestSettingsPreferences

class DigestSettingsPreferencesImpl(
  private val prefs: Prefs,
) : DigestSettingsPreferences {
  override var aiDigestDailyEnabled: Boolean
    get() = prefs.aiDigestDailyEnabled
    set(value) { prefs.aiDigestDailyEnabled = value }

  override var aiDigestHour: Int
    get() = prefs.aiDigestHour
    set(value) { prefs.aiDigestHour = value }
}
