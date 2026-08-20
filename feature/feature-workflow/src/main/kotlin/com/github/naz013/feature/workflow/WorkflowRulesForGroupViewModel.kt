package com.github.naz013.feature.workflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logic.workflow.ApplyWorkflowTemplateUseCase
import com.github.naz013.logic.workflow.GetWorkflowRulesForGroupUseCase
import com.github.naz013.logic.workflow.GetWorkflowTemplatesUseCase
import com.github.naz013.logic.workflow.SaveWorkflowRuleAsTemplateUseCase
import com.github.naz013.logic.workflow.isExecutable
import com.github.naz013.repository.WorkflowRuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Group-scope workflow rule management, reached from a group's "Workflow rules" row. Same shape
 * as [WorkflowGalleryViewModel] but scoped to one [groupId] — lists the rules already attached to
 * this group and offers the template gallery filtered to group-supporting templates, plus the
 * [com.github.naz013.feature.workflow.builder.WorkflowRuleBuilderScreen] to create a custom rule. */
internal class WorkflowRulesForGroupViewModel(
  private val groupId: String,
  private val dispatcherProvider: DispatcherProvider,
  private val getWorkflowRulesForGroupUseCase: GetWorkflowRulesForGroupUseCase,
  private val getWorkflowTemplatesUseCase: GetWorkflowTemplatesUseCase,
  private val applyWorkflowTemplateUseCase: ApplyWorkflowTemplateUseCase,
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
      val template = getWorkflowTemplatesUseCase().firstOrNull { it.id == templateId && it.isExecutable() } ?: return@launch
      applyWorkflowTemplateUseCase(template, WorkflowScope.ForGroup(groupId))
      loadData()
    }
  }

  private suspend fun loadData() {
    val rules = getWorkflowRulesForGroupUseCase(groupId).map { it.toUi() }
    val templates = getWorkflowTemplatesUseCase()
      .filter { it.isExecutable() }
      .map { it.toUi(WorkflowScopeType.GROUP) }
      .groupBy { it.category }
    withContext(dispatcherProvider.main()) {
      state.update { it.copy(isLoading = false, rules = rules, templatesByCategory = templates) }
    }
  }
}
