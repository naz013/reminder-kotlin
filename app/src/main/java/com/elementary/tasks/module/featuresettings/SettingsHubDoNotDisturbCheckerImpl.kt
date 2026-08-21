package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.github.naz013.feature.settings.SettingsHubDoNotDisturbChecker
import com.github.naz013.logic.notificationaction.DoNotDisturbManager

class SettingsHubDoNotDisturbCheckerImpl(
  private val prefs: Prefs,
  private val doNotDisturbManager: DoNotDisturbManager,
) : SettingsHubDoNotDisturbChecker {
  override fun isActive(): Boolean = doNotDisturbManager.applyDoNotDisturb(0)

  override fun addChangeObserver(observer: () -> Unit) {
    val prefsObserver: (String) -> Unit = { observer() }
    observers[observer] = prefsObserver
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_FROM, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_TO, prefsObserver)
    prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, prefsObserver)
  }

  override fun removeChangeObserver(observer: () -> Unit) {
    val prefsObserver = observers.remove(observer) ?: return
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_FROM, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_TO, prefsObserver)
    prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, prefsObserver)
  }

  private val observers = mutableMapOf<() -> Unit, (String) -> Unit>()
}
