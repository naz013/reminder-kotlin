package com.github.naz013.feature.workflow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.workflow.builder.WorkflowRuleBuilderScreen
import com.github.naz013.feature.workflow.builder.WorkflowRuleBuilderViewModel
import com.github.naz013.domain.workflow.WorkflowScopeType
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Contributes the Workflow island's screens (Nav3 entries) into the app's single, shared
 * [androidx.navigation3.ui.NavDisplay] (see [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
fun EntryProviderScope<NavKey>.workflowEntries(backStack: MutableList<NavKey>) {
  entry<WorkflowNavKey.Gallery> { WorkflowGalleryEntry(backStack) }
  entry<WorkflowNavKey.RulesForGroup> { key -> WorkflowRulesForGroupEntry(key, backStack) }
  entry<WorkflowNavKey.Builder> { key -> WorkflowBuilderEntry(key, backStack) }
}

@Composable
private fun WorkflowGalleryEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<WorkflowGalleryViewModel>()
  val state by viewModel.state.collectAsState()
  WorkflowGalleryScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onRuleEnabledChange = viewModel::onRuleEnabledChange,
    onDeleteRuleClick = viewModel::onDeleteRuleClick,
    onSaveRuleAsTemplateClick = viewModel::onSaveRuleAsTemplateClick,
    onApplyTemplateClick = viewModel::onApplyTemplateClick,
    onCreateRuleClick = { backStack.add(WorkflowNavKey.Builder(scopeType = WorkflowScopeType.GLOBAL.name)) },
  )
}

@Composable
private fun WorkflowRulesForGroupEntry(
  key: WorkflowNavKey.RulesForGroup,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<WorkflowRulesForGroupViewModel> { parametersOf(key.groupId) }
  val state by viewModel.state.collectAsState()
  WorkflowRulesForGroupScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onRuleEnabledChange = viewModel::onRuleEnabledChange,
    onDeleteRuleClick = viewModel::onDeleteRuleClick,
    onSaveRuleAsTemplateClick = viewModel::onSaveRuleAsTemplateClick,
    onApplyTemplateClick = viewModel::onApplyTemplateClick,
    onCreateRuleClick = {
      backStack.add(WorkflowNavKey.Builder(scopeType = WorkflowScopeType.GROUP.name, scopeId = key.groupId))
    },
  )
}

@Composable
private fun WorkflowBuilderEntry(
  key: WorkflowNavKey.Builder,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<WorkflowRuleBuilderViewModel> {
    parametersOf(WorkflowScopeType.valueOf(key.scopeType), key.scopeId, key.editingRuleId)
  }
  val state by viewModel.state.collectAsState()
  LaunchedEffect(state.didSave) {
    if (state.didSave && backStack.size > 1) backStack.removeLastOrNull()
  }
  WorkflowRuleBuilderScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onTriggerRowClick = viewModel::onTriggerRowClick,
    onRemoveTriggerClick = viewModel::onRemoveTriggerClick,
    onAddConditionClick = viewModel::onAddConditionClick,
    onEditConditionClick = viewModel::onEditConditionClick,
    onRemoveConditionClick = viewModel::onRemoveConditionClick,
    onActionRowClick = viewModel::onActionRowClick,
    onRemoveActionClick = viewModel::onRemoveActionClick,
    onSaveClick = viewModel::onSaveClick,
    onTriggerPickerDismiss = viewModel::onTriggerPickerDismiss,
    onTriggerSelected = viewModel::onTriggerSelected,
    onConditionPickerDismiss = viewModel::onConditionPickerDismiss,
    onConditionSelected = viewModel::onConditionSelected,
    onActionPickerDismiss = viewModel::onActionPickerDismiss,
    onActionSelected = viewModel::onActionSelected,
  )
}
