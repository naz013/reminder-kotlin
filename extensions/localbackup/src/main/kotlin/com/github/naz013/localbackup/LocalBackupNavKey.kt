package com.github.naz013.localbackup

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface LocalBackupNavKey : NavKey {
  @Serializable
  data class Export(
    val uriString: String
  ) : LocalBackupNavKey

  @Serializable
  data class Import(
    val uriString: String
  ) : LocalBackupNavKey
}
