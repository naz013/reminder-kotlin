package com.elementary.tasks.module.logicnotificationaction

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logic.notificationaction.InAppAlertPreferences

class InAppAlertPreferencesImpl(
  private val prefs: Prefs,
) : InAppAlertPreferences {
  override val isInAppAlertBannerEnabled: Boolean
    get() = prefs.isInAppAlertBannerEnabled
}
