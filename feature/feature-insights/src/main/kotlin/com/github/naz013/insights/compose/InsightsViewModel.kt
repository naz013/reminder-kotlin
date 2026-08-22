package com.github.naz013.insights.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.routine.RoutineExecutionRecord
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.insights.aggregator.CompletionStatsCalculator
import com.github.naz013.insights.aggregator.ReminderStreakCalculator
import com.github.naz013.insights.aggregator.RoutineStepDropoffCalculator
import com.github.naz013.insights.aggregator.RoutineStreakCalculator
import com.github.naz013.logic.routine.RoutineDurationCalculator
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.RoutineExecutionRepository
import com.github.naz013.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

internal class InsightsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val eventHistoryRepository: EventHistoryRepository,
  private val reminderV2Repository: ReminderV2Repository,
  private val dateTimeManager: DateTimeManager,
  private val routineRepository: RoutineRepository,
  private val routineExecutionRepository: RoutineExecutionRepository,
  private val routineDurationCalculator: RoutineDurationCalculator
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

    val routineInsights = buildRoutineInsights(today)

    return InsightsScreenState(
      listState = if (uiStreaks.isEmpty() && routineInsights.isEmpty()) {
        InsightsListState.Empty
      } else {
        InsightsListState.Ready(uiStreaks)
      },
      weeklyTrend = weeklyTrend.map { point -> WeeklyTrendUi(point.weekStart, point.count) },
      busiestDay = busiestDay,
      routineInsights = routineInsights,
    )
  }

  private suspend fun buildRoutineInsights(today: LocalDate): List<UiRoutineInsight> {
    val records = routineExecutionRepository.getByDateRange(today.minusDays(LOOKBACK_DAYS), today)
    if (records.isEmpty()) return emptyList()

    val recordsByRoutine: Map<String, List<RoutineExecutionRecord>> = records.groupBy { it.routineId }
    val streaks = RoutineStreakCalculator.calculate(records, today)

    return streaks
      .sortedByDescending { it.currentStreakDays }
      .mapNotNull { streak ->
        val routine = routineRepository.getById(streak.routineId) ?: return@mapNotNull null
        val routineRecords = recordsByRoutine[streak.routineId].orEmpty()
        val dropoff = RoutineStepDropoffCalculator.calculate(
          records = routineRecords,
          stepIds = routine.sortedSteps.map { it.id },
        )
        val mostSkipped = dropoff.filter { it.completionRate < 1f }.minByOrNull { it.completionRate }
        val mostSkippedStep = mostSkipped?.let { d -> routine.steps.find { it.id == d.stepId } }
        val totalFocusSeconds = routineRecords.sumOf { it.totalTimeSpentSeconds }

        UiRoutineInsight(
          routineId = streak.routineId,
          title = routine.title,
          currentStreakDays = streak.currentStreakDays,
          longestStreakDays = streak.longestStreakDays,
          totalFocusTimeLabel = routineDurationCalculator.formatDuration(totalFocusSeconds),
          mostSkippedStepTitle = mostSkippedStep?.title,
          mostSkippedCompletionPercent = mostSkipped?.let { (it.completionRate * PERCENT).toInt() },
        )
      }
  }

  companion object {
    private const val LOOKBACK_DAYS = 90L
    private const val WEEKLY_TREND_WEEKS = 8
    private const val PERCENT = 100
  }
}
