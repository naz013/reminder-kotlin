package com.github.naz013.insights.compose

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.domain.history.EventHistoricalRecordType
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineExecutionRecord
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logic.routine.RoutineDurationCalculator
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.RoutineExecutionRepository
import com.github.naz013.repository.RoutineRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class InsightsViewModelTest {

  private val eventHistoryRepository = mockk<EventHistoryRepository>()
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val routineRepository = mockk<RoutineRepository>()
  private val routineExecutionRepository = mockk<RoutineExecutionRepository>()
  private val routineDurationCalculator = RoutineDurationCalculator()
  private val today = LocalDate.of(2026, 8, 2)

  private fun dispatcherProvider(): DispatcherProvider {
    val provider = mockk<DispatcherProvider>()
    every { provider.default() }.returns(Dispatchers.Unconfined)
    every { provider.io() }.returns(Dispatchers.Unconfined)
    every { provider.main() }.returns(Dispatchers.Unconfined)
    return provider
  }

  private fun record(eventId: String, date: LocalDate) = EventHistoricalRecord(
    id = "$eventId-$date",
    eventId = eventId,
    date = date,
    time = LocalTime.NOON,
    type = EventHistoricalRecordType.Reminder
  )

  private fun reminder(id: String, summary: String) = ReminderV2(
    uuId = id,
    summary = summary,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.now())
  )

  private fun routine(id: String, title: String, stepIds: List<String> = listOf("s1")) = Routine(
    id = id,
    title = title,
    steps = stepIds.mapIndexed { index, stepId -> RoutineStep(id = stepId, title = stepId, order = index) },
    createdAt = LocalDateTime.now(),
    updatedAt = LocalDateTime.now(),
  )

  private fun executionRecord(
    routineId: String,
    date: LocalDate,
    completedStepIds: List<String>,
    totalStepsCount: Int,
    totalTimeSpentSeconds: Int = 0,
  ) = RoutineExecutionRecord(
    routineId = routineId,
    executedAt = date.atTime(LocalTime.NOON),
    completedStepIds = completedStepIds,
    totalStepsCount = totalStepsCount,
    totalTimeSpentSeconds = totalTimeSpentSeconds,
  )

  private fun viewModel(): InsightsViewModel {
    every { dateTimeManager.getCurrentDateTime() } returns today.atStartOfDay()
    return InsightsViewModel(
      dispatcherProvider(),
      eventHistoryRepository,
      reminderV2Repository,
      dateTimeManager,
      routineRepository,
      routineExecutionRepository,
      routineDurationCalculator,
    )
  }

  @Test
  fun `shows empty state when there is no history`() = runTest {
    coEvery { eventHistoryRepository.getByDateRange(any(), any()) } returns emptyList()
    coEvery { routineExecutionRepository.getByDateRange(any(), any()) } returns emptyList()

    val result = viewModel().buildState()

    assertEquals(InsightsListState.Empty, result.listState)
  }

  @Test
  fun `builds a streak card per reminder with a resolvable title`() = runTest {
    coEvery { eventHistoryRepository.getByDateRange(any(), any()) } returns listOf(
      record("r1", today),
      record("r1", today.minusDays(1)),
    )
    coEvery { reminderV2Repository.getById("r1") } returns reminder("r1", "Take pills")
    coEvery { routineExecutionRepository.getByDateRange(any(), any()) } returns emptyList()

    val result = viewModel().buildState()

    val streaks = (result.listState as InsightsListState.Ready).streaks
    assertEquals(1, streaks.size)
    assertEquals("Take pills", streaks.single().title)
    assertEquals(2, streaks.single().currentStreakDays)
    assertEquals(2, streaks.single().firedCount)
  }

  @Test
  fun `skips streaks for reminders that no longer exist`() = runTest {
    coEvery { eventHistoryRepository.getByDateRange(any(), any()) } returns listOf(record("deleted", today))
    coEvery { reminderV2Repository.getById("deleted") } returns null
    coEvery { routineExecutionRepository.getByDateRange(any(), any()) } returns emptyList()

    val result = viewModel().buildState()

    assertEquals(InsightsListState.Empty, result.listState)
  }

  @Test
  fun `exposes a weekly trend and busiest day alongside the streak list`() = runTest {
    coEvery { eventHistoryRepository.getByDateRange(any(), any()) } returns listOf(
      record("r1", today),
      record("r1", today),
    )
    coEvery { reminderV2Repository.getById("r1") } returns reminder("r1", "Water plants")
    coEvery { routineExecutionRepository.getByDateRange(any(), any()) } returns emptyList()

    val result = viewModel().buildState()

    assertTrue(result.weeklyTrend.isNotEmpty())
    assertEquals(today.dayOfWeek, result.busiestDay)
  }

  @Test
  fun `is not empty when only routine activity exists`() = runTest {
    coEvery { eventHistoryRepository.getByDateRange(any(), any()) } returns emptyList()
    coEvery { routineExecutionRepository.getByDateRange(any(), any()) } returns listOf(
      executionRecord("o1", today, completedStepIds = listOf("s1"), totalStepsCount = 1),
    )
    coEvery { routineRepository.getById("o1") } returns routine("o1", "Morning routine")

    val result = viewModel().buildState()

    assertTrue(result.listState is InsightsListState.Ready)
    assertEquals(1, result.routineInsights.size)
    assertEquals("Morning routine", result.routineInsights.single().title)
    assertEquals(1, result.routineInsights.single().currentStreakDays)
  }

  @Test
  fun `surfaces total focus time and the most skipped step for a routine`() = runTest {
    coEvery { eventHistoryRepository.getByDateRange(any(), any()) } returns emptyList()
    coEvery { routineExecutionRepository.getByDateRange(any(), any()) } returns listOf(
      executionRecord(
        "o1",
        today,
        completedStepIds = listOf("s1"),
        totalStepsCount = 2,
        totalTimeSpentSeconds = 300,
      ),
      executionRecord(
        "o1",
        today.minusDays(1),
        completedStepIds = listOf("s1", "s2"),
        totalStepsCount = 2,
        totalTimeSpentSeconds = 300,
      ),
    )
    coEvery { routineRepository.getById("o1") } returns routine("o1", "Morning routine", stepIds = listOf("s1", "s2"))

    val result = viewModel().buildState()

    val insight = result.routineInsights.single()
    assertEquals("10m", insight.totalFocusTimeLabel)
    assertEquals("s2", insight.mostSkippedStepTitle)
    assertEquals(50, insight.mostSkippedCompletionPercent)
  }

  @Test
  fun `skips routine insights for routines that no longer exist`() = runTest {
    coEvery { eventHistoryRepository.getByDateRange(any(), any()) } returns emptyList()
    coEvery { routineExecutionRepository.getByDateRange(any(), any()) } returns listOf(
      executionRecord("deleted", today, completedStepIds = emptyList(), totalStepsCount = 1),
    )
    coEvery { routineRepository.getById("deleted") } returns null

    val result = viewModel().buildState()

    assertTrue(result.routineInsights.isEmpty())
    assertEquals(InsightsListState.Empty, result.listState)
  }
}
