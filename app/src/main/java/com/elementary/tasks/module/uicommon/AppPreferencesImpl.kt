package com.elementary.tasks.module.uicommon

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.ui.common.preferences.AppPreferences

class AppPreferencesImpl(
  private val prefs: Prefs
) : AppPreferences {

  override val hapticsEnabled: Boolean
    get() = prefs.hapticsEnabled
}
