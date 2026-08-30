package com.github.naz013.feature.workflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.logic.workflow.ApplyWorkflowTemplateUseCase
import com.github.naz013.logic.workflow.DeleteWorkflowRuleUseCase
import com.github.naz013.logic.workflow.GetWorkflowTemplatesUseCase
import com.github.naz013.logic.workflow.SaveWorkflowRuleAsTemplateUseCase
import com.github.naz013.logic.workflow.SaveWorkflowRuleUseCase
import com.github.naz013.logic.workflow.isExecutable
import com.github.naz013.repository.WorkflowRuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Global-scope workflow rule management: the screen behind the Home header tile and the
 * Settings "Workflow rules" row. Lists rules already applied globally and the full template
 * gallery grouped by category, and lets a user apply a template globally, create a fresh rule
 * via the [com.github.naz013.feature.workflow.builder.WorkflowRuleBuilderScreen], toggle/delete an
 * existing rule, or save one back as a reusable template. */
internal class WorkflowGalleryViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val getWorkflowTemplatesUseCase: GetWorkflowTemplatesUseCase,
  private val applyWorkflowTemplateUseCase: ApplyWorkflowTemplateUseCase,
  private val saveWorkflowRuleAsTemplateUseCase: SaveWorkflowRuleAsTemplateUseCase,
  private val saveWorkflowRuleUseCase: SaveWorkflowRuleUseCase,
  private val deleteWorkflowRuleUseCase: DeleteWorkflowRuleUseCase,
  private val workflowRuleRepository: WorkflowRuleRepository,
) : ViewModel() {

  val state: StateFlow<WorkflowGalleryState> field = MutableStateFlow(WorkflowGalleryState())

  init {
    viewModelScope.launch(dispatcherProvider.default()) {
      workflowRuleRepository.observeByScope(scopeType = SCOPE_TYPE_GLOBAL, scopeId = null)
        .collect { rules -> applyRules(rules) }
    }
  }

  fun onRuleEnabledChange(ruleId: String, isEnabled: Boolean) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val rule = workflowRuleRepository.getById(ruleId) ?: return@launch
      saveWorkflowRuleUseCase(rule.copy(isEnabled = isEnabled))
    }
  }

  fun onDeleteRuleClick(ruleId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteWorkflowRuleUseCase(ruleId)
    }
  }

  fun onSaveRuleAsTemplateClick(ruleId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val rule = workflowRuleRepository.getById(ruleId) ?: return@launch
      saveWorkflowRuleAsTemplateUseCase(rule)
    }
  }

  fun onApplyTemplateClick(templateId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val template = getWorkflowTemplatesUseCase().firstOrNull { it.id == templateId && it.isExecutable() } ?: return@launch
      applyWorkflowTemplateUseCase(template, WorkflowScope.Global)
    }
  }

  // Driven by workflowRuleRepository.observeByScope in init - no manual reload needed, the Flow
  // re-emits on its own once a rule save/delete goes through.
  private suspend fun applyRules(globalRules: List<WorkflowRule>) {
    val appliedTemplateIds = globalRules.mapNotNull { it.templateId }.toSet()
    val rules = globalRules.map { it.toUi() }
    val templates = getWorkflowTemplatesUseCase()
      .filter { it.isExecutable() }
      .map { it.toUi(WorkflowScopeType.GLOBAL, appliedTemplateIds) }
      .groupBy { it.category }
    state.update { it.copy(isLoading = false, globalRules = rules, templatesByCategory = templates) }
  }

  companion object {
    private const val SCOPE_TYPE_GLOBAL = "GLOBAL"
  }
}
