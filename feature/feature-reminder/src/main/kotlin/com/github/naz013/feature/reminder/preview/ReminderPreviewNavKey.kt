package com.github.naz013.feature.reminder.preview

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ReminderPreviewNavKey : NavKey {
  @Serializable
  data class Preview(
    val id: String,
  ) : ReminderPreviewNavKey

  @Serializable
  data class FullscreenMap(
    val id: String,
  ) : ReminderPreviewNavKey
}
