package com.elementary.tasks.workflow

import com.github.naz013.domain.workflow.WorkflowTemplateCategory

data class WorkflowRulesForGroupState(
  val isLoading: Boolean = true,
  val rules: List<UiWorkflowRule> = emptyList(),
  val templatesByCategory: Map<WorkflowTemplateCategory, List<UiWorkflowTemplate>> = emptyMap(),
)
