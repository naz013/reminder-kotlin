package com.elementary.tasks.settings.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem

@Composable
fun RemindersSettingsScreen(
  state: RemindersSettingsState,
  hasLocation: Boolean,
  onPresetsClick: () -> Unit,
  onLocationClick: () -> Unit,
  onPriorityClick: () -> Unit,
  onCompletedToggle: () -> Unit,
  onWearToggle: () -> Unit,
  onSnoozeClick: () -> Unit,
  onRepeatToggle: () -> Unit,
  onRepeatIntervalClick: () -> Unit,
  onLedToggle: () -> Unit,
  onLedColorClick: () -> Unit,
  onPermanentNotificationClick: () -> Unit,
  onStatusIconToggle: () -> Unit,
  onDoNotDisturbToggle: () -> Unit,
  onDndFromClick: () -> Unit,
  onDndToClick: () -> Unit,
  onDndActionClick: () -> Unit,
  onDndIgnoreClick: () -> Unit,
  onChoiceOptionSelected: (Int) -> Unit,
  onSeekValueChange: (Int) -> Unit,
  onSeekConfirm: () -> Unit,
  onDialogDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState()),
  ) {
    SettingsItem(
      title = stringResource(R.string.recur_presets),
      icon = painterResource(R.drawable.ic_builder_preset),
      dividerBottom = true,
      onClick = onPresetsClick,
    )
    if (hasLocation) {
      SettingsItem(
        title = stringResource(R.string.location),
        icon = painterResource(R.drawable.ic_builder_map_my_location),
        dividerBottom = true,
        onClick = onLocationClick,
      )
    }
    SettingsItem(
      title = stringResource(R.string.reminder_default_priority),
      subtitle = state.priorityName,
      icon = painterResource(R.drawable.ic_fluent_star),
      dividerBottom = true,
      onClick = onPriorityClick,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.completed_reminders),
      checked = state.isCompletedChecked,
      onCheckedChange = { onCompletedToggle() },
      subtitleOn = stringResource(R.string.move_to_the_archive),
      subtitleOff = stringResource(R.string.do_nothing),
      icon = painterResource(R.drawable.ic_builder_google_task_list),
      dividerBottom = true,
    )

    SettingsSectionHeader(stringResource(R.string.notification))

    SettingsSwitchItem(
      title = stringResource(R.string.android_wear_notification),
      checked = state.isWearChecked,
      onCheckedChange = { onWearToggle() },
      subtitleOn = stringResource(R.string.show_notifications_on_wear_devices),
      subtitleOff = stringResource(R.string.do_no_show_notifications_on_wear_devices),
      icon = painterResource(R.drawable.ic_fluent_watch),
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.default_reminder_snooze_time),
      subtitle = state.snoozeText,
      icon = painterResource(R.drawable.ic_fluent_alert_snooze),
      dividerBottom = true,
      onClick = onSnoozeClick,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.reminder_notification_repeating),
      checked = state.isRepeatChecked,
      onCheckedChange = { onRepeatToggle() },
      subtitleOn = stringResource(R.string.do_not_repeat_reminder_notification_when_no_action_taken),
      subtitleOff = stringResource(R.string.repeat_reminder_notification_until_dismissed),
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.reminder_notification_repeat_interval),
      subtitle = state.repeatIntervalText,
      enabled = state.isRepeatIntervalRowEnabled,
      dividerBottom = true,
      onClick = onRepeatIntervalClick,
    )
    if (state.isLedVisible) {
      SettingsSwitchItem(
        title = stringResource(R.string.led_indication_if_available),
        checked = state.isLedChecked,
        onCheckedChange = { onLedToggle() },
        subtitleOn = stringResource(R.string.show_led_indication),
        subtitleOff = stringResource(R.string.do_not_show_led_indication),
        icon = painterResource(R.drawable.ic_builder_led_color),
        dividerBottom = true,
      )
      SettingsItem(
        title = stringResource(R.string.led_indication_color),
        subtitle = state.ledColorName,
        icon = painterResource(R.drawable.ic_fluent_color),
        enabled = state.isLedColorRowEnabled,
        dividerBottom = true,
        onClick = onLedColorClick,
      )
    }

    SettingsSectionHeader(stringResource(R.string.status_bar))

    SettingsSwitchItem(
      title = stringResource(R.string.permanent_notification),
      checked = state.isPermanentNotificationChecked,
      onCheckedChange = { onPermanentNotificationClick() },
      subtitleOn = stringResource(R.string.always_showing),
      subtitleOff = stringResource(R.string.hidden),
      dividerBottom = true,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.permanent_notification_icon),
      checked = state.isStatusIconChecked,
      onCheckedChange = { onStatusIconToggle() },
      subtitleOn = stringResource(R.string.show_icon),
      subtitleOff = stringResource(R.string.hide_icon),
      enabled = state.isStatusIconRowEnabled,
      dividerBottom = true,
    )

    SettingsSectionHeader(stringResource(R.string.do_not_disturb))

    SettingsSwitchItem(
      title = stringResource(R.string.do_not_disturb),
      checked = state.isDoNotDisturbChecked,
      onCheckedChange = { onDoNotDisturbToggle() },
      icon = painterResource(R.drawable.ic_fluent_sleep),
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.from),
      icon = painterResource(R.drawable.ic_builder_timer),
      enabled = state.isDoNotDisturbDependentEnabled,
      dividerBottom = true,
      onClick = onDndFromClick,
      trailing = { Text(state.doNotDisturbFromText, style = MaterialTheme.typography.titleLarge) },
    )
    SettingsItem(
      title = stringResource(R.string.to),
      icon = painterResource(R.drawable.ic_builder_timer_exclusion),
      enabled = state.isDoNotDisturbDependentEnabled,
      dividerBottom = true,
      onClick = onDndToClick,
      trailing = { Text(state.doNotDisturbToText, style = MaterialTheme.typography.titleLarge) },
    )
    SettingsItem(
      title = stringResource(R.string.events_that_occured_during),
      subtitle = state.doNotDisturbActionName,
      enabled = state.isDoNotDisturbDependentEnabled,
      dividerBottom = true,
      onClick = onDndActionClick,
    )
    SettingsItem(
      title = stringResource(R.string.ignore_when_priority),
      subtitle = state.doNotDisturbIgnoreName,
      enabled = state.isDoNotDisturbDependentEnabled,
      dividerBottom = true,
      onClick = onDndIgnoreClick,
    )
  }

  when (val dialog = state.dialog) {
    is RemindersSettingsDialog.Choice -> {
      SingleChoiceDialog(
        title = dialog.title,
        options = dialog.options,
        selectedIndex = dialog.selectedIndex,
        onOptionSelected = onChoiceOptionSelected,
        onDismiss = onDialogDismiss,
      )
    }

    is RemindersSettingsDialog.Seek -> {
      AlertDialog(
        onDismissRequest = onDialogDismiss,
        title = { Text(dialog.title) },
        text = {
          Column {
            Text(text = dialog.formattedValue, style = MaterialTheme.typography.bodyLarge)
            Slider(
              value = dialog.previewValue.toFloat(),
              onValueChange = { onSeekValueChange(it.toInt()) },
              valueRange = 0f..60f,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        },
        confirmButton = { TextButton(onClick = onSeekConfirm) { Text(stringResource(R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDialogDismiss) { Text(stringResource(R.string.cancel)) } },
      )
    }

    null -> Unit
  }
}

@Composable
private fun SingleChoiceDialog(
  title: String,
  options: List<String>,
  selectedIndex: Int,
  onOptionSelected: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column(modifier = Modifier.selectableGroup()) {
        options.forEachIndexed { index, option ->
          val selected = index == selectedIndex
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .selectable(selected = selected, onClick = { onOptionSelected(index) }, role = Role.RadioButton)
              .padding(vertical = 8.dp),
          ) {
            RadioButton(selected = selected, onClick = null)
            Text(
              text = option,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.padding(start = 8.dp),
            )
          }
        }
      }
    },
    confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
  )
}
