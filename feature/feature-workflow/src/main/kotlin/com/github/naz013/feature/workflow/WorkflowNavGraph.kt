package com.github.naz013.feature.workflow

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.feature.workflow.builder.WorkflowRuleBuilderScreen
import com.github.naz013.feature.workflow.builder.WorkflowRuleBuilderViewModel
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.navigation.DetailPanePlaceholder
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Contributes the Workflow island's screens (Nav3 entries) into the app's single, shared
 * [androidx.navigation3.ui.NavDisplay] (see [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.workflowEntries(
  backStack: MutableList<NavKey>,
  isRenderedAsDetailPane: (NavKey) -> Boolean,
) {
  entry<WorkflowNavKey.Gallery>(
    metadata = ListDetailSceneStrategy.listPane(
      detailPlaceholder = {
        DetailPanePlaceholder(
          text = stringResource(R.string.select_workflow_template_to_configure),
          icon = AppIcons.Fluent.ArrowRepeatAll,
        )
      },
    ),
  ) { WorkflowGalleryEntry(backStack) }
  entry<WorkflowNavKey.RulesForGroup>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    WorkflowRulesForGroupEntry(key, backStack, renderAsDetailPane)
  }
  entry<WorkflowNavKey.RulesForReminder>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    WorkflowRulesForReminderEntry(key, backStack, renderAsDetailPane)
  }
  entry<WorkflowNavKey.Builder>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    WorkflowBuilderEntry(key, backStack, renderAsDetailPane)
  }
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
    onCreateRuleClick = {
      backStack.navigateToDetailPane(WorkflowNavKey.Builder(scopeType = WorkflowScopeType.GLOBAL.name))
    },
  )
}

@Composable
private fun WorkflowRulesForGroupEntry(
  key: WorkflowNavKey.RulesForGroup,
  backStack: MutableList<NavKey>,
  renderAsDetailPane: Boolean,
) {
  val viewModel = koinViewModel<WorkflowRulesForGroupViewModel> { parametersOf(key.groupId) }
  val state by viewModel.state.collectAsState()
  WorkflowRulesForGroupScreen(
    state = state,
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onRuleEnabledChange = viewModel::onRuleEnabledChange,
    onDeleteRuleClick = viewModel::onDeleteRuleClick,
    onSaveRuleAsTemplateClick = viewModel::onSaveRuleAsTemplateClick,
    onApplyTemplateClick = viewModel::onApplyTemplateClick,
    onCreateRuleClick = {
      backStack.navigateToDetailPane(
        WorkflowNavKey.Builder(scopeType = WorkflowScopeType.GROUP.name, scopeId = key.groupId),
      )
    },
  )
}

@Composable
private fun WorkflowRulesForReminderEntry(
  key: WorkflowNavKey.RulesForReminder,
  backStack: MutableList<NavKey>,
  renderAsDetailPane: Boolean,
) {
  val viewModel = koinViewModel<WorkflowRulesForReminderViewModel> { parametersOf(key.reminderId) }
  val state by viewModel.state.collectAsState()
  WorkflowRulesForReminderScreen(
    state = state,
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onRuleEnabledChange = viewModel::onRuleEnabledChange,
    onDeleteRuleClick = viewModel::onDeleteRuleClick,
    onSaveRuleAsTemplateClick = viewModel::onSaveRuleAsTemplateClick,
    onApplyTemplateClick = viewModel::onApplyTemplateClick,
    onCreateRuleClick = {
      backStack.navigateToDetailPane(
        WorkflowNavKey.Builder(scopeType = WorkflowScopeType.REMINDER.name, scopeId = key.reminderId),
      )
    },
  )
}

@Composable
private fun WorkflowBuilderEntry(
  key: WorkflowNavKey.Builder,
  backStack: MutableList<NavKey>,
  renderAsDetailPane: Boolean,
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
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onTriggerRowClick = viewModel::onTriggerRowClick,
    onRemoveTriggerClick = viewModel::onRemoveTriggerClick,
    onAddConditionClick = viewModel::onAddConditionClick,
    onEditConditionClick = viewModel::onEditConditionClick,
    onRemoveConditionClick = viewModel::onRemoveConditionClick,
    onActionRowClick = viewModel::onActionRowClick,
    onRemoveActionClick = viewModel::onRemoveActionClick,
    onRevertOnEndDateChange = viewModel::onRevertOnEndDateChange,
    onEndDateTimeSelected = viewModel::onEndDateTimeSelected,
    onSaveClick = viewModel::onSaveClick,
    onTriggerPickerDismiss = viewModel::onTriggerPickerDismiss,
    onTriggerSelected = viewModel::onTriggerSelected,
    onConditionPickerDismiss = viewModel::onConditionPickerDismiss,
    onConditionSelected = viewModel::onConditionSelected,
    onActionPickerDismiss = viewModel::onActionPickerDismiss,
    onActionSelected = viewModel::onActionSelected,
  )
}

/**
 * Navigation for a workflow list's (Gallery/RulesForGroup/RulesForReminder) detail pane: if the
 * current top entry is itself the rule builder, replace it instead of stacking another one on top
 * - mirrors `GroupsNavGraph.kt`'s identically-purposed private helper. Only ever matters in
 * two-pane mode, where the "+" create-rule action stays reachable while a builder is already open
 * in the detail pane.
 */
private fun MutableList<NavKey>.navigateToDetailPane(key: NavKey) {
  if (lastOrNull() is WorkflowNavKey.Builder) {
    removeLastOrNull()
  }
  add(key)
}
