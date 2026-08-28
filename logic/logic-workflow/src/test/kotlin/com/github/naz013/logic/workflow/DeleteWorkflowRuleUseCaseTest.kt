package com.github.naz013.logic.workflow

import com.github.naz013.files.DataType
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.WorkflowRuleRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteWorkflowRuleUseCaseTest {
  private val workflowRuleRepository = mockk<WorkflowRuleRepository>(relaxed = true)
  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)

  private val useCase = DeleteWorkflowRuleUseCase(workflowRuleRepository, scheduleBackgroundWorkUseCase)

  @Test
  fun `invoke deletes the rule`() = runTest {
    useCase("rule-1")

    coVerify(exactly = 1) { workflowRuleRepository.delete("rule-1") }
  }

  @Test
  fun `invoke schedules a delete for the rule's DataType`() = runTest {
    useCase("rule-1")

    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Delete,
        dataType = DataType.WorkflowRules,
        id = "rule-1"
      )
    }
  }
}
