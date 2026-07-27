package com.elementary.tasks.workflow

import com.github.naz013.domain.workflow.WorkflowTemplateCategory

data class WorkflowGalleryState(
  val isLoading: Boolean = true,
  val globalRules: List<UiWorkflowRule> = emptyList(),
  val templatesByCategory: Map<WorkflowTemplateCategory, List<UiWorkflowTemplate>> = emptyMap(),
)
