package com.elementary.tasks.settings.security

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface SecurityNavKey : NavKey {
  @Serializable
  data object Security : SecurityNavKey

  @Serializable
  data object AddPin : SecurityNavKey

  @Serializable
  data object ChangePin : SecurityNavKey

  @Serializable
  data object DisablePin : SecurityNavKey
}
