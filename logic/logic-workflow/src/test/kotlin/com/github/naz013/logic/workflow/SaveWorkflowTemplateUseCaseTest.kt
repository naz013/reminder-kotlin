package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.files.DataType
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.WorkflowTemplateRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveWorkflowTemplateUseCaseTest {
  private val workflowTemplateRepository = mockk<WorkflowTemplateRepository>(relaxed = true)
  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)

  private val useCase = SaveWorkflowTemplateUseCase(workflowTemplateRepository, scheduleBackgroundWorkUseCase)

  @Test
  fun `invoke saves the template`() = runTest {
    val template = WorkflowTemplate(
      id = "template-1",
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.ArchiveReminder
    )

    useCase(template)

    coVerify(exactly = 1) { workflowTemplateRepository.save(template) }
  }

  @Test
  fun `invoke schedules an upload for the template's DataType`() = runTest {
    val template = WorkflowTemplate(
      id = "template-2",
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.ArchiveReminder
    )

    useCase(template)

    coVerify(exactly = 1) {
      scheduleBackgroundWorkUseCase(
        workType = WorkType.Upload,
        dataType = DataType.WorkflowTemplates,
        id = "template-2"
      )
    }
  }
}
