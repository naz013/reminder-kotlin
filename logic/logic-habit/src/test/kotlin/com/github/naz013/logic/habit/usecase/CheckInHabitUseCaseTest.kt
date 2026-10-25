package com.github.naz013.logic.habit.usecase

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.logic.habit.HabitWithProgress
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.threeten.bp.LocalDateTime

class CheckInHabitUseCaseTest {
  private val stamp = LocalDateTime.of(2026, 10, 1, 8, 0)
  private val toggleUseCase = mockk<ToggleHabitCheckInUseCase>(relaxed = true)
  private val incrementUseCase = mockk<IncrementHabitCheckInUseCase>(relaxed = true)
  private val useCase = CheckInHabitUseCase(toggleUseCase, incrementUseCase)

  private fun progress(frequency: HabitFrequency, target: Int, todayCount: Int) = HabitWithProgress(
    habit = Habit(id = "h-1", frequency = frequency, targetCount = target, createdAt = stamp, updatedAt = stamp),
    todayCount = todayCount,
    periodCount = todayCount,
    streak = 0
  )

  @Test
  fun `toggles a single-target daily habit`() = runBlocking {
    useCase(progress(HabitFrequency.DAILY, target = 1, todayCount = 0))

    coVerify(exactly = 1) { toggleUseCase("h-1") }
    coVerify(exactly = 0) { incrementUseCase(any()) }
  }

  @Test
  fun `increments a multi-target daily habit below target`() = runBlocking {
    useCase(progress(HabitFrequency.DAILY, target = 3, todayCount = 1))

    coVerify(exactly = 1) { incrementUseCase("h-1") }
    coVerify(exactly = 0) { toggleUseCase(any()) }
  }

  @Test
  fun `clears a multi-target daily habit that reached its target`() = runBlocking {
    useCase(progress(HabitFrequency.DAILY, target = 3, todayCount = 3))

    coVerify(exactly = 1) { toggleUseCase("h-1") }
  }

  @Test
  fun `increments a weekly habit with nothing logged today`() = runBlocking {
    useCase(progress(HabitFrequency.WEEKLY, target = 3, todayCount = 0))

    coVerify(exactly = 1) { incrementUseCase("h-1") }
    coVerify(exactly = 0) { toggleUseCase(any()) }
  }

  @Test
  fun `clears a weekly habit that has progress today`() = runBlocking {
    useCase(progress(HabitFrequency.WEEKLY, target = 3, todayCount = 1))

    coVerify(exactly = 1) { toggleUseCase("h-1") }
    coVerify(exactly = 0) { incrementUseCase(any()) }
  }
}
