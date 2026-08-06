package com.elementary.tasks.groups

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface GroupsNavKey : NavKey {
  @Serializable
  data object List : GroupsNavKey

  @Serializable
  data class Edit(
    val id: String = "",
    val fromIntentData: Boolean = false,
  ) : GroupsNavKey

  @Serializable
  data class Details(
    val id: String,
  ) : GroupsNavKey
}
