package com.elementary.tasks.settings.birthday

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
fun BirthdaySettingsScreen(
  state: BirthdaySettingsState,
  onReminderToggle: () -> Unit,
  onDaysToBirthdayClick: () -> Unit,
  onDaysToBirthdayPreviewChange: (Int) -> Unit,
  onDaysToBirthdayConfirm: () -> Unit,
  onPriorityClick: () -> Unit,
  onPriorityOptionSelected: (Int) -> Unit,
  onReminderTimeClick: () -> Unit,
  onWidgetToggle: () -> Unit,
  onHomeDaysClick: () -> Unit,
  onHomeDaysPreviewChange: (Int) -> Unit,
  onHomeDaysConfirm: () -> Unit,
  onPermanentToggle: () -> Unit,
  onGlobalToggle: () -> Unit,
  onLedToggle: () -> Unit,
  onLedColorClick: () -> Unit,
  onLedColorOptionSelected: (Int) -> Unit,
  onUseContactsToggle: () -> Unit,
  onAutoScanToggle: () -> Unit,
  onDialogDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()),
  ) {
    SettingsSwitchItem(
      title = stringResource(R.string.birthday_reminder),
      checked = state.isReminderChecked,
      onCheckedChange = { onReminderToggle() },
      subtitleOn = stringResource(R.string.show_reminder_about_birthdays),
      subtitleOff = stringResource(R.string.do_not_remind_about_birthdays),
      icon = painterResource(R.drawable.ic_fluent_alert),
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.days_to_birthday),
      subtitle = stringResource(R.string.days_to_birthday_explanation),
      enabled = state.isDependentEnabled,
      dividerBottom = true,
      onClick = onDaysToBirthdayClick,
      trailing = { Text(state.daysToBirthday.toString(), style = MaterialTheme.typography.titleLarge) },
    )
    SettingsItem(
      title = stringResource(R.string.birthday_notification_priority),
      subtitle = state.priorityName,
      icon = painterResource(R.drawable.ic_fluent_star),
      enabled = state.isDependentEnabled,
      dividerBottom = true,
      onClick = onPriorityClick,
    )
    SettingsItem(
      title = stringResource(R.string.remind_at),
      subtitle = stringResource(R.string.birthday_remind_at_description),
      icon = painterResource(R.drawable.ic_builder_time),
      enabled = state.isDependentEnabled,
      dividerBottom = true,
      onClick = onReminderTimeClick,
      trailing = { Text(state.reminderTime, style = MaterialTheme.typography.titleLarge) },
    )
    SettingsSwitchItem(
      title = stringResource(R.string.birthdays_in_home_screen_widget),
      checked = state.isWidgetChecked,
      onCheckedChange = { onWidgetToggle() },
      subtitleOn = stringResource(R.string.show_in_home_screen),
      subtitleOff = stringResource(R.string.do_not_show_in_home_screen_widget),
      enabled = state.isDependentEnabled,
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.birthdays_on_home_for_next),
      subtitle = state.homeDaysText,
      enabled = state.isDependentEnabled,
      dividerBottom = true,
      onClick = onHomeDaysClick,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.permanent_status_bar_notification),
      checked = state.isPermanentChecked,
      onCheckedChange = { onPermanentToggle() },
      subtitleOn = stringResource(R.string.show_when_available_birthdays),
      subtitleOff = stringResource(R.string.do_not_show_permanent_notification),
      icon = painterResource(R.drawable.ic_fluent_phone_status_bar),
      enabled = state.isDependentEnabled,
      dividerBottom = true,
    )

    SettingsSectionHeader(stringResource(R.string.notification))

    SettingsSwitchItem(
      title = stringResource(R.string.global_settings),
      checked = state.isGlobalChecked,
      onCheckedChange = { onGlobalToggle() },
      subtitleOn = stringResource(R.string.use_settings_for_reminders),
      subtitleOff = stringResource(R.string.specify_own_configuration),
      enabled = state.isDependentEnabled,
      dividerBottom = true,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.led_indication_if_available),
      checked = state.isLedChecked,
      onCheckedChange = { onLedToggle() },
      subtitleOn = stringResource(R.string.show_led_indication),
      subtitleOff = stringResource(R.string.do_not_show_led_indication),
      icon = painterResource(R.drawable.ic_builder_led_color),
      enabled = state.isLedRowEnabled,
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

    SettingsSectionHeader(stringResource(R.string.import_))

    SettingsSwitchItem(
      title = stringResource(R.string.birthdays_from_contacts),
      checked = state.isUseContactsChecked,
      onCheckedChange = { onUseContactsToggle() },
      subtitleOn = stringResource(R.string.retrieve_birthdays_from_contacts),
      subtitleOff = stringResource(R.string.use_only_my_birthdays),
      icon = painterResource(R.drawable.ic_fluent_contacts),
      enabled = state.isDependentEnabled,
      dividerBottom = true,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.scan_contacts_automatically),
      checked = state.isAutoScanChecked,
      onCheckedChange = { onAutoScanToggle() },
      subtitleOn = stringResource(R.string.automatically_check_for_new_birthdays),
      subtitleOff = stringResource(R.string.do_not_scan_contacts),
      enabled = state.isAutoScanRowEnabled,
      dividerBottom = true,
    )
  }

  when (val dialog = state.dialog) {
    is BirthdayDialog.DaysToBirthday -> {
      SeekValueDialog(
        title = stringResource(R.string.days_to_birthday),
        value = dialog.previewValue,
        valueRange = 0f..5f,
        valueText = dialog.previewValue.toString(),
        onValueChange = onDaysToBirthdayPreviewChange,
        onConfirm = onDaysToBirthdayConfirm,
        onDismiss = onDialogDismiss,
      )
    }

    is BirthdayDialog.HomeDays -> {
      SeekValueDialog(
        title = stringResource(R.string.birthdays_on_home_for_next),
        value = dialog.previewValue,
        valueRange = 0f..5f,
        valueText = dialog.previewValue.toString(),
        onValueChange = onHomeDaysPreviewChange,
        onConfirm = onHomeDaysConfirm,
        onDismiss = onDialogDismiss,
      )
    }

    is BirthdayDialog.Priority -> {
      SingleChoiceDialog(
        title = dialog.title,
        options = dialog.options,
        selectedIndex = dialog.selectedIndex,
        onOptionSelected = onPriorityOptionSelected,
        onDismiss = onDialogDismiss,
      )
    }

    is BirthdayDialog.LedColor -> {
      SingleChoiceDialog(
        title = dialog.title,
        options = dialog.options,
        selectedIndex = dialog.selectedIndex,
        onOptionSelected = onLedColorOptionSelected,
        onDismiss = onDialogDismiss,
      )
    }

    null -> Unit
  }
}

@Composable
private fun SeekValueDialog(
  title: String,
  value: Int,
  valueRange: ClosedFloatingPointRange<Float>,
  valueText: String,
  onValueChange: (Int) -> Unit,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column {
        Text(text = valueText, style = MaterialTheme.typography.bodyLarge)
        Slider(
          value = value.toFloat(),
          onValueChange = { onValueChange(it.toInt()) },
          valueRange = valueRange,
          steps = (valueRange.endInclusive - valueRange.start).toInt() - 1,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.ok)) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
  )
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
            modifier =
              Modifier
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
