package com.github.naz013.logic.routine.usecase

import com.github.naz013.domain.TaggedItemType
import com.github.naz013.files.DataType
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.RoutineExecutionRepository
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteRoutineUseCaseTest {
  private val routineRepository = mockk<RoutineRepository>(relaxed = true)
  private val routineExecutionRepository = mockk<RoutineExecutionRepository>(relaxed = true)
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>(relaxed = true)
  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)

  private lateinit var useCase: DeleteRoutineUseCase

  @Before
  fun setUp() {
    useCase = DeleteRoutineUseCase(
      routineRepository = routineRepository,
      routineExecutionRepository = routineExecutionRepository,
      tagAssignmentRepository = tagAssignmentRepository,
      scheduleBackgroundWorkUseCase = scheduleBackgroundWorkUseCase
    )
  }

  @Test
  fun `invoke deletes the routine, its executions, tag assignments, and schedules a delete upload`() = runTest {
    useCase("id-1")

    coVerify(exactly = 1) { routineRepository.delete("id-1") }
    coVerify(exactly = 1) { routineExecutionRepository.deleteByRoutineId("id-1") }
    coVerify(exactly = 1) { tagAssignmentRepository.detachAll("id-1", TaggedItemType.ROUTINE) }
    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.Routines,
        id = "id-1"
      )
    }
  }
}
