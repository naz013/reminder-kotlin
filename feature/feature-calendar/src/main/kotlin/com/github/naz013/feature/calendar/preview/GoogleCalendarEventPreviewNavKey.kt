package com.github.naz013.feature.calendar.preview

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface GoogleCalendarEventPreviewNavKey : NavKey {
  @Serializable
  data class Preview(
    val id: String,
  ) : GoogleCalendarEventPreviewNavKey
}
