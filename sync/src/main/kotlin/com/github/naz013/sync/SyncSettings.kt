package com.github.naz013.sync

import com.github.naz013.files.model.SettingsModel

interface SyncSettings {
  fun getSettings(): SettingsModel
}
