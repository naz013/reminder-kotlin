package com.github.naz013.feature.reminder.todo

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface TodoEditNavKey : NavKey {
  @Serializable
  data class Main(
    val id: String = "",
  ) : TodoEditNavKey
}
