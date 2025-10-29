package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.elementary.tasks.settings.export.SyncOptions
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncSettings
import com.github.naz013.sync.settings.SettingsModel

class SyncSettingsImpl(
  private val prefs: Prefs
) : SyncSettings {

  override fun isDataTypeEnabled(dataType: DataType): Boolean {
    val syncFlags = prefs.autoSyncFlags
    return when (dataType) {
      DataType.Reminders -> syncFlags.contains(SyncOptions.FLAG_REMINDER)
      DataType.Notes -> syncFlags.contains(SyncOptions.FLAG_NOTE)
      DataType.Birthdays -> syncFlags.contains(SyncOptions.FLAG_BIRTHDAY)
      DataType.Places -> syncFlags.contains(SyncOptions.FLAG_PLACE)
      DataType.Groups -> true
      DataType.Settings -> syncFlags.contains(SyncOptions.FLAG_SETTINGS)
    }
  }

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
