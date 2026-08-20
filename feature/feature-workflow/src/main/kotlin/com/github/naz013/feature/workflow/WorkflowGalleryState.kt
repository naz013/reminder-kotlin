package com.github.naz013.feature.workflow

import com.github.naz013.domain.workflow.WorkflowTemplateCategory

internal data class WorkflowGalleryState(
  val isLoading: Boolean = true,
  val globalRules: List<UiWorkflowRule> = emptyList(),
  val templatesByCategory: Map<WorkflowTemplateCategory, List<UiWorkflowTemplate>> = emptyMap(),
)
