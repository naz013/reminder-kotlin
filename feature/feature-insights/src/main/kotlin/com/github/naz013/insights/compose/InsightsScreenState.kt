package com.github.naz013.insights.compose

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

internal data class InsightsScreenState(
  val listState: InsightsListState = InsightsListState.Loading,
  val weeklyTrend: List<WeeklyTrendUi> = emptyList(),
  val busiestDay: DayOfWeek? = null,
  val routineInsights: List<UiRoutineInsight> = emptyList()
)

internal sealed interface InsightsListState {
  data object Loading : InsightsListState

  data object Empty : InsightsListState

  data class Ready(
    val streaks: List<UiStreak>
  ) : InsightsListState
}

internal data class UiStreak(
  val eventId: String,
  val title: String,
  val currentStreakDays: Int,
  val longestStreakDays: Int,
  val lastFiredDate: LocalDate,
  val firedCount: Int
)

internal data class WeeklyTrendUi(
  val weekStart: LocalDate,
  val count: Int
)

internal data class UiRoutineInsight(
  val routineId: String,
  val title: String,
  val currentStreakDays: Int,
  val longestStreakDays: Int,
  val totalFocusTimeLabel: String,
  val mostSkippedStepTitle: String?,
  val mostSkippedCompletionPercent: Int?
)
