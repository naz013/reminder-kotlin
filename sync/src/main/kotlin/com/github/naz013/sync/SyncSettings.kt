package com.github.naz013.sync

import com.github.naz013.sync.settings.SettingsModel

interface SyncSettings {
  fun getSettings(): SettingsModel
}
