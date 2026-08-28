package com.github.naz013.feature.workflow.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.workflow.ScheduleRecurrence
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logic.workflow.CreateWorkflowRuleUseCase
import com.github.naz013.logic.workflow.SaveWorkflowRuleUseCase
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.WorkflowRuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import java.util.UUID

/** Builds and saves a single [com.github.naz013.domain.workflow.WorkflowRule], either from
 * scratch or (when [editingRuleId] is non-null) editing an existing one. [scopeType]/[scopeId]
 * fix the rule's [WorkflowScope] for the whole session - the entry point (Gallery = global, a
 * group's rules screen = that group) decides scope, the builder only fills in trigger/conditions/
 * action. */
internal class WorkflowRuleBuilderViewModel(
  private val scopeType: WorkflowScopeType,
  private val scopeId: String?,
  private val editingRuleId: String?,
  private val dispatcherProvider: DispatcherProvider,
  private val workflowRuleRepository: WorkflowRuleRepository,
  private val createWorkflowRuleUseCase: CreateWorkflowRuleUseCase,
  private val saveWorkflowRuleUseCase: SaveWorkflowRuleUseCase,
  private val reminderV2Repository: ReminderV2Repository,
  private val groupV2Repository: GroupV2Repository,
) : ViewModel() {

  val state: StateFlow<WorkflowRuleBuilderState> field =
    MutableStateFlow(WorkflowRuleBuilderState(scopeType = scopeType, editingRuleId = editingRuleId))

  init {
    viewModelScope.launch(dispatcherProvider.default()) { loadData() }
  }

  private suspend fun loadData() {
    val groups = groupV2Repository.getAll().map { UiWorkflowGroupOption(id = it.uuId, title = it.title) }
    val reminders = reminderV2Repository.getAll(active = true, removed = false)
      .map { UiWorkflowReminderOption(id = it.uuId, title = it.summary) }
    val existingRule = editingRuleId?.let { workflowRuleRepository.getById(it) }
    withContext(dispatcherProvider.main()) {
      state.update {
        it.copy(
          isLoading = false,
          availableGroups = groups,
          availableReminders = reminders,
          trigger = existingRule?.trigger,
          conditions = existingRule?.conditions ?: emptyList(),
          action = existingRule?.action,
        )
      }
    }
  }

  fun onTriggerRowClick() {
    state.update { it.copy(isTriggerPickerVisible = true) }
  }

  fun onTriggerPickerDismiss() {
    state.update { it.copy(isTriggerPickerVisible = false) }
  }

  fun onTriggerSelected(trigger: WorkflowTrigger) {
    state.update { it.copy(trigger = trigger, isTriggerPickerVisible = false) }
  }

  fun onRemoveTriggerClick() {
    state.update { it.copy(trigger = null) }
  }

  fun onAddConditionClick() {
    state.update { it.copy(isConditionPickerVisible = true, editingConditionIndex = null) }
  }

  fun onEditConditionClick(index: Int) {
    state.update { it.copy(isConditionPickerVisible = true, editingConditionIndex = index) }
  }

  fun onConditionPickerDismiss() {
    state.update { it.copy(isConditionPickerVisible = false, editingConditionIndex = null) }
  }

  fun onConditionSelected(condition: WorkflowCondition) {
    state.update { current ->
      val index = current.editingConditionIndex
      val updatedConditions =
        if (index != null) {
          current.conditions.toMutableList().also { it[index] = condition }
        } else {
          current.conditions + condition
        }
      current.copy(conditions = updatedConditions, isConditionPickerVisible = false, editingConditionIndex = null)
    }
  }

  fun onRemoveConditionClick(index: Int) {
    state.update { current ->
      current.copy(conditions = current.conditions.toMutableList().also { it.removeAt(index) })
    }
  }

  fun onActionRowClick() {
    state.update { it.copy(isActionPickerVisible = true) }
  }

  fun onActionPickerDismiss() {
    state.update { it.copy(isActionPickerVisible = false) }
  }

  fun onActionSelected(action: WorkflowAction) {
    state.update { it.copy(action = action, isActionPickerVisible = false) }
  }

  fun onRemoveActionClick() {
    state.update { it.copy(action = null) }
  }

  fun onRevertOnEndDateChange(enabled: Boolean) {
    state.update { it.copy(revertOnEndDate = enabled, endDateTime = if (enabled) it.endDateTime else null) }
  }

  fun onEndDateTimeSelected(dateTime: LocalDateTime) {
    state.update { it.copy(endDateTime = dateTime) }
  }

  fun onSaveClick() {
    val current = state.value
    val trigger = current.trigger ?: return
    val action = current.action ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      val editingId = editingRuleId
      if (editingId != null) {
        workflowRuleRepository.getById(editingId)?.let { existing ->
          saveWorkflowRuleUseCase(
            existing.copy(trigger = trigger, conditions = current.conditions, action = action)
          )
        }
      } else {
        saveNewRule(current, trigger, action)
      }
      withContext(dispatcherProvider.main()) {
        state.update { it.copy(didSave = true) }
      }
    }
  }

  /** Creates the rule as configured, and - if [WorkflowRuleBuilderState.showRevertOnEndDateOption]
   * is checked - a second, paired rule that reverts the notification override at
   * [WorkflowRuleBuilderState.endDateTime]. Both rules share a freshly-minted id in
   * [com.github.naz013.domain.workflow.WorkflowRule.templateId] - repurposed here as a pairing id
   * rather than a real gallery template id, which also means neither rule offers "save as
   * template" afterwards (see `WorkflowRule.toUi`'s `canSaveAsTemplate`), which is desirable: only
   * half of the pair would be captured. */
  private suspend fun saveNewRule(current: WorkflowRuleBuilderState, trigger: WorkflowTrigger, action: WorkflowAction) {
    val endDateTime = current.endDateTime
    val pairId = if (current.showRevertOnEndDateOption && current.revertOnEndDate && endDateTime != null) {
      UUID.randomUUID().toString()
    } else {
      null
    }
    createWorkflowRuleUseCase(
      title = autoTitle(trigger, action),
      scope = scope(),
      trigger = trigger,
      conditions = current.conditions,
      action = action,
      templateId = pairId,
    )
    if (pairId != null && endDateTime != null) {
      createWorkflowRuleUseCase(
        title = autoTitle(WorkflowTrigger.ScheduleReached(endDateTime), WorkflowAction.ClearNotificationOverride),
        scope = scope(),
        trigger = WorkflowTrigger.ScheduleReached(atDateTime = endDateTime, recurrence = ScheduleRecurrence.ONCE),
        action = WorkflowAction.ClearNotificationOverride,
        templateId = pairId,
      )
    }
  }

  private fun scope(): WorkflowScope = when (scopeType) {
    WorkflowScopeType.GLOBAL -> WorkflowScope.Global
    WorkflowScopeType.GROUP -> WorkflowScope.ForGroup(scopeId.orEmpty())
    WorkflowScopeType.REMINDER -> WorkflowScope.ForReminder(scopeId.orEmpty())
  }

  /** Plain, non-localized sentence for [com.github.naz013.domain.workflow.WorkflowRule.title] -
   * matches the existing precedent set by the (now-retired) hardcoded creation dialogs, whose
   * auto-generated titles were never localized either. */
  private fun autoTitle(trigger: WorkflowTrigger, action: WorkflowAction): String =
    "When ${autoTitleTriggerText(trigger)}, ${autoTitleActionText(action)}"

  private fun autoTitleTriggerText(trigger: WorkflowTrigger): String = when (trigger) {
    is WorkflowTrigger.ReminderCreated -> "a reminder is created"
    is WorkflowTrigger.ReminderCompleted -> "reminder completed"
    is WorkflowTrigger.ReminderSnoozedNTimes -> "snoozed ${trigger.count} times"
    is WorkflowTrigger.GroupAllCompleted -> "group fully completed"
    is WorkflowTrigger.LocationEntered -> "location entered"
    is WorkflowTrigger.LocationExited -> "location exited"
    is WorkflowTrigger.ReminderAgeExceeded -> "completed for ${trigger.days} days"
    is WorkflowTrigger.ReminderUnacknowledgedFor -> "unacknowledged for ${trigger.minutes} minutes"
    is WorkflowTrigger.ScheduleReached -> "a scheduled time is reached"
  }

  private fun autoTitleActionText(action: WorkflowAction): String = when (action) {
    is WorkflowAction.ArchiveReminder -> "archive it"
    is WorkflowAction.CompleteReminder -> "complete it"
    is WorkflowAction.PurgeReminder -> "delete it permanently"
    is WorkflowAction.ApplyNotificationOverride -> "change its notification settings"
    is WorkflowAction.ClearNotificationOverride -> "clear its notification override"
    is WorkflowAction.ActivateReminder -> "activate another reminder"
    is WorkflowAction.MoveToGroup -> "move it to another group"
    is WorkflowAction.SendBroadcastIntent -> "send a broadcast intent"
    is WorkflowAction.RunBackgroundTask -> "run a background task"
  }
}
