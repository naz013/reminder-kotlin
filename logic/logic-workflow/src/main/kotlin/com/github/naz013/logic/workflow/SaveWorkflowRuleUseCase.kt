package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.files.DataType
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.logic.schedule.WorkType
import com.github.naz013.repository.WorkflowRuleRepository

/** The one place a [WorkflowRule] write is saved *and* scheduled for cloud/local backup upload -
 * every rule save (create, edit, toggle, template-link) should go through this rather than
 * calling [WorkflowRuleRepository.save] directly, so a rule can never silently skip sync. */
class SaveWorkflowRuleUseCase(
  private val workflowRuleRepository: WorkflowRuleRepository,
  private val scheduleBackgroundWorkUseCase: ScheduleBackgroundWorkUseCase
) {

  @IgnorableReturnValue
  suspend operator fun invoke(rule: WorkflowRule): WorkflowRule {
    workflowRuleRepository.save(rule)
    scheduleBackgroundWorkUseCase(
      workType = WorkType.Upload,
      dataType = DataType.WorkflowRules,
      id = rule.uuId
    )
    return rule
  }
}
