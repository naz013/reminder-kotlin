package com.github.naz013.feature.workflow

import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory

internal data class UiWorkflowRule(
  val id: String,
  val title: String,
  val isEnabled: Boolean,
  val canSaveAsTemplate: Boolean,
)

internal data class UiWorkflowTemplate(
  val id: String,
  val title: String,
  val description: String?,
  val category: WorkflowTemplateCategory,
  val canApply: Boolean,
  val alreadyApplied: Boolean,
)

internal fun WorkflowRule.toUi(): UiWorkflowRule = UiWorkflowRule(
  id = uuId,
  title = title,
  isEnabled = isEnabled,
  canSaveAsTemplate = templateId == null,
)

/** [appliedTemplateIds] are the templateIds of rules already applied in the current scope - a
 * template already active there is offered as already-applied rather than letting the user create
 * a duplicate rule for it (see the "Save as template"-produced [WorkflowRule.templateId] link this
 * relies on). */
internal fun WorkflowTemplate.toUi(
  scopeType: WorkflowScopeType,
  appliedTemplateIds: Set<String>,
): UiWorkflowTemplate {
  val alreadyApplied = id in appliedTemplateIds
  return UiWorkflowTemplate(
    id = id,
    title = title,
    description = description,
    category = category,
    canApply = scopeType in supportedScopeTypes && !alreadyApplied,
    alreadyApplied = alreadyApplied,
  )
}
