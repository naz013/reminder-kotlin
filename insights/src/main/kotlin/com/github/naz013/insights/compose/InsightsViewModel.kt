package com.github.naz013.insights.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.insights.aggregator.CompletionStatsCalculator
import com.github.naz013.insights.aggregator.ReminderStreakCalculator
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class InsightsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val eventHistoryRepository: EventHistoryRepository,
  private val reminderV2Repository: ReminderV2Repository,
  private val dateTimeManager: DateTimeManager
) : ViewModel() {

  private val _state = MutableStateFlow(InsightsScreenState())
  val state = _state.stateInWhileSubscribed(InsightsScreenState())

  init {
    viewModelScope.launch(dispatcherProvider.io()) {
      val result = buildState()
      withContext(dispatcherProvider.main()) {
        _state.update { result }
      }
    }
  }

  internal suspend fun buildState(): InsightsScreenState {
    val today = dateTimeManager.getCurrentDateTime().toLocalDate()
    val records = eventHistoryRepository.getByDateRange(today.minusDays(LOOKBACK_DAYS), today)

    val streaks = ReminderStreakCalculator.calculate(records, today)
    val firedCounts = CompletionStatsCalculator.firedCounts(records).associateBy { it.eventId }
    val weeklyTrend = CompletionStatsCalculator.weeklyTrend(records, WEEKLY_TREND_WEEKS, today)
    val busiestDay = CompletionStatsCalculator.busiestDayOfWeek(records).maxByOrNull { it.value }?.key

    val uiStreaks = streaks
      .sortedByDescending { it.currentStreakDays }
      .mapNotNull { streak ->
        val reminder = reminderV2Repository.getById(streak.eventId) ?: return@mapNotNull null
        UiStreak(
          eventId = streak.eventId,
          title = reminder.summary,
          currentStreakDays = streak.currentStreakDays,
          longestStreakDays = streak.longestStreakDays,
          lastFiredDate = streak.lastFiredDate,
          firedCount = firedCounts[streak.eventId]?.count ?: 0
        )
      }

    return InsightsScreenState(
      listState = if (uiStreaks.isEmpty()) InsightsListState.Empty else InsightsListState.Ready(uiStreaks),
      weeklyTrend = weeklyTrend.map { point -> WeeklyTrendUi(point.weekStart, point.count) },
      busiestDay = busiestDay,
    )
  }

  companion object {
    private const val LOOKBACK_DAYS = 90L
    private const val WEEKLY_TREND_WEEKS = 8
  }
}
