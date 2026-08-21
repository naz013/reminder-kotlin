package com.elementary.tasks.module.logicnotificationaction

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logic.notificationaction.WearPreferences

class WearPreferencesImpl(
  private val prefs: Prefs,
) : WearPreferences {
  override val isWearEnabled: Boolean
    get() = prefs.isWearEnabled
}
