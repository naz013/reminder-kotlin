package com.github.naz013.feature.places

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface PlacesNavKey : NavKey {
  @Serializable
  data object List : PlacesNavKey

  @Serializable
  data class Edit(
    val id: String = "",
    val fromIntentData: Boolean = false,
  ) : PlacesNavKey
}
