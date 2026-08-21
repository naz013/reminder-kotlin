package com.github.naz013.logic.routine.usecase

import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.repository.RoutineRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ResetRoutineStepsUseCaseTest {
  private val routineRepository = mockk<RoutineRepository>()
  private val saveRoutineUseCase = mockk<SaveRoutineUseCase>()

  private lateinit var useCase: ResetRoutineStepsUseCase

  private val now = LocalDateTime.of(2026, 7, 22, 9, 0)

  @Before
  fun setUp() {
    useCase = ResetRoutineStepsUseCase(routineRepository, saveRoutineUseCase)
  }

  @Test
  fun `invoke unchecks all completed steps and saves`() = runTest {
    val routine = Routine(
      id = "id-1",
      title = "Morning routine",
      steps = listOf(
        RoutineStep(id = "s1", isCompleted = true),
        RoutineStep(id = "s2", isCompleted = false)
      ),
      createdAt = now,
      updatedAt = now
    )
    coEvery { routineRepository.getById("id-1") } returns routine
    coEvery { saveRoutineUseCase(any()) } answers { firstArg() }

    val result = useCase("id-1")

    assertTrue(result!!.steps.none { it.isCompleted })
    coVerify(exactly = 1) {
      saveRoutineUseCase(match { r -> r.steps.all { !it.isCompleted } })
    }
  }

  @Test
  fun `invoke skips saving when no steps are completed`() = runTest {
    val routine = Routine(
      id = "id-2",
      title = "Already fresh",
      steps = listOf(RoutineStep(id = "s1", isCompleted = false)),
      createdAt = now,
      updatedAt = now
    )
    coEvery { routineRepository.getById("id-2") } returns routine

    val result = useCase("id-2")

    assertEquals(routine, result)
    coVerify(exactly = 0) { saveRoutineUseCase(any()) }
  }

  @Test
  fun `invoke returns null when the routine is not found`() = runTest {
    coEvery { routineRepository.getById("missing") } returns null

    val result = useCase("missing")

    assertNull(result)
    coVerify(exactly = 0) { saveRoutineUseCase(any()) }
  }
}
