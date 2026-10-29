package com.github.naz013.logic.reminder.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logic.reminder.usecase.RestoreReminderStatesUseCase.State
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.scheduler.JobSchedulerApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import java.util.TimeZone

class RestoreReminderStatesUseCaseTest {
  private val originalZone: TimeZone = TimeZone.getDefault()
  private val nowProvider = mockk<NowDateTimeProvider>()
  private val repository = mockk<ReminderV2Repository>()
  private val saveReminderUseCase = mockk<SaveReminderUseCase>()
  private val jobScheduler = mockk<JobSchedulerApi>()
  private val saved = slot<ReminderV2>()

  private lateinit var useCase: RestoreReminderStatesUseCase

  @Before
  fun setUp() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    every { nowProvider.nowDateTime() } returns LocalDateTime.of(2025, 3, 10, 12, 0)
    every { nowProvider.nowInstant() } returns Instant.parse("2025-03-10T12:00:00Z")
    coEvery { saveReminderUseCase(capture(saved)) } just runs
    every { jobScheduler.cancelReminder(any()) } just runs
    every { jobScheduler.scheduleReminder(any()) } just runs

    useCase = RestoreReminderStatesUseCase(
      reminderRepository = repository,
      saveReminderUseCase = saveReminderUseCase,
      jobScheduler = jobScheduler,
      dateTimeManager = DateTimeManager(nowProvider, mockk(relaxed = true)),
    )
  }

  @After
  fun tearDown() {
    TimeZone.setDefault(originalZone)
  }

  private fun stored(
    event: LocalDateTime?,
    eventCount: Long = 5,
    isActive: Boolean = true,
  ) = ReminderV2(
    uuId = "1",
    eventCount = eventCount,
    isActive = isActive,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2025, 3, 1, 9, 0), eventDateTime = event),
  ).also { coEvery { repository.getById("1") } returns it }

  @Test
  fun `captures event time, count and active flag`() =
    runTest {
      val event = LocalDateTime.of(2025, 3, 8, 15, 30)
      stored(event = event, eventCount = 3, isActive = false)
      coEvery { repository.getById("missing") } returns null

      val states = useCase.capture(listOf("1", "1", "missing"))

      assertEquals(listOf(State("1", event, 3L, false)), states)
    }

  @Test
  fun `restores captured values and marks reminder for upload`() =
    runTest {
      val current = stored(event = LocalDateTime.of(2025, 3, 11, 9, 0), eventCount = 6, isActive = true)
      val original = LocalDateTime.of(2025, 3, 8, 15, 30)

      useCase(listOf(State("1", original, 5L, false)))

      assertEquals(original, saved.captured.schedule.eventDateTime)
      assertEquals(5L, saved.captured.eventCount)
      assertEquals(false, saved.captured.isActive)
      assertEquals(SyncState.WaitingForUpload, saved.captured.sync.syncState)
      assertEquals(current.sync.version + 1, saved.captured.sync.version)
      verify(exactly = 1) { jobScheduler.cancelReminder(current.uniqueId) }
      verify(exactly = 0) { jobScheduler.scheduleReminder(any()) }
    }

  @Test
  fun `reschedules the alarm when restored reminder is active and in the future`() =
    runTest {
      stored(event = LocalDateTime.of(2025, 3, 12, 9, 0))

      useCase(listOf(State("1", LocalDateTime.of(2025, 3, 15, 9, 0), 5L, true)))

      verify(exactly = 1) { jobScheduler.scheduleReminder(any()) }
    }

  @Test
  fun `does not schedule the alarm when restored reminder is overdue again`() =
    runTest {
      stored(event = LocalDateTime.of(2025, 3, 12, 9, 0))

      useCase(listOf(State("1", LocalDateTime.of(2025, 3, 8, 9, 0), 5L, true)))

      verify(exactly = 0) { jobScheduler.scheduleReminder(any()) }
    }

  @Test
  fun `ignores states of reminders that no longer exist`() =
    runTest {
      coEvery { repository.getById("gone") } returns null

      useCase(listOf(State("gone", null, 0L, true)))

      coVerify(exactly = 0) { saveReminderUseCase(any()) }
    }
}
