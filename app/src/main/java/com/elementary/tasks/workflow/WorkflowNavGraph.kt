package com.elementary.tasks.workflow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Contributes the Workflow island's screens (Nav3 entries) into the app's single, shared
 * [androidx.navigation3.ui.NavDisplay] (see [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
fun EntryProviderScope<NavKey>.workflowEntries(backStack: MutableList<NavKey>) {
  entry<WorkflowNavKey.Gallery> { WorkflowGalleryEntry(backStack) }
  entry<WorkflowNavKey.RulesForGroup> { key -> WorkflowRulesForGroupEntry(key, backStack) }
}

@Composable
private fun WorkflowGalleryEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<WorkflowGalleryViewModel>()
  val state by viewModel.state.collectAsState()
  WorkflowGalleryScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onRuleEnabledChange = viewModel::onRuleEnabledChange,
    onDeleteRuleClick = viewModel::onDeleteRuleClick,
    onSaveRuleAsTemplateClick = viewModel::onSaveRuleAsTemplateClick,
    onApplyTemplateClick = viewModel::onApplyTemplateClick,
    onCreateRuleClick = viewModel::onCreateRuleClick,
    onCreateRuleDaysChange = viewModel::onCreateRuleDaysChange,
    onCreateRuleConfirm = viewModel::onCreateRuleConfirm,
    onCreateRuleDismiss = viewModel::onCreateRuleDismiss,
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
    onBackClick = { backStack.removeLastOrNull() },
    onRuleEnabledChange = viewModel::onRuleEnabledChange,
    onDeleteRuleClick = viewModel::onDeleteRuleClick,
    onSaveRuleAsTemplateClick = viewModel::onSaveRuleAsTemplateClick,
    onApplyTemplateClick = viewModel::onApplyTemplateClick,
    onCreateRuleClick = viewModel::onCreateRuleClick,
    onCreateRuleOptionSelected = viewModel::onCreateRuleOptionSelected,
    onCreateRuleDaysChange = viewModel::onCreateRuleDaysChange,
    onCreateRuleConfirm = viewModel::onCreateRuleConfirm,
    onCreateRuleDismiss = viewModel::onCreateRuleDismiss,
  )
}
