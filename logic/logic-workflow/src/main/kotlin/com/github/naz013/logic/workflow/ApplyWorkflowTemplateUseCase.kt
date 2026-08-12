package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.type
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.repository.WorkflowTemplateRepository

/** Applies a gallery [WorkflowTemplate] to one target, creating a concrete [WorkflowRule]. */
class ApplyWorkflowTemplateUseCase(
  private val workflowTemplateRepository: WorkflowTemplateRepository,
  private val workflowRuleRepository: WorkflowRuleRepository
) {

  /** Returns null if [scope]'s type isn't in [template]'s supportedScopeTypes — a future UI
   * shouldn't offer that combination in the first place, but this stays defensive. */
  suspend operator fun invoke(template: WorkflowTemplate, scope: WorkflowScope): WorkflowRule? {
    if (scope.type() !in template.supportedScopeTypes) return null

    val rule = WorkflowRule(
      title = template.title,
      templateId = template.id,
      scope = scope,
      trigger = template.trigger,
      action = template.action
    )
    workflowRuleRepository.save(rule)
    workflowTemplateRepository.save(template.copy(useCount = template.useCount + 1))
    return rule
  }
}
