package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTemplate

/** Promotes a rule's trigger/action into a reusable, user-created [WorkflowTemplate], and links
 * the originating rule back to it via [WorkflowRule.templateId]. */
class SaveWorkflowRuleAsTemplateUseCase(
  private val saveWorkflowTemplateUseCase: SaveWorkflowTemplateUseCase,
  private val saveWorkflowRuleUseCase: SaveWorkflowRuleUseCase
) {

  @IgnorableReturnValue
  suspend operator fun invoke(rule: WorkflowRule, title: String = rule.title): WorkflowTemplate {
    val template = WorkflowTemplate(
      title = title,
      trigger = rule.trigger,
      action = rule.action,
      isBuiltIn = false,
      // Broadest default: no UI yet to let the user narrow which scope types the template supports.
      supportedScopeTypes = WorkflowScopeType.entries
    )
    saveWorkflowTemplateUseCase(template)
    saveWorkflowRuleUseCase(rule.copy(templateId = template.id))
    return template
  }
}
