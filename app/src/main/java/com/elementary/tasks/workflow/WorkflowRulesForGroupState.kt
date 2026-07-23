package com.elementary.tasks.workflow

import com.github.naz013.domain.workflow.WorkflowTemplateCategory

data class WorkflowRulesForGroupState(
  val isLoading: Boolean = true,
  val rules: List<UiWorkflowRule> = emptyList(),
  val templatesByCategory: Map<WorkflowTemplateCategory, List<UiWorkflowTemplate>> = emptyMap(),
  val isCreateRuleDialogVisible: Boolean = false,
  val createRuleOption: CreateGroupRuleOption = CreateGroupRuleOption.ARCHIVE_BY_AGE,
  val createRuleDays: String = "30",
)

/** The two trigger×action combos the engine actually executes for a group-scoped rule this pass
 * — see [com.github.naz013.usecase.reminders.WorkflowEngine]. */
enum class CreateGroupRuleOption { ARCHIVE_BY_AGE, ARCHIVE_ON_COMPLETION }
