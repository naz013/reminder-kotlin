package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.repository.WorkflowRuleRepository

/** Answers "which rules apply everywhere" — same reverse-lookup shape as
 * [GetWorkflowRulesForReminderUseCase]/[GetWorkflowRulesForGroupUseCase], for the
 * `WorkflowScope.Global` case (no id — `scopeId` is always null for a global rule). */
class GetGlobalWorkflowRulesUseCase(
  private val workflowRuleRepository: WorkflowRuleRepository
) {

  suspend operator fun invoke(): List<WorkflowRule> =
    workflowRuleRepository.getByScope(scopeType = SCOPE_TYPE_GLOBAL, scopeId = null)

  companion object {
    private const val SCOPE_TYPE_GLOBAL = "GLOBAL"
  }
}
