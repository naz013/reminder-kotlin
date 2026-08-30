package com.github.naz013.group.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.common.compose.foundation.component.ColorPickerCard
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader
import com.github.naz013.ui.common.compose.foundation.dialog.SingleChoiceDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditGroupScreen(
  modifier: Modifier = Modifier,
  state: EditGroupState,
  // True when shown as a two-pane detail pane rather than pushed full-screen - only changes the
  // leading icon (close vs. back), onBackClick pops the entry either way.
  renderAsDetailPane: Boolean = false,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onDeleteMenuClick: () -> Unit,
  onNameChange: (String) -> Unit,
  onColorSelected: (Int) -> Unit,
  onDefaultCheckChanged: (Boolean) -> Unit,
  onWorkflowRulesClick: () -> Unit,
  onVibrateClick: () -> Unit,
  onRepeatNotificationClick: () -> Unit,
  onBypassDndClick: () -> Unit,
  onWakeScreenClick: () -> Unit,
  onPriorityClick: () -> Unit,
  onCategoryClick: () -> Unit,
  onLockScreenVisibilityClick: () -> Unit,
  onVibrationPatternClick: () -> Unit,
  onNotificationHelpClick: () -> Unit,
  onNotificationChoiceSelected: (Int) -> Unit,
  onDelayMinutesClick: () -> Unit,
  onDelayMinutesOverrideToggle: (Boolean) -> Unit,
  onDelayMinutesPreviewChange: (Int) -> Unit,
  onDelayMinutesConfirm: () -> Unit,
  onDeleteConfirmed: () -> Unit,
  onCopyKeepClick: () -> Unit,
  onCopyReplaceClick: () -> Unit,
  onDialogDismiss: () -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(if (state.hasId) R.string.change_group else R.string.create_group)) },
        navigationIcon = {
          MenuIconButton(
            icon = if (renderAsDetailPane) AppIcons.Fluent.Dismiss else AppIcons.Builder.ArrowLeft,
            contentDescription = if (renderAsDetailPane) stringResource(R.string.acc_close) else null,
            enabled = !state.isLoading,
            onClick = onBackClick,
          )
        },
        actions = {
          if (state.canDelete) {
            MenuIconButton(
              icon = painterResource(R.drawable.ic_fluent_delete),
              contentDescription = stringResource(R.string.delete),
              enabled = !state.isLoading,
              onClick = onDeleteMenuClick,
            )
          }
          MenuTextButton(
            text = stringResource(R.string.save),
            enabled = !state.isLoading,
            onClick = onSaveClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(padding)
          .verticalScroll(rememberScrollState())
          .padding(16.dp),
    ) {
      OutlinedTextField(
        value = state.title,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.title)) },
        isError = state.titleError,
        supportingText = {
          if (state.titleError) Text(stringResource(R.string.must_be_not_empty))
        },
        singleLine = true,
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      ) {
        Text(
          text = stringResource(R.string.make_default),
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.weight(1f),
        )
        Switch(
          checked = state.isDefault,
          onCheckedChange = onDefaultCheckChanged,
          enabled = !state.isLoading && state.defaultCheckEnabled,
        )
      }

      ColorPickerCard(
        colors = state.sliderColors,
        selectedIndex = state.colorPosition,
        onColorSelected = onColorSelected,
        enabled = !state.isLoading,
        hapticFeedbackEnabled = state.hapticFeedbackEnabled,
        modifier = Modifier.padding(top = 16.dp),
      )

      adsContent()

      SettingsSectionHeader(
        stringResource(R.string.notification_overrides),
        modifier = Modifier.padding(top = 16.dp),
      )
      SettingsItem(
        title = stringResource(R.string.how_does_this_work),
        icon = AppIcons.Fluent.QuestionCircle,
        dividerBottom = true,
        onClick = onNotificationHelpClick,
      )
      SettingsItem(
        title = stringResource(R.string.reminder_default_priority),
        subtitle = state.prioritySubtitle,
        dividerBottom = true,
        onClick = onPriorityClick,
        icon = AppIcons.Fluent.Star,
      )
      SettingsItem(
        title = stringResource(R.string.repeat_notification),
        subtitle = state.repeatNotificationSubtitle,
        dividerBottom = true,
        onClick = onRepeatNotificationClick,
      )
      SettingsItem(
        title = stringResource(R.string.notification_delay),
        subtitle = state.delayMinutesSubtitle,
        dividerBottom = true,
        onClick = onDelayMinutesClick,
        icon = AppIcons.Builder.Interval,
      )
      SettingsItem(
        title = stringResource(R.string.notification_category),
        subtitle = state.categorySubtitle,
        dividerBottom = true,
        onClick = onCategoryClick,
        icon = AppIcons.Fluent.ChannelNotifications,
      )
      SettingsItem(
        title = stringResource(R.string.default_vibrate),
        subtitle = state.vibrateSubtitle,
        dividerBottom = true,
        onClick = onVibrateClick,
        icon = AppIcons.Fluent.PhoneVibrate,
      )
      SettingsItem(
        title = stringResource(R.string.vibration_pattern),
        subtitle = state.vibrationPatternSubtitle,
        dividerBottom = true,
        onClick = onVibrationPatternClick,
        icon = AppIcons.Fluent.PhoneVibrate,
      )
      SettingsItem(
        title = stringResource(R.string.bypass_do_not_disturb),
        subtitle = state.bypassDndSubtitle,
        dividerBottom = true,
        onClick = onBypassDndClick,
        icon = AppIcons.Fluent.Sleep
      )
      SettingsItem(
        title = stringResource(R.string.wake_screen),
        subtitle = state.wakeScreenSubtitle,
        dividerBottom = true,
        onClick = onWakeScreenClick,
      )
      SettingsItem(
        title = stringResource(R.string.lock_screen_visibility),
        subtitle = state.lockScreenVisibilitySubtitle,
        dividerBottom = true,
        onClick = onLockScreenVisibilityClick,
        icon = AppIcons.Fluent.LockShield,
      )
      if (state.workflowsVisible) {
        SettingsItem(
          title = stringResource(R.string.workflow_rules),
          icon = painterResource(R.drawable.ic_fluent_arrow_repeat_all),
          modifier = Modifier.padding(top = 16.dp),
          onClick = onWorkflowRulesClick,
        )
      }
    }
  }

  when (val dialog = state.dialog) {
    EditGroupDialog.CopyConflict -> {
      AlertDialog(
        onDismissRequest = onDialogDismiss,
        text = { Text(stringResource(R.string.same_group_message)) },
        confirmButton = {
          TextButton(onClick = onCopyKeepClick) { Text(stringResource(R.string.keep)) }
        },
        dismissButton = {
          TextButton(onClick = onCopyReplaceClick) { Text(stringResource(R.string.replace)) }
        },
      )
    }

    EditGroupDialog.DeleteConfirm -> {
      AlertDialog(
        onDismissRequest = onDialogDismiss,
        text = { Text(stringResource(R.string.are_you_sure)) },
        confirmButton = {
          TextButton(onClick = onDeleteConfirmed) { Text(stringResource(R.string.yes)) }
        },
        dismissButton = {
          TextButton(onClick = onDialogDismiss) { Text(stringResource(R.string.no)) }
        },
      )
    }

    is EditGroupDialog.NotificationChoice -> {
      SingleChoiceDialog(
        title = dialog.title,
        options = dialog.options,
        selectedIndex = dialog.selectedIndex,
        onOptionSelected = onNotificationChoiceSelected,
        onDismiss = onDialogDismiss,
      )
    }

    is EditGroupDialog.DelayMinutes -> {
      AlertDialog(
        onDismissRequest = onDialogDismiss,
        title = { Text(stringResource(R.string.notification_delay)) },
        text = {
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(text = stringResource(R.string.inherit_from_settings), modifier = Modifier.weight(1f))
              Switch(
                checked = !dialog.isOverridden,
                onCheckedChange = { inherit -> onDelayMinutesOverrideToggle(!inherit) },
              )
            }
            if (dialog.isOverridden) {
              Text(
                text = stringResource(R.string.x_minutes, dialog.previewValue.toString()),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp),
              )
              Slider(
                value = dialog.previewValue.toFloat(),
                onValueChange = { onDelayMinutesPreviewChange(it.toInt()) },
                valueRange = 0f..60f,
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
        },
        confirmButton = { TextButton(onClick = onDelayMinutesConfirm) { Text(stringResource(R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDialogDismiss) { Text(stringResource(R.string.cancel)) } },
      )
    }

    null -> Unit
  }
}

@Preview(showBackground = true)
@Composable
private fun EditGroupScreenPreview() {
  AppTheme {
    EditGroupScreen(
      state =
        EditGroupState(
          title = "Work",
          colorPosition = 5,
          sliderColors = listOf(Color.Red, Color.Magenta, Color.Blue, Color.Cyan, Color.Green, Color.Yellow),
          canDelete = true,
        ),
      onBackClick = {},
      onSaveClick = {},
      onDeleteMenuClick = {},
      onNameChange = {},
      onColorSelected = {},
      onDefaultCheckChanged = {},
      onWorkflowRulesClick = {},
      onVibrateClick = {},
      onRepeatNotificationClick = {},
      onBypassDndClick = {},
      onWakeScreenClick = {},
      onPriorityClick = {},
      onCategoryClick = {},
      onLockScreenVisibilityClick = {},
      onVibrationPatternClick = {},
      onNotificationHelpClick = {},
      onNotificationChoiceSelected = {},
      onDelayMinutesClick = {},
      onDelayMinutesOverrideToggle = {},
      onDelayMinutesPreviewChange = {},
      onDelayMinutesConfirm = {},
      onDeleteConfirmed = {},
      onCopyKeepClick = {},
      onCopyReplaceClick = {},
      onDialogDismiss = {},
      adsContent = {},
    )
  }
}
