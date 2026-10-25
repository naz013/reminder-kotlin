package com.github.naz013.logic.habit.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.logic.habit.HabitHistoryBuilder
import com.github.naz013.logic.habit.HabitStreakCalculator
import com.github.naz013.repository.HabitCheckInRepository
import com.github.naz013.repository.HabitRepository
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class ObserveHabitDetailsUseCaseTest {
  private val now = LocalDateTime.of(2026, 10, 14, 12, 0)
  private val today = now.toLocalDate()
  private val habitRepository = mockk<HabitRepository>()
  private val checkInRepository = mockk<HabitCheckInRepository>()
  private val nowProvider = mockk<NowDateTimeProvider> {
    every { nowDate() } returns today
    every { nowDateTime() } returns now
  }
  private val useCase = ObserveHabitDetailsUseCase(
    habitRepository,
    checkInRepository,
    HabitStreakCalculator(),
    HabitHistoryBuilder(),
    nowProvider,
    mockDispatcherProvider()
  )

  private fun habit() = Habit(id = "h-1", targetCount = 1, createdAt = now, updatedAt = now)

  private fun checkIn(date: LocalDate) = HabitCheckIn(
    habitId = "h-1",
    date = date,
    count = 1,
    isCompleted = true,
    createdAt = now,
    updatedAt = now
  )

  @Test
  fun `combines the habit with streaks totals and history`() = runTest {
    every { habitRepository.observeById("h-1") } returns flowOf(habit())
    every { checkInRepository.observeRange("h-1", any(), today) } returns flowOf(
      listOf(checkIn(today), checkIn(today.minusDays(1)), checkIn(today.minusDays(5)))
    )

    val details = useCase("h-1", weeks = 2).first()!!

    assertEquals(1, details.todayCount)
    assertEquals(2, details.streak)
    assertEquals(2, details.longestStreak)
    assertEquals(3, details.totalCheckIns)
    assertEquals(today, details.days.last().date)
  }

  @Test
  fun `emits null when the habit does not exist`() = runTest {
    every { habitRepository.observeById("missing") } returns flowOf(null)

    assertNull(useCase("missing", weeks = 2).first())
  }
}
