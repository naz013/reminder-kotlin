package com.github.naz013.tags

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface TagsNavKey : NavKey {
  @Serializable
  data object Manage : TagsNavKey

  @Serializable
  data class Edit(
    val id: String? = null
  ) : TagsNavKey

  @Serializable
  data class Details(
    val id: String
  ) : TagsNavKey
}
