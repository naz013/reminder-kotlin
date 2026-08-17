package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.settings.export.CloudBackupSettingsPreferences
import com.github.naz013.logic.schedule.WorkerNetworkType

class CloudBackupSettingsPreferencesImpl(
  private val prefs: Prefs,
) : CloudBackupSettingsPreferences {
  override var autoBackupState: Int
    get() = prefs.autoBackupState
    set(value) { prefs.autoBackupState = value }

  override var workerNetworkType: WorkerNetworkType
    get() = prefs.workerNetworkType
    set(value) { prefs.workerNetworkType = value }
}
