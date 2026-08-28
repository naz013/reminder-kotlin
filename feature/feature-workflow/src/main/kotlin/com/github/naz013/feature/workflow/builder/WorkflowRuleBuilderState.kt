package com.github.naz013.feature.workflow.builder

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTrigger
import org.threeten.bp.LocalDateTime

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
  val revertOnEndDate: Boolean = false,
  val endDateTime: LocalDateTime? = null,
  val didSave: Boolean = false
) {
  /** A "revert on end date" option is only offered when creating (not editing) a rule that
   * schedules a notification override - the classic vacation-mode shape. Saving with it checked
   * creates a second, paired rule that reverts the override at [endDateTime] - see
   * `WorkflowRuleBuilderViewModel.saveNewRule`. */
  val showRevertOnEndDateOption: Boolean
    get() = editingRuleId == null &&
      trigger is WorkflowTrigger.ScheduleReached &&
      action is WorkflowAction.ApplyNotificationOverride

  val canSave: Boolean
    get() = trigger != null && action != null && (!showRevertOnEndDateOption || !revertOnEndDate || endDateTime != null)
}
