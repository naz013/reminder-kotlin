package com.github.naz013.logic.habit.nudge

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.logic.habit.HabitStreakCalculator
import com.github.naz013.logic.habit.usecase.CheckInHabitUseCase
import com.github.naz013.logic.routine.HabitConfig
import com.github.naz013.repository.HabitCheckInRepository
import com.github.naz013.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class HabitNudgeProcessorTest {
  private val today = LocalDate.of(2026, 10, 20)
  private val now = LocalDateTime.of(2026, 10, 20, 20, 0)
  private val habitRepository = mockk<HabitRepository>()
  private val checkInRepository = mockk<HabitCheckInRepository>()
  private val schedule = mockk<ScheduleHabitNudgeUseCase>(relaxed = true)
  private val checkIn = mockk<CheckInHabitUseCase>(relaxed = true)
  private val notifier = mockk<HabitNudgeNotifier>(relaxed = true)
  private val config = mockk<HabitConfig> { every { isEnabled } returns true }
  private val nowProvider = mockk<NowDateTimeProvider> {
    every { nowDate() } returns today
    every { nowDateTime() } returns now
  }
  private val processor = HabitNudgeProcessor(
    habitRepository,
    checkInRepository,
    HabitStreakCalculator(),
    HabitNudgeCalculator(),
    nowProvider,
    config,
    schedule,
    checkIn,
    notifier
  )

  private fun habit(time: LocalTime? = LocalTime.of(20, 0)) = Habit(
    id = "h-1",
    reminderTime = time,
    createdAt = now,
    updatedAt = now
  )

  private fun doneToday() = HabitCheckIn(
    habitId = "h-1",
    date = today,
    count = 1,
    isCompleted = true,
    createdAt = now,
    updatedAt = now
  )

  @Test
  fun `shows notification and reschedules when habit is not done`() = runTest {
    val habit = habit()
    coEvery { habitRepository.getById("h-1") } returns habit
    coEvery { checkInRepository.getRange("h-1", any(), any()) } returns emptyList()

    processor.onNudgeDue("h-1")

    verify { schedule(habit) }
    verify { notifier.show(habit) }
  }

  @Test
  fun `does not show notification when today is already done`() = runTest {
    val habit = habit()
    coEvery { habitRepository.getById("h-1") } returns habit
    coEvery { checkInRepository.getRange("h-1", any(), any()) } returns listOf(doneToday())

    processor.onNudgeDue("h-1")

    verify { schedule(habit) }
    verify(exactly = 0) { notifier.show(any()) }
  }

  @Test
  fun `does not show notification when habits are disabled`() = runTest {
    val habit = habit()
    every { config.isEnabled } returns false
    coEvery { habitRepository.getById("h-1") } returns habit

    processor.onNudgeDue("h-1")

    verify(exactly = 0) { notifier.show(any()) }
  }

  @Test
  fun `does nothing when habit no longer exists`() = runTest {
    coEvery { habitRepository.getById("h-1") } returns null

    processor.onNudgeDue("h-1")

    verify(exactly = 0) { schedule(any()) }
    verify(exactly = 0) { notifier.show(any()) }
  }

  @Test
  fun `check in action dismisses notification and checks in the habit`() = runTest {
    coEvery { habitRepository.getById("h-1") } returns habit()
    coEvery { checkInRepository.getRange("h-1", any(), any()) } returns emptyList()

    processor.onCheckIn("h-1")

    verify { notifier.cancel("h-1") }
    coVerify(exactly = 1) { checkIn(any()) }
  }
}
