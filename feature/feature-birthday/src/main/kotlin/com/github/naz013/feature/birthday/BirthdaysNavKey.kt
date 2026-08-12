package com.github.naz013.feature.birthday

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface BirthdaysNavKey : NavKey {
  @Serializable
  data class Preview(
    val id: String,
  ) : BirthdaysNavKey

  @Serializable
  data class Edit(
    val id: String? = null,
    val fromIntentData: Boolean = false,
    val prefillDateEpochDay: Long? = null,
  ) : BirthdaysNavKey
}
