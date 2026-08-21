package com.github.naz013.feature.routine

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface RoutineNavKey : NavKey {
  @Serializable
  data object List : RoutineNavKey

  @Serializable
  data class Edit(
    val id: String? = null
  ) : RoutineNavKey

  @Serializable
  data class Preview(
    val id: String
  ) : RoutineNavKey
}
