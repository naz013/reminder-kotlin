package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.snooze.SnoozePresetResolver
import com.github.naz013.logic.reminder.snooze.SnoozePresetType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class SnoozeReminderToPresetUseCaseTest {
  private val snoozeReminderUseCase = mockk<SnoozeReminderUseCase>()
  private val preferences = mockk<ReminderPreferences>()
  private val reminder = ReminderV2(
    uuId = "id-1",
    summary = "Test",
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 5, 10, 9, 0)),
  )
  private val snoozed = reminder.copy(snoozeCount = 1)

  @Before
  fun setUp() {
    every { preferences.snoozeEveningTime } returns "18:00"
    every { preferences.snoozeMorningTime } returns "08:00"
    coEvery { snoozeReminderUseCase(any(), any()) } returns snoozed
  }

  private fun useCase(now: String) =
    SnoozeReminderToPresetUseCaseImpl(
      snoozeReminderUseCase = snoozeReminderUseCase,
      resolver = SnoozePresetResolver(),
      reminderPreferences = preferences,
      clock = Clock.fixed(Instant.parse(now), ZoneId.of("UTC")),
    )

  @Test
  fun `snoozes by preset minutes for relative preset`() = runTest {
    val result = useCase("2026-05-10T10:00:00Z")(reminder, SnoozePresetType.HOUR_1)

    assertSame(snoozed, result)
    coVerify(exactly = 1) { snoozeReminderUseCase(reminder, 60) }
  }

  @Test
  fun `snoozes until evening time when it is later today`() = runTest {
    useCase("2026-05-10T10:00:00Z")(reminder, SnoozePresetType.THIS_EVENING)

    coVerify(exactly = 1) { snoozeReminderUseCase(reminder, 480) }
  }

  @Test
  fun `snoozes until evening time tomorrow when it already passed`() = runTest {
    useCase("2026-05-10T19:00:00Z")(reminder, SnoozePresetType.THIS_EVENING)

    coVerify(exactly = 1) { snoozeReminderUseCase(reminder, 23 * 60) }
  }

  @Test
  fun `rounds partial minutes up`() = runTest {
    useCase("2026-05-10T07:59:30Z")(reminder, SnoozePresetType.TOMORROW_MORNING)

    coVerify(exactly = 1) { snoozeReminderUseCase(reminder, 24 * 60 + 1) }
  }

  @Test
  fun `uses default time when stored evening time is invalid`() = runTest {
    every { preferences.snoozeEveningTime } returns "not-a-time"

    useCase("2026-05-10T10:00:00Z")(reminder, SnoozePresetType.THIS_EVENING)

    coVerify(exactly = 1) { snoozeReminderUseCase(reminder, 480) }
  }
}
