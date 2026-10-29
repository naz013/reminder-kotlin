package com.github.naz013.logic.reminder.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.LocationBasedStrategyV2
import com.github.naz013.logic.reminder.behavior.ReminderBehaviorStrategyV2
import com.github.naz013.logic.reminder.usecase.RescheduleRemindersUseCase.SkipReason
import com.github.naz013.logic.reminder.usecase.RescheduleRemindersUseCase.Target
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import java.util.TimeZone

class RescheduleRemindersUseCaseTest {
  private val originalZone: TimeZone = TimeZone.getDefault()
  private val nowProvider = mockk<NowDateTimeProvider>()
  private val repository = mockk<ReminderV2Repository>()
  private val strategyResolver = mockk<BehaviorStrategyResolverV2>()
  private val strategy = mockk<ReminderBehaviorStrategyV2>()
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>()
  private val activated = slot<ReminderV2>()

  private lateinit var useCase: RescheduleRemindersUseCase

  private val now = LocalDateTime.of(2025, 3, 10, 12, 0)

  @Before
  fun setUp() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    every { nowProvider.nowDateTime() } returns now
    every { nowProvider.nowInstant() } returns Instant.parse("2025-03-10T12:00:00Z")
    every { strategyResolver.resolve(any()) } returns strategy
    every { strategy.canSkip(any()) } returns false
    coEvery { activateReminderUseCase(capture(activated), any(), any()) } answers { activated.captured }

