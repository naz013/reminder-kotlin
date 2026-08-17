package com.github.naz013.feature.settings.export

import com.github.naz013.logic.schedule.WorkerNetworkType

interface CloudBackupSettingsPreferences {
  var autoBackupState: Int
  var workerNetworkType: WorkerNetworkType
}
