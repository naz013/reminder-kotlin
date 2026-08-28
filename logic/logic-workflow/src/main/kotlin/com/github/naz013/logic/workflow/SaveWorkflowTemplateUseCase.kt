package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.files.DataType
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.WorkflowTemplateRepository

/** The one place a [WorkflowTemplate] write is saved *and* scheduled for cloud/local backup
 * upload - see [SaveWorkflowRuleUseCase] for the rule-side counterpart. */
class SaveWorkflowTemplateUseCase(
  private val workflowTemplateRepository: WorkflowTemplateRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {

  @IgnorableReturnValue
  suspend operator fun invoke(template: WorkflowTemplate): WorkflowTemplate {
    workflowTemplateRepository.save(template)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.WorkflowTemplates,
      id = template.id
    )
    return template
  }
}
