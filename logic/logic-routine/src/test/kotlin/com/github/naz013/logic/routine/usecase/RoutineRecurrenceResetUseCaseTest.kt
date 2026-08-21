package com.github.naz013.logic.routine.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineStep
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class RoutineRecurrenceResetUseCaseTest {
  private val nowDateTimeProvider = mockk<NowDateTimeProvider>()
  private val saveRoutineUseCase = mockk<SaveRoutineUseCase>()

  private lateinit var useCase: RoutineRecurrenceResetUseCase

  private val today = LocalDate.of(2026, 7, 22)
  private val now = LocalDateTime.of(2026, 7, 22, 9, 0)

  @Before
  fun setUp() {
    every { nowDateTimeProvider.nowDate() } returns today
    every { nowDateTimeProvider.nowDateTime() } returns now
    coEvery { saveRoutineUseCase(any()) } answers { firstArg() }
    useCase = RoutineRecurrenceResetUseCase(nowDateTimeProvider, saveRoutineUseCase)
  }

  @Test
  fun `invoke leaves an on-demand routine untouched`() = runTest {
    val routine = Routine(
      id = "id-1",
      title = "On demand",
      recurrence = null,
      steps = listOf(RoutineStep(isCompleted = true)),
      createdAt = now,
      updatedAt = now
    )

    val result = useCase(routine)

    assertEquals(routine, result)
    coVerify(exactly = 0) { saveRoutineUseCase(any()) }
  }

  @Test
  fun `invoke resets steps when lastResetAt is from a previous day`() = runTest {
    val routine = Routine(
      id = "id-2",
      title = "Morning routine",
      recurrence = RecurrenceRule.Daily(),
      steps = listOf(RoutineStep(id = "s1", isCompleted = true)),
      lastResetAt = LocalDateTime.of(2026, 7, 21, 9, 0),
      createdAt = now,
      updatedAt = now
    )

    val result = useCase(routine)

    assertTrue(result.steps.none { it.isCompleted })
    assertEquals(now, result.lastResetAt)
    coVerify(exactly = 1) { saveRoutineUseCase(match { it.steps.all { s -> !s.isCompleted } }) }
  }

  @Test
  fun `invoke resets steps when never reset before`() = runTest {
    val routine = Routine(
      id = "id-3",
      title = "New recurring routine",
      recurrence = RecurrenceRule.Daily(),
      steps = listOf(RoutineStep(id = "s1", isCompleted = true)),
      lastResetAt = null,
      createdAt = now,
      updatedAt = now
    )

    val result = useCase(routine)

    assertTrue(result.steps.none { it.isCompleted })
    coVerify(exactly = 1) { saveRoutineUseCase(any()) }
  }

  @Test
  fun `invoke does nothing when already reset today`() = runTest {
    val routine = Routine(
      id = "id-4",
      title = "Already reset",
      recurrence = RecurrenceRule.Daily(),
      steps = listOf(RoutineStep(id = "s1", isCompleted = false)),
      lastResetAt = LocalDateTime.of(2026, 7, 22, 7, 0),
      createdAt = now,
      updatedAt = now
    )

    val result = useCase(routine)

    assertEquals(routine, result)
    coVerify(exactly = 0) { saveRoutineUseCase(any()) }
  }
}
