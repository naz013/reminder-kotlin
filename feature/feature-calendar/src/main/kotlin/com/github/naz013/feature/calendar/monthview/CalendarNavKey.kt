package com.github.naz013.feature.calendar.monthview

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface CalendarNavKey : NavKey {
  /** The calendar screen, opened in the user's last-used view mode centered on today. */
  @Serializable
  data object Home : CalendarNavKey

  /** Deep-link entry that opens the calendar in Day mode focused on [dateMillis]. */
  @Serializable
  data class DayAt(
    val dateMillis: Long,
  ) : CalendarNavKey
}
