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
import com.github.naz013.usecase.reminders.GetGlobalWorkflowRulesUseCase
import com.github.naz013.usecase.reminders.GetWorkflowTemplatesUseCase
import com.github.naz013.usecase.reminders.SaveWorkflowRuleAsTemplateUseCase
import com.github.naz013.usecase.reminders.isExecutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Global-scope workflow rule management: the screen behind the Home header tile and the
 * Settings "Workflow rules" row. Lists rules already applied globally and the full template
 * gallery grouped by category, and lets a user apply a template globally, create a fresh rule
 * from the one currently-executable trigger/action combo, toggle/delete an existing rule, or
 * save one back as a reusable template. */
class WorkflowGalleryViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val getGlobalWorkflowRulesUseCase: GetGlobalWorkflowRulesUseCase,
  private val getWorkflowTemplatesUseCase: GetWorkflowTemplatesUseCase,
  private val applyWorkflowTemplateUseCase: ApplyWorkflowTemplateUseCase,
  private val createWorkflowRuleUseCase: CreateWorkflowRuleUseCase,
  private val saveWorkflowRuleAsTemplateUseCase: SaveWorkflowRuleAsTemplateUseCase,
  private val workflowRuleRepository: WorkflowRuleRepository,
) : ViewModel() {

  val state: StateFlow<WorkflowGalleryState> field = MutableStateFlow(WorkflowGalleryState())

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
      val template = getWorkflowTemplatesUseCase().firstOrNull { it.id == templateId && it.isExecutable() } ?: return@launch
      applyWorkflowTemplateUseCase(template, WorkflowScope.Global)
      loadData()
    }
  }

  fun onCreateRuleClick() {
    state.update { it.copy(isCreateRuleDialogVisible = true, createRuleDays = "30") }
  }

  fun onCreateRuleDaysChange(days: String) {
    state.update { it.copy(createRuleDays = days.filter(Char::isDigit).take(MAX_DAYS_DIGITS)) }
  }

  fun onCreateRuleConfirm() {
    val days = state.value.createRuleDays.toIntOrNull()?.takeIf { it > 0 } ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      createWorkflowRuleUseCase(
        title = "Archive completed reminders after $days days",
        scope = WorkflowScope.Global,
        trigger = WorkflowTrigger.ReminderAgeExceeded(days),
        action = WorkflowAction.ArchiveReminder,
      )
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
    val rules = getGlobalWorkflowRulesUseCase().map { it.toUi() }
    val templates = getWorkflowTemplatesUseCase()
      .filter { it.isExecutable() }
      .map { it.toUi(WorkflowScopeType.GLOBAL) }
      .groupBy { it.category }
    withContext(dispatcherProvider.main()) {
      state.update { it.copy(isLoading = false, globalRules = rules, templatesByCategory = templates) }
    }
  }

  companion object {
    private const val MAX_DAYS_DIGITS = 4
  }
}
