package com.github.naz013.feature.home.dayoverview

internal data class DayOverviewState(
  val dateHeading: String = "",
  val summary: String = "",
  val nextItem: NextItem? = null,
  val freeTime: FreeTimeLabels = FreeTimeLabels(),
  val weekDays: List<WeekDay> = emptyList(),
) {
  data class NextItem(
    val time: String,
    val title: String,
  )

  data class FreeTimeLabels(
    val current: String = "",
    val largest: String? = null,
  )

  data class WeekDay(
    val weekDayLabel: String,
    val dayOfMonth: String,
    val isToday: Boolean,
  )
}
