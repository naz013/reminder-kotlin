package com.github.naz013.logic.routine.usecase

import com.github.naz013.domain.routine.Routine
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ToggleRoutinePinUseCaseTest {
  private val saveRoutineUseCase = mockk<SaveRoutineUseCase>()

  private lateinit var useCase: ToggleRoutinePinUseCase

  @Before
  fun setUp() {
    useCase = ToggleRoutinePinUseCase(saveRoutineUseCase)
  }

  @Test
  fun `invoke flips isPinned and delegates the save to SaveRoutineUseCase`() = runTest {
    val routine = Routine(
      id = "id-1",
      title = "Morning routine",
      isPinned = false,
      createdAt = LocalDateTime.of(2026, 7, 22, 9, 0),
      updatedAt = LocalDateTime.of(2026, 7, 22, 9, 0)
    )
    coEvery { saveRoutineUseCase(any()) } answers { firstArg() }

    val result = useCase(routine)

    assertTrue(result.isPinned)
    coVerify(exactly = 1) { saveRoutineUseCase(routine.copy(isPinned = true)) }
    assertEquals(routine.id, result.id)
  }
}
