package com.github.naz013.appwidgets.habit

import com.github.naz013.domain.habit.Habit
import com.github.naz013.logic.habit.HabitWithProgress
import com.github.naz013.logic.habit.usecase.CheckInHabitUseCase
import com.github.naz013.logic.habit.usecase.ObserveHabitsWithProgressUseCase
import com.github.naz013.logic.routine.HabitConfig
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class HabitCheckInHandlerTest {

  private val stamp = LocalDateTime.of(2026, 10, 1, 8, 0)
  private val habitConfig = mockk<HabitConfig>()
  private val observeUseCase = mockk<ObserveHabitsWithProgressUseCase>()
  private val checkInUseCase = mockk<CheckInHabitUseCase>(relaxed = true)
  private val handler = HabitCheckInHandler(habitConfig, observeUseCase, checkInUseCase)

  private val progress = HabitWithProgress(
    habit = Habit(id = "h-1", createdAt = stamp, updatedAt = stamp),
    todayCount = 0,
    periodCount = 0,
    streak = 0
  )

  @Before
  fun setUp() {
    every { habitConfig.isEnabled } returns true
    every { observeUseCase() } returns flowOf(listOf(progress))
  }

  @Test
  fun `checks in the tapped habit with its current progress`() = runTest {
    val result = handler.checkIn("h-1")

    assertTrue(result)
    coVerify(exactly = 1) { checkInUseCase(progress) }
  }

  @Test
  fun `does nothing when the habit no longer exists`() = runTest {
    val result = handler.checkIn("missing")

    assertFalse(result)
    coVerify(exactly = 0) { checkInUseCase(any()) }
  }

  @Test
  fun `does nothing when habits are unavailable`() = runTest {
    every { habitConfig.isEnabled } returns false

    val result = handler.checkIn("h-1")

    assertFalse(result)
    coVerify(exactly = 0) { checkInUseCase(any()) }
  }
}
