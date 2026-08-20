package com.github.naz013.feature.workflow

import com.github.naz013.domain.workflow.WorkflowTemplateCategory

internal data class WorkflowRulesForGroupState(
  val isLoading: Boolean = true,
  val rules: List<UiWorkflowRule> = emptyList(),
  val templatesByCategory: Map<WorkflowTemplateCategory, List<UiWorkflowTemplate>> = emptyMap(),
)
