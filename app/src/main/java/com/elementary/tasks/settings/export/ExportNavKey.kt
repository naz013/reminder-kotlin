package com.elementary.tasks.settings.export

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ExportNavKey : NavKey {
  @Serializable
  data object CloudBackup : ExportNavKey

  @Serializable
  data object CloudServices : ExportNavKey
}
