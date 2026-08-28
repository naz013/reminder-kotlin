package com.github.naz013.logic.workflow

import com.github.naz013.files.DataType
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.WorkflowRuleRepository

/** The one place a [com.github.naz013.domain.workflow.WorkflowRule] is deleted *and* scheduled
 * for cloud/local backup deletion - see [SaveWorkflowRuleUseCase] for the save-side counterpart. */
class DeleteWorkflowRuleUseCase(
  private val workflowRuleRepository: WorkflowRuleRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {

  @IgnorableReturnValue
  suspend operator fun invoke(id: String) {
    workflowRuleRepository.delete(id)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Delete,
      dataType = DataType.WorkflowRules,
      id = id
    )
  }
}
