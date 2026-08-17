package com.github.naz013.feature.calendar.monthview

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface CalendarNavKey : NavKey {
  @Serializable
  data object Month : CalendarNavKey

  @Serializable
  data class Day(
    val dateMillis: Long,
  ) : CalendarNavKey
}
