package com.github.naz013.feature.workflow.builder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.feature.workflow.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.BuilderItemStatus
import com.github.naz013.ui.common.compose.foundation.component.BuilderListItemCard
import com.github.naz013.ui.common.compose.foundation.component.SettingsCheckboxItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader
import org.threeten.bp.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkflowRuleBuilderScreen(
  modifier: Modifier = Modifier,
  state: WorkflowRuleBuilderState,
  // True when shown as a two-pane detail pane rather than pushed full-screen - only changes the
  // leading icon (close vs. back), onBackClick pops the entry either way.
  renderAsDetailPane: Boolean = false,
  onBackClick: () -> Unit,
  onTriggerRowClick: () -> Unit,
  onRemoveTriggerClick: () -> Unit,
  onAddConditionClick: () -> Unit,
  onEditConditionClick: (Int) -> Unit,
  onRemoveConditionClick: (Int) -> Unit,
  onActionRowClick: () -> Unit,
  onRemoveActionClick: () -> Unit,
  onRevertOnEndDateChange: (Boolean) -> Unit,
  onEndDateTimeSelected: (LocalDateTime) -> Unit,
  onSaveClick: () -> Unit,
  onTriggerPickerDismiss: () -> Unit,
  onTriggerSelected: (WorkflowTrigger) -> Unit,
  onConditionPickerDismiss: () -> Unit,
  onConditionSelected: (WorkflowCondition) -> Unit,
  onActionPickerDismiss: () -> Unit,
  onActionSelected: (WorkflowAction) -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(
            stringResource(
              if (state.editingRuleId != null) R.string.workflow_builder_edit_rule_title else R.string.workflow_builder_new_rule_title
            )
          )
        },
        navigationIcon = {
          MenuIconButton(
            icon = if (renderAsDetailPane) AppIcons.Fluent.Dismiss else AppIcons.Builder.ArrowLeft,
            contentDescription = if (renderAsDetailPane) {
              stringResource(com.github.naz013.ui.common.R.string.acc_close)
            } else {
              null
            },
            onClick = onBackClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
      item { SettingsSectionHeader(stringResource(R.string.workflow_builder_when)) }
      item {
        val trigger = state.trigger
        if (trigger == null) {
          BuilderListItemCard(
            icon = AppIcons.Fluent.Calendar,
            title = stringResource(R.string.workflow_builder_choose_trigger),
            value = stringResource(R.string.workflow_builder_not_set),
            status = BuilderItemStatus.EMPTY,
            onClick = onTriggerRowClick,
            onRemoveClick = onTriggerRowClick,
            removeIcon = AppIcons.Fluent.Add,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          )
        } else {
          BuilderListItemCard(
            icon = AppIcons.Fluent.Calendar,
            title = workflowTriggerLabel(trigger),
            value = workflowTriggerValue(trigger) ?: "",
            status = BuilderItemStatus.DONE,
            onClick = onTriggerRowClick,
            onRemoveClick = onRemoveTriggerClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          )
        }
      }

      item { SettingsSectionHeader(stringResource(R.string.workflow_builder_if_optional)) }
      items(state.conditions.size) { index ->
        val condition = state.conditions[index]
        BuilderListItemCard(
          icon = AppIcons.Fluent.Settings,
          title = workflowConditionLabel(condition),
          value = workflowConditionValue(condition, state.availableGroups, state.availableTags),
          status = BuilderItemStatus.DONE,
          onClick = { onEditConditionClick(index) },
          onRemoveClick = { onRemoveConditionClick(index) },
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
      }
      item {
        TextButton(onClick = onAddConditionClick, modifier = Modifier.padding(start = 12.dp)) {
          Icon(painter = AppIcons.Fluent.Add, contentDescription = null)
          Text(
            text = stringResource(R.string.workflow_builder_add_condition),
            modifier = Modifier.padding(start = 4.dp)
          )
        }
      }

      item { SettingsSectionHeader(stringResource(R.string.workflow_builder_then)) }
      item {
        val action = state.action
        if (action == null) {
          BuilderListItemCard(
            icon = AppIcons.Fluent.Checkmark,
            title = stringResource(R.string.workflow_builder_choose_action),
            value = stringResource(R.string.workflow_builder_not_set),
            status = BuilderItemStatus.EMPTY,
            onClick = onActionRowClick,
            onRemoveClick = onActionRowClick,
            removeIcon = AppIcons.Fluent.Add,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          )
        } else {
          BuilderListItemCard(
            icon = AppIcons.Fluent.Checkmark,
            title = workflowActionLabel(action),
            value = workflowActionValue(action, state.availableReminders, state.availableGroups, state.availableTags) ?: "",
            status = BuilderItemStatus.DONE,
            onClick = onActionRowClick,
            onRemoveClick = onRemoveActionClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          )
        }
      }

      if (state.showRevertOnEndDateOption) {
        item {
          SettingsCheckboxItem(
            title = stringResource(R.string.workflow_builder_revert_on_end_date),
            checked = state.revertOnEndDate,
            onCheckedChange = onRevertOnEndDateChange,
          )
        }
        if (state.revertOnEndDate) {
          item {
            DateTimePickerRow(
              label = stringResource(R.string.workflow_builder_ends_on),
              dateTime = state.endDateTime,
              onDateTimePicked = onEndDateTimeSelected,
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }
        }
      }

      item {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
          Button(onClick = onSaveClick, enabled = state.canSave) {
            Text(stringResource(R.string.save))
          }
        }
      }
    }
  }

  if (state.isTriggerPickerVisible) {
    WorkflowTriggerPickerSheet(
      scopeType = state.scopeType,
      onDismiss = onTriggerPickerDismiss,
      onConfirm = onTriggerSelected,
    )
  }

  if (state.isConditionPickerVisible) {
    WorkflowConditionPickerSheet(
      groups = state.availableGroups,
      tags = state.availableTags,
      initial = state.editingConditionIndex?.let { state.conditions.getOrNull(it) },
      onDismiss = onConditionPickerDismiss,
      onConfirm = onConditionSelected,
    )
  }

  if (state.isActionPickerVisible) {
    WorkflowActionPickerSheet(
      reminders = state.availableReminders,
      groups = state.availableGroups,
      tags = state.availableTags,
      onDismiss = onActionPickerDismiss,
      onConfirm = onActionSelected,
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun WorkflowRuleBuilderScreenPreview() {
  AppTheme {
    WorkflowRuleBuilderScreen(
      state = WorkflowRuleBuilderState(isLoading = false, scopeType = WorkflowScopeType.GLOBAL),
      onBackClick = {},
      onTriggerRowClick = {},
      onRemoveTriggerClick = {},
      onAddConditionClick = {},
      onEditConditionClick = {},
      onRemoveConditionClick = {},
      onActionRowClick = {},
      onRemoveActionClick = {},
      onRevertOnEndDateChange = {},
      onEndDateTimeSelected = {},
      onSaveClick = {},
      onTriggerPickerDismiss = {},
      onTriggerSelected = {},
      onConditionPickerDismiss = {},
      onConditionSelected = {},
      onActionPickerDismiss = {},
      onActionSelected = {},
    )
  }
}
