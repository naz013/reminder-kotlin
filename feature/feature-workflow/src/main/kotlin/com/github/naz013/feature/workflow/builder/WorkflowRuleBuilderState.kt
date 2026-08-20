package com.github.naz013.feature.workflow.builder

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTrigger

internal data class UiWorkflowGroupOption(
  val id: String,
  val title: String
)

internal data class UiWorkflowReminderOption(
  val id: String,
  val title: String
)

/** Builder state for a single rule: exactly one trigger slot, zero-or-more condition slots, and
 * exactly one action slot - not the reminder builder's open bag of 0-or-more-of-many-types. */
internal data class WorkflowRuleBuilderState(
  val isLoading: Boolean = true,
  val scopeType: WorkflowScopeType = WorkflowScopeType.GLOBAL,
  val editingRuleId: String? = null,
  val trigger: WorkflowTrigger? = null,
  val conditions: List<WorkflowCondition> = emptyList(),
  val action: WorkflowAction? = null,
  val isTriggerPickerVisible: Boolean = false,
  val isConditionPickerVisible: Boolean = false,
  val editingConditionIndex: Int? = null,
  val isActionPickerVisible: Boolean = false,
  val availableGroups: List<UiWorkflowGroupOption> = emptyList(),
  val availableReminders: List<UiWorkflowReminderOption> = emptyList(),
  val didSave: Boolean = false
) {
  val canSave: Boolean get() = trigger != null && action != null
}
