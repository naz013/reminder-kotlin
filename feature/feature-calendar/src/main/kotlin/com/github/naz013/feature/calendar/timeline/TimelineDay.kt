package com.github.naz013.feature.calendar.timeline

import org.threeten.bp.LocalDate

/** One day column's header data in the 3-day / 7-day timeline. */
internal data class TimelineDay(
  val date: LocalDate,
  val weekdayLabel: String,
  val dayLabel: String,
  val isToday: Boolean,
)
