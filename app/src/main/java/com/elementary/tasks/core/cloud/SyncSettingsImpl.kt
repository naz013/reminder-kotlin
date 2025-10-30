package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.github.naz013.sync.SyncSettings
import com.github.naz013.sync.settings.SettingsModel

class SyncSettingsImpl(
  private val prefs: Prefs
) : SyncSettings {

  override fun getSettings(): SettingsModel {
    val list = prefs.all().toMutableMap()
    if (list.containsKey(PrefsConstants.DRIVE_USER)) {
      list.remove(PrefsConstants.DRIVE_USER)
    }
    if (list.containsKey(PrefsConstants.TASKS_USER)) {
      list.remove(PrefsConstants.TASKS_USER)
    }
    return SettingsModel(list)
  }
}
