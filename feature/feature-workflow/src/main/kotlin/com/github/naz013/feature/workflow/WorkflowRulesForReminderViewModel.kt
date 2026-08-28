package com.github.naz013.feature.workflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logic.workflow.ApplyWorkflowTemplateUseCase
import com.github.naz013.logic.workflow.DeleteWorkflowRuleUseCase
import com.github.naz013.logic.workflow.GetWorkflowRulesForReminderUseCase
import com.github.naz013.logic.workflow.GetWorkflowTemplatesUseCase
import com.github.naz013.logic.workflow.SaveWorkflowRuleAsTemplateUseCase
import com.github.naz013.logic.workflow.SaveWorkflowRuleUseCase
import com.github.naz013.logic.workflow.isExecutable
import com.github.naz013.repository.WorkflowRuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Reminder-scope workflow rule management, reached from a reminder preview's overflow menu. Same
 * shape as [WorkflowRulesForGroupViewModel] but scoped to one [reminderId] - the only entry point
 * that can create a [WorkflowScope.ForReminder] rule, which is what the `LocationEntered`/
 * `LocationExited` triggers require (see [com.github.naz013.logic.workflow.WorkflowEngine]). */
internal class WorkflowRulesForReminderViewModel(
  private val reminderId: String,
  private val dispatcherProvider: DispatcherProvider,
  private val getWorkflowRulesForReminderUseCase: GetWorkflowRulesForReminderUseCase,
  private val getWorkflowTemplatesUseCase: GetWorkflowTemplatesUseCase,
  private val applyWorkflowTemplateUseCase: ApplyWorkflowTemplateUseCase,
  private val saveWorkflowRuleAsTemplateUseCase: SaveWorkflowRuleAsTemplateUseCase,
  private val saveWorkflowRuleUseCase: SaveWorkflowRuleUseCase,
  private val deleteWorkflowRuleUseCase: DeleteWorkflowRuleUseCase,
  private val workflowRuleRepository: WorkflowRuleRepository,
) : ViewModel() {

  val state: StateFlow<WorkflowRulesForReminderState> field = MutableStateFlow(WorkflowRulesForReminderState())

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      loadData()
    }
  }

  fun onRuleEnabledChange(ruleId: String, isEnabled: Boolean) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val rule = workflowRuleRepository.getById(ruleId) ?: return@launch
      saveWorkflowRuleUseCase(rule.copy(isEnabled = isEnabled))
      loadData()
    }
  }

  fun onDeleteRuleClick(ruleId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteWorkflowRuleUseCase(ruleId)
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
      val template = getWorkflowTemplatesUseCase()
        .firstOrNull { it.id == templateId && it.isExecutable() } ?: return@launch
      applyWorkflowTemplateUseCase(template, WorkflowScope.ForReminder(reminderId))
      loadData()
    }
  }

  private suspend fun loadData() {
    val reminderRules = getWorkflowRulesForReminderUseCase(reminderId)
    val appliedTemplateIds = reminderRules.mapNotNull { it.templateId }.toSet()
    val rules = reminderRules.map { it.toUi() }
    val templates = getWorkflowTemplatesUseCase()
      .filter { it.isExecutable() }
      .map { it.toUi(WorkflowScopeType.REMINDER, appliedTemplateIds) }
      .groupBy { it.category }
    withContext(dispatcherProvider.main()) {
      state.update { it.copy(isLoading = false, rules = rules, templatesByCategory = templates) }
    }
  }
}
