package com.github.naz013.usecase.reminders

import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.repository.WorkflowRuleRepository

/** Answers "which rules are attached to this group" — same reverse-lookup shape as
 * [GetWorkflowRulesForReminderUseCase], for the `WorkflowScope.ForGroup` case. */
class GetWorkflowRulesForGroupUseCase(
  private val workflowRuleRepository: WorkflowRuleRepository
) {

  suspend operator fun invoke(groupId: String): List<WorkflowRule> =
    workflowRuleRepository.getByScope(scopeType = SCOPE_TYPE_GROUP, scopeId = groupId)

  companion object {
    private const val SCOPE_TYPE_GROUP = "GROUP"
  }
}
