package com.github.naz013.insights.compose

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

data class InsightsScreenState(
  val listState: InsightsListState = InsightsListState.Loading,
  val weeklyTrend: List<WeeklyTrendUi> = emptyList(),
  val busiestDay: DayOfWeek? = null
)

sealed interface InsightsListState {
  data object Loading : InsightsListState

  data object Empty : InsightsListState

  data class Ready(
    val streaks: List<UiStreak>
  ) : InsightsListState
}

data class UiStreak(
  val eventId: String,
  val title: String,
  val currentStreakDays: Int,
  val longestStreakDays: Int,
  val lastFiredDate: LocalDate,
  val firedCount: Int
)

data class WeeklyTrendUi(
  val weekStart: LocalDate,
  val count: Int
)
