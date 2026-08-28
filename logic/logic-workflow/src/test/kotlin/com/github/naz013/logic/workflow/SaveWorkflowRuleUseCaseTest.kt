package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.files.DataType
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.WorkflowRuleRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveWorkflowRuleUseCaseTest {
  private val workflowRuleRepository = mockk<WorkflowRuleRepository>(relaxed = true)
  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)

  private val useCase = SaveWorkflowRuleUseCase(workflowRuleRepository, scheduleBackgroundWorkUseCase)

  @Test
  fun `invoke saves the rule`() = runTest {
    val rule = WorkflowRule(
      uuId = "rule-1",
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.ArchiveReminder
    )

    useCase(rule)

    coVerify(exactly = 1) { workflowRuleRepository.save(rule) }
  }

  @Test
  fun `invoke schedules an upload for the rule's DataType`() = runTest {
    val rule = WorkflowRule(
      uuId = "rule-2",
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.ArchiveReminder
    )

    useCase(rule)

    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Upload,
        dataType = DataType.WorkflowRules,
        id = "rule-2"
      )
    }
  }
}
