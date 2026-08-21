package com.elementary.tasks.module.logicnotificationaction

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logic.notificationaction.DoNotDisturbPreferences

class DoNotDisturbPreferencesImpl(
  private val prefs: Prefs,
) : DoNotDisturbPreferences {
  override val isDoNotDisturbEnabled: Boolean
    get() = prefs.isDoNotDisturbEnabled

  override val doNotDisturbFrom: String
    get() = prefs.doNotDisturbFrom

  override val doNotDisturbTo: String
    get() = prefs.doNotDisturbTo

  override val doNotDisturbIgnore: Int
    get() = prefs.doNotDisturbIgnore

  override val doNotDisturbAction: Int
    get() = prefs.doNotDisturbAction
}
