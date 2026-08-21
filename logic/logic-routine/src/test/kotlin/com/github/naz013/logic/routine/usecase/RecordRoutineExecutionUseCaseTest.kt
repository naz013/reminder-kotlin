package com.github.naz013.logic.routine.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import com.github.naz013.repository.RoutineExecutionRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class RecordRoutineExecutionUseCaseTest {
  private val routineExecutionRepository = mockk<RoutineExecutionRepository>(relaxed = true)
  private val nowDateTimeProvider = mockk<NowDateTimeProvider>()

  private lateinit var useCase: RecordRoutineExecutionUseCase

  private val now = LocalDateTime.of(2026, 7, 22, 9, 0)

  @Before
  fun setUp() {
    every { nowDateTimeProvider.nowDateTime() } returns now
    useCase = RecordRoutineExecutionUseCase(routineExecutionRepository, nowDateTimeProvider)
  }

  @Test
  fun `invoke builds and saves a record stamped with the current time`() = runTest {
    val result = useCase(
      routineId = "routine-1",
      completedStepIds = listOf("s1", "s2"),
      totalTimeSpentSeconds = 900,
      totalStepsCount = 3
    )

    assertEquals("routine-1", result.routineId)
    assertEquals(now, result.executedAt)
    assertEquals(listOf("s1", "s2"), result.completedStepIds)
    assertEquals(2, result.completedStepsCount)
    assertEquals(3, result.totalStepsCount)
    coVerify(exactly = 1) { routineExecutionRepository.save(result) }
  }
}
