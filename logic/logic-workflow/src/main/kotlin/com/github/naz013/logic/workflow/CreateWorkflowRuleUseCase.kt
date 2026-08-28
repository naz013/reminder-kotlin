package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTrigger

/** Creates a rule from scratch (not from a template) — the "fresh one" path alongside
 * [ApplyWorkflowTemplateUseCase], so a future ViewModel has one consistent entry point for both. */
class CreateWorkflowRuleUseCase(
  private val saveWorkflowRuleUseCase: SaveWorkflowRuleUseCase
) {

  @IgnorableReturnValue
  suspend operator fun invoke(
    title: String,
    scope: WorkflowScope,
    trigger: WorkflowTrigger,
    conditions: List<WorkflowCondition> = emptyList(),
    action: WorkflowAction
  ): WorkflowRule {
    val rule = WorkflowRule(title = title, scope = scope, trigger = trigger, conditions = conditions, action = action)
    saveWorkflowRuleUseCase(rule)
    return rule
  }
}
