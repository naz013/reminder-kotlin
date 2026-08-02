package com.github.naz013.insights.compose

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.domain.history.EventHistoricalRecordType
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.EventHistoryRepository
import com.github.naz013.repository.ReminderV2Repository
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

  private fun viewModel(): InsightsViewModel {
    every { dateTimeManager.getCurrentDateTime() } returns today.atStartOfDay()
    return InsightsViewModel(dispatcherProvider(), eventHistoryRepository, reminderV2Repository, dateTimeManager)
  }

  @Test
  fun `shows empty state when there is no history`() = runTest {
    coEvery { eventHistoryRepository.getByDateRange(any(), any()) } returns emptyList()

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

    val result = viewModel().buildState()

    assertTrue(result.weeklyTrend.isNotEmpty())
    assertEquals(today.dayOfWeek, result.busiestDay)
  }
}
