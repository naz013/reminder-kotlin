package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.repository.WorkflowRuleRepository

/** Answers "which rules are attached to this reminder" — the reverse lookup from
 * [WorkflowRule.scope] (a rule points at its reminder id; the reminder doesn't store rule ids
 * back), by querying the indexed `scopeType`/`scopeId` columns via [WorkflowRuleRepository.getByScope]. */
class GetWorkflowRulesForReminderUseCase(
  private val workflowRuleRepository: WorkflowRuleRepository
) {

  suspend operator fun invoke(reminderId: String): List<WorkflowRule> =
    workflowRuleRepository.getByScope(scopeType = SCOPE_TYPE_REMINDER, scopeId = reminderId)

  companion object {
    private const val SCOPE_TYPE_REMINDER = "REMINDER"
  }
}
