package com.github.naz013.logic.habit.nudge

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.logic.routine.HabitConfig
import com.github.naz013.scheduler.JobSchedulerApi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId

class ScheduleHabitNudgeUseCaseTest {
  private val now = LocalDateTime.of(2026, 10, 20, 9, 0)
  private val jobScheduler = mockk<JobSchedulerApi>(relaxed = true)
  private val config = mockk<HabitConfig> { every { isEnabled } returns true }
  private val nowProvider = mockk<NowDateTimeProvider> { every { nowDateTime() } returns now }
  private val useCase = ScheduleHabitNudgeUseCase(jobScheduler, HabitNudgeCalculator(), nowProvider, config)

  private fun habit(time: LocalTime?) = Habit(id = "h-1", reminderTime = time, createdAt = now, updatedAt = now)

  @Test
  fun `cancels the nudge when habits are disabled`() {
    every { config.isEnabled } returns false

    useCase(habit(LocalTime.of(20, 0)))

    verify { jobScheduler.cancelHabitNudge("h-1") }
    verify(exactly = 0) { jobScheduler.scheduleHabitNudge(any(), any()) }
  }

  @Test
  fun `cancels the nudge when the habit has no reminder time`() {
    useCase(habit(null))

    verify { jobScheduler.cancelHabitNudge("h-1") }
    verify(exactly = 0) { jobScheduler.scheduleHabitNudge(any(), any()) }
  }

  @Test
  fun `schedules the next trigger in epoch millis`() {
    val expected = LocalDateTime.of(2026, 10, 20, 20, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    useCase(habit(LocalTime.of(20, 0)))

    verify { jobScheduler.scheduleHabitNudge("h-1", expected) }
    verify(exactly = 0) { jobScheduler.cancelHabitNudge(any()) }
  }
}