    useCase = RescheduleRemindersUseCase(
      reminderRepository = repository,
      strategyResolver = strategyResolver,
      activateReminderUseCase = activateReminderUseCase,
      dateTimeManager = DateTimeManager(nowProvider, mockk(relaxed = true)),
    )
  }

  @After
  fun tearDown() {
    TimeZone.setDefault(originalZone)
  }

  private fun reminder(
    id: String = "1",
    event: LocalDateTime? = LocalDateTime.of(2025, 3, 8, 15, 30),
    recurrence: RecurrenceRule = RecurrenceRule.Once,
    zoneId: String? = null,
  ) = ReminderV2(
    uuId = id,
    recurrence = recurrence,
    timeZoneId = zoneId,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2025, 3, 1, 9, 0), eventDateTime = event),
  ).also { coEvery { repository.getById(id) } returns it }

  @Test
  fun `moves one-time reminder to today keeping its time of day`() =
    runTest {
      reminder(event = LocalDateTime.of(2025, 3, 8, 15, 30))

      val summary = useCase(listOf("1"), Target.Today)

      assertEquals(1, summary.moved)
      assertEquals(LocalDateTime.of(2025, 3, 10, 15, 30), activated.captured.schedule.eventDateTime)
    }

  @Test
  fun `moves one-time reminder to now plus one hour when today time already passed`() =
    runTest {
      reminder(event = LocalDateTime.of(2025, 3, 8, 9, 0))

      useCase(listOf("1"), Target.Today)

      assertEquals(LocalDateTime.of(2025, 3, 10, 13, 0), activated.captured.schedule.eventDateTime)
    }

  @Test
  fun `moves one-time reminder to tomorrow keeping its time of day`() =
    runTest {
      reminder(event = LocalDateTime.of(2025, 3, 8, 9, 0))

      useCase(listOf("1"), Target.Tomorrow)

      assertEquals(LocalDateTime.of(2025, 3, 11, 9, 0), activated.captured.schedule.eventDateTime)
    }

  @Test
  fun `moves one-time reminder to the picked date time`() =
    runTest {
      reminder()

      useCase(listOf("1"), Target.Picked(LocalDateTime.of(2025, 4, 2, 18, 45)))

      assertEquals(LocalDateTime.of(2025, 4, 2, 18, 45), activated.captured.schedule.eventDateTime)
    }

  @Test
  fun `fixed zone tomorrow landing in a spring gap is shifted forward on the utc timeline`() =
    runTest {
      every { nowProvider.nowDateTime() } returns LocalDateTime.of(2025, 3, 29, 11, 0)
      every { nowProvider.nowInstant() } returns Instant.parse("2025-03-29T11:00:00Z")
      reminder(event = LocalDateTime.of(2025, 3, 28, 1, 30), zoneId = "Europe/Berlin")

      useCase(listOf("1"), Target.Tomorrow)

      assertEquals(LocalDateTime.of(2025, 3, 30, 1, 30), activated.captured.schedule.eventDateTime)
    }

  private fun nextDailyAtNine() {
    every { strategy.canSkip(any()) } returns true
    every { strategy.calculateNextOccurrence(any(), any()) } answers {
      secondArg<LocalDateTime>().toLocalDate().plusDays(1).atTime(9, 0)
    }
  }

  @Test
  fun `skips recurring reminder forward through several overdue cycles with one activation`() =
    runTest {
      reminder(event = LocalDateTime.of(2025, 3, 6, 9, 0), recurrence = RecurrenceRule.Daily())
      nextDailyAtNine()

      val summary = useCase(listOf("1"), Target.Tomorrow)

      assertEquals(1, summary.moved)
      coVerify(exactly = 1) { activateReminderUseCase(any(), any(), any()) }
      assertEquals(LocalDateTime.of(2025, 3, 11, 9, 0), activated.captured.schedule.eventDateTime)
      assertEquals(0L, activated.captured.eventCount)
    }

  @Test
  fun `reports recurring reminder that is not overdue as nothing to move`() =
    runTest {
      reminder(event = LocalDateTime.of(2025, 3, 12, 9, 0), recurrence = RecurrenceRule.Daily())
      nextDailyAtNine()

      val summary = useCase(listOf("1"), Target.Today)

      assertEquals(SkipReason.NOTHING_TO_MOVE, summary.skipped.single().reason)
    }

  @Test
  fun `reports recurring reminder without next occurrence as nothing to move`() =
    runTest {
      reminder(recurrence = RecurrenceRule.Daily())
      every { strategy.canSkip(any()) } returns true
      every { strategy.calculateNextOccurrence(any(), any()) } returns null

      val summary = useCase(listOf("1"), Target.Today)

      assertEquals(SkipReason.NOTHING_TO_MOVE, summary.skipped.single().reason)
    }

  @Test
  fun `reports recurring reminder that stays overdue after the skip limit`() =
    runTest {
      reminder(recurrence = RecurrenceRule.Daily())
      every { strategy.canSkip(any()) } returns true
      every { strategy.calculateNextOccurrence(any(), any()) } answers { secondArg<LocalDateTime>().plusSeconds(1) }

      val summary = useCase(listOf("1"), Target.Today)

      assertEquals(SkipReason.STILL_OVERDUE, summary.skipped.single().reason)
      coVerify(exactly = 0) { activateReminderUseCase(any(), any(), any()) }
    }

  @Test
  fun `reports recurring reminder that cannot skip as nothing to move`() =
    runTest {
      reminder(recurrence = RecurrenceRule.Daily())

      val summary = useCase(listOf("1"), Target.Today)

      assertEquals(SkipReason.NOTHING_TO_MOVE, summary.skipped.single().reason)
      coVerify(exactly = 0) { activateReminderUseCase(any(), any(), any()) }
    }

  @Test
  fun `rejects picked target in the past`() =
    runTest {
      reminder()

      val summary = useCase(listOf("1"), Target.Picked(LocalDateTime.of(2025, 3, 9, 8, 0)))

      assertEquals(SkipReason.TARGET_IN_PAST, summary.skipped.single().reason)
      coVerify(exactly = 0) { activateReminderUseCase(any(), any(), any()) }
    }

  @Test
  fun `reports reminder as not activated when activation leaves it inactive`() =
    runTest {
      reminder()
      coEvery { activateReminderUseCase(capture(activated), any(), any()) } answers {
        activated.captured.copy(isActive = false)
      }

      val summary = useCase(listOf("1"), Target.Tomorrow)

      assertEquals(0, summary.moved)
      assertEquals(SkipReason.NOT_ACTIVATED, summary.skipped.single().reason)
    }

  @Test
  fun `reports location based reminder as unsupported`() =
    runTest {
      reminder()
      every { strategyResolver.resolve(any()) } returns LocationBasedStrategyV2

      val summary = useCase(listOf("1"), Target.Today)

      assertEquals(SkipReason.UNSUPPORTED_TYPE, summary.skipped.single().reason)
    }

  @Test
  fun `reports countdown reminder as unsupported`() =
    runTest {
      reminder(recurrence = RecurrenceRule.Countdown(after = 60_000))

      val summary = useCase(listOf("1"), Target.Today)

      assertEquals(SkipReason.UNSUPPORTED_TYPE, summary.skipped.single().reason)
      coVerify(exactly = 0) { activateReminderUseCase(any(), any(), any()) }
    }

  @Test
  fun `reports missing id as not found`() =
    runTest {
      coEvery { repository.getById("missing") } returns null

      val summary = useCase(listOf("missing"), Target.Today)

      assertEquals(SkipReason.NOT_FOUND, summary.skipped.single().reason)
      assertEquals("missing", summary.skipped.single().id)
    }

  @Test
  fun `summary counts moved and skipped reminders`() =
    runTest {
      reminder(id = "1")
      reminder(id = "2")
      coEvery { repository.getById("3") } returns null

      val summary = useCase(listOf("1", "2", "3"), Target.Tomorrow)

      assertEquals(2, summary.moved)
      assertEquals(listOf("3"), summary.skipped.map { it.id })
    }
}
