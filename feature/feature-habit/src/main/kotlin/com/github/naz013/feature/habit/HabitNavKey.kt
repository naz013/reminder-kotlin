package com.github.naz013.feature.habit

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface HabitNavKey : NavKey {
  @Serializable
  data object List : HabitNavKey

  @Serializable
  data class Editor(
    val habitId: String? = null
  ) : HabitNavKey
}
