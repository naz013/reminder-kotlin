package com.github.naz013.feature.workflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.usecase.reminders.ApplyWorkflowTemplateUseCase
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
 * via the [com.github.naz013.feature.workflow.builder.WorkflowRuleBuilderScreen], toggle/delete an
 * existing rule, or save one back as a reusable template. */
class WorkflowGalleryViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val getGlobalWorkflowRulesUseCase: GetGlobalWorkflowRulesUseCase,
  private val getWorkflowTemplatesUseCase: GetWorkflowTemplatesUseCase,
  private val applyWorkflowTemplateUseCase: ApplyWorkflowTemplateUseCase,
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
}
