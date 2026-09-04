package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.settings.debug.DeveloperSettingsPreferences

class DeveloperSettingsPreferencesImpl(
  private val prefs: Prefs,
) : DeveloperSettingsPreferences {
  override var isUserLogged: Boolean
    get() = prefs.isUserLogged
    set(value) { prefs.isUserLogged = value }

  override var lastVersionCode: Long
    get() = prefs.lastVersionCode
    set(value) { prefs.lastVersionCode = value }

  override var hasSeenOnboarding: Boolean
    get() = prefs.hasSeenOnboarding
    set(value) { prefs.hasSeenOnboarding = value }
}
