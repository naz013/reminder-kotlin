package com.github.naz013.feature.googletask

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface GoogleTasksNavKey : NavKey {
  @Serializable
  data object List : GoogleTasksNavKey

  @Serializable
  data class TaskList(
    val listId: String,
  ) : GoogleTasksNavKey

  @Serializable
  data class TaskPreview(
    val id: String,
  ) : GoogleTasksNavKey

  @Serializable
  data class TaskEdit(
    val id: String? = null,
    val listId: String = "",
  ) : GoogleTasksNavKey

  @Serializable
  data class ListEdit(
    val id: String? = null,
  ) : GoogleTasksNavKey
}
