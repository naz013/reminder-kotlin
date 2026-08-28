package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.type

/** Applies a gallery [WorkflowTemplate] to one target, creating a concrete [WorkflowRule]. */
class ApplyWorkflowTemplateUseCase(
  private val saveWorkflowTemplateUseCase: SaveWorkflowTemplateUseCase,
  private val saveWorkflowRuleUseCase: SaveWorkflowRuleUseCase
) {

  /** Returns null if [scope]'s type isn't in [template]'s supportedScopeTypes — a future UI
   * shouldn't offer that combination in the first place, but this stays defensive. */
  @IgnorableReturnValue
  suspend operator fun invoke(template: WorkflowTemplate, scope: WorkflowScope): WorkflowRule? {
    if (scope.type() !in template.supportedScopeTypes) return null

    val rule = WorkflowRule(
      title = template.title,
      templateId = template.id,
      scope = scope,
      trigger = template.trigger,
      action = template.action
    )
    saveWorkflowRuleUseCase(rule)
    saveWorkflowTemplateUseCase(template.copy(useCount = template.useCount + 1))
    return rule
  }
}
