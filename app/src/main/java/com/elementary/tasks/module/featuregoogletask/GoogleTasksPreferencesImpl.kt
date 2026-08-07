package com.elementary.tasks.module.featuregoogletask

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.googletask.GoogleTasksPreferences

class GoogleTasksPreferencesImpl(
  private val prefs: Prefs
) : GoogleTasksPreferences {
  override val hapticsEnabled: Boolean
    get() = prefs.hapticsEnabled
  override val is24HourFormat: Boolean
    get() = prefs.is24HourFormat
}
