package com.elementary.tasks.workflow

import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory

data class UiWorkflowRule(
  val id: String,
  val title: String,
  val isEnabled: Boolean,
  val canSaveAsTemplate: Boolean,
)

data class UiWorkflowTemplate(
  val id: String,
  val title: String,
  val description: String?,
  val category: WorkflowTemplateCategory,
  val canApply: Boolean,
)

fun WorkflowRule.toUi(): UiWorkflowRule = UiWorkflowRule(
  id = uuId,
  title = title,
  isEnabled = isEnabled,
  canSaveAsTemplate = templateId == null,
)

fun WorkflowTemplate.toUi(scopeType: WorkflowScopeType): UiWorkflowTemplate = UiWorkflowTemplate(
  id = id,
  title = title,
  description = description,
  category = category,
  canApply = scopeType in supportedScopeTypes,
)
