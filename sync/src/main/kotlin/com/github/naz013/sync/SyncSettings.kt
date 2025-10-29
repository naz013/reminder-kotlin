package com.github.naz013.sync

import com.github.naz013.sync.settings.SettingsModel

interface SyncSettings {
  fun isDataTypeEnabled(dataType: DataType): Boolean
  fun getSettings(): SettingsModel
}
