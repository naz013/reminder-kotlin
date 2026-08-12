package com.github.naz013.feature.note

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface NotesNavKey : NavKey {
  @Serializable
  data object List : NotesNavKey

  @Serializable
  data object Archive : NotesNavKey

  @Serializable
  data class Preview(
    val id: String,
  ) : NotesNavKey

  @Serializable
  data class Edit(
    val id: String? = null,
    val fromIntentData: Boolean = false,
    val sharedText: String? = null,
    val sharedImageUris: kotlin.collections.List<String>? = null,
  ) : NotesNavKey

  @Serializable
  data class ImagePreview(
    val position: Int,
  ) : NotesNavKey
}
