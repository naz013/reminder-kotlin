package com.elementary.tasks.settings.location

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface LocationNavKey : NavKey {
  @Serializable
  data object Location : LocationNavKey

  @Serializable
  data object MapStyle : LocationNavKey
}
