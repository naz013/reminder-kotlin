package com.elementary.tasks.workflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.usecase.reminders.ApplyWorkflowTemplateUseCase
import com.github.naz013.usecase.reminders.CreateWorkflowRuleUseCase
import com.github.naz013.usecase.reminders.GetWorkflowRulesForGroupUseCase
import com.github.naz013.usecase.reminders.GetWorkflowTemplatesUseCase
import com.github.naz013.usecase.reminders.SaveWorkflowRuleAsTemplateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Group-scope workflow rule management, reached from a group's "Workflow rules" row. Same shape
 * as [WorkflowGalleryViewModel] but scoped to one [groupId] — lists the rules already attached to
 * this group and offers the template gallery filtered to group-supporting templates, plus the two
 * trigger×action combos the engine can execute for a group ("archive by age" and "archive on
 * completion", the latter only meaningful for a concrete group). */
class WorkflowRulesForGroupViewModel(
  private val groupId: String,
  private val dispatcherProvider: DispatcherProvider,
  private val getWorkflowRulesForGroupUseCase: GetWorkflowRulesForGroupUseCase,
  private val getWorkflowTemplatesUseCase: GetWorkflowTemplatesUseCase,
  private val applyWorkflowTemplateUseCase: ApplyWorkflowTemplateUseCase,
  private val createWorkflowRuleUseCase: CreateWorkflowRuleUseCase,
  private val saveWorkflowRuleAsTemplateUseCase: SaveWorkflowRuleAsTemplateUseCase,
  private val workflowRuleRepository: WorkflowRuleRepository,
) : ViewModel() {

  val state: StateFlow<WorkflowRulesForGroupState> field = MutableStateFlow(WorkflowRulesForGroupState())

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      loadData()
    }
  }

  fun onRuleEnabledChange(ruleId: String, isEnabled: Boolean) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val rule = workflowRuleRepository.getById(ruleId) ?: return@launch
      workflowRuleRepository.save(rule.copy(isEnabled = isEnabled))
      loadData()
    }
  }

  fun onDeleteRuleClick(ruleId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      workflowRuleRepository.delete(ruleId)
      loadData()
    }
  }

  fun onSaveRuleAsTemplateClick(ruleId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val rule = workflowRuleRepository.getById(ruleId) ?: return@launch
      saveWorkflowRuleAsTemplateUseCase(rule)
      loadData()
    }
  }

  fun onApplyTemplateClick(templateId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val template = getWorkflowTemplatesUseCase().firstOrNull { it.id == templateId } ?: return@launch
      applyWorkflowTemplateUseCase(template, WorkflowScope.ForGroup(groupId))
      loadData()
    }
  }

  fun onCreateRuleClick() {
    state.update {
      it.copy(
        isCreateRuleDialogVisible = true,
        createRuleOption = CreateGroupRuleOption.ARCHIVE_BY_AGE,
        createRuleDays = "30",
      )
    }
  }

  fun onCreateRuleOptionSelected(option: CreateGroupRuleOption) {
    state.update { it.copy(createRuleOption = option) }
  }

  fun onCreateRuleDaysChange(days: String) {
    state.update { it.copy(createRuleDays = days.filter(Char::isDigit).take(MAX_DAYS_DIGITS)) }
  }

  fun onCreateRuleConfirm() {
    viewModelScope.launch(dispatcherProvider.default()) {
      when (state.value.createRuleOption) {
        CreateGroupRuleOption.ARCHIVE_BY_AGE -> {
          val days = state.value.createRuleDays.toIntOrNull()?.takeIf { it > 0 } ?: return@launch
          createWorkflowRuleUseCase(
            title = "Archive completed reminders after $days days",
            scope = WorkflowScope.ForGroup(groupId),
            trigger = WorkflowTrigger.ReminderAgeExceeded(days),
            action = WorkflowAction.ArchiveReminder,
          )
        }

        CreateGroupRuleOption.ARCHIVE_ON_COMPLETION -> {
          createWorkflowRuleUseCase(
            title = "Archive group once everything is completed",
            scope = WorkflowScope.ForGroup(groupId),
            trigger = WorkflowTrigger.GroupAllCompleted,
            action = WorkflowAction.ArchiveReminder,
          )
        }
      }
      withContext(dispatcherProvider.main()) {
        state.update { it.copy(isCreateRuleDialogVisible = false) }
      }
      loadData()
    }
  }

  fun onCreateRuleDismiss() {
    state.update { it.copy(isCreateRuleDialogVisible = false) }
  }

  private suspend fun loadData() {
    val rules = getWorkflowRulesForGroupUseCase(groupId).map { it.toUi() }
    val templates = getWorkflowTemplatesUseCase()
      .map { it.toUi(WorkflowScopeType.GROUP) }
      .groupBy { it.category }
    withContext(dispatcherProvider.main()) {
      state.update { it.copy(isLoading = false, rules = rules, templatesByCategory = templates) }
    }
  }

  companion object {
    private const val MAX_DAYS_DIGITS = 4
  }
}
