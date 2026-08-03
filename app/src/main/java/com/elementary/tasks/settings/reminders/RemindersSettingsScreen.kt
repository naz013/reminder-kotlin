package com.elementary.tasks.settings.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.DisabledAlpha
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem
import com.github.naz013.ui.common.compose.foundation.dialog.SingleChoiceDialog

@Composable
fun RemindersSettingsScreen(
  modifier: Modifier = Modifier,
  state: RemindersSettingsState,
  onInsightsClick: () -> Unit,
  onPresetsClick: () -> Unit,
  onLocationClick: () -> Unit,
  onWorkflowRulesClick: () -> Unit,
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
  onDefaultVibrateToggle: () -> Unit,
  onDefaultBypassDoNotDisturbToggle: () -> Unit,
  onDefaultWakeScreenToggle: () -> Unit,
  onDefaultCategoryClick: () -> Unit,
  onDefaultLockScreenVisibilityClick: () -> Unit,
  onDefaultVibrationPatternClick: () -> Unit,
  onNotificationHelpClick: () -> Unit,
  onChoiceOptionSelected: (Int) -> Unit,
  onSeekValueChange: (Int) -> Unit,
  onSeekConfirm: () -> Unit,
  onDialogDismiss: () -> Unit,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()),
  ) {
    if (state.isInsightsVisible) {
      SettingsItem(
        title = stringResource(R.string.insights),
        icon = AppIcons.Fluent.DataPie,
        dividerBottom = true,
        onClick = onInsightsClick,
      )
    }
    SettingsItem(
      title = stringResource(R.string.recur_presets),
      icon = painterResource(R.drawable.ic_builder_preset),
      dividerBottom = true,
      onClick = onPresetsClick,
    )
    if (state.workflowsVisible) {
      SettingsItem(
        title = stringResource(R.string.workflow_rules),
        icon = painterResource(R.drawable.ic_fluent_arrow_repeat_all),
        dividerBottom = true,
        onClick = onWorkflowRulesClick,
      )
    }
    if (state.hasLocation) {
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
      icon = AppIcons.Fluent.Star,
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
      icon = AppIcons.Builder.Interval,
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
      icon = AppIcons.Fluent.Sleep,
      dividerBottom = true,
    )

    val dndValueColor = MaterialTheme.colorScheme.onSurface.copy(
      alpha = if (state.isDoNotDisturbDependentEnabled) {
        1f
      } else DisabledAlpha
    )
    SettingsItem(
      title = stringResource(R.string.from),
      icon = painterResource(R.drawable.ic_builder_timer),
      enabled = state.isDoNotDisturbDependentEnabled,
      dividerBottom = true,
      onClick = onDndFromClick,
      trailing = {
        Text(
          text = state.doNotDisturbFromText,
          style = MaterialTheme.typography.titleLarge,
          color = dndValueColor
        )
      },
    )
    SettingsItem(
      title = stringResource(R.string.to),
      icon = painterResource(R.drawable.ic_builder_timer_exclusion),
      enabled = state.isDoNotDisturbDependentEnabled,
      dividerBottom = true,
      onClick = onDndToClick,
      trailing = {
        Text(
          text = state.doNotDisturbToText,
          style = MaterialTheme.typography.titleLarge,
          color = dndValueColor
        )
      },
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

    SettingsSectionHeader(stringResource(R.string.notification_defaults))

    SettingsItem(
      title = stringResource(R.string.how_does_this_work),
      icon = AppIcons.Fluent.QuestionCircle,
      dividerBottom = true,
      onClick = onNotificationHelpClick,
    )
    SettingsItem(
      title = stringResource(R.string.notification_category),
      subtitle = state.defaultCategoryName,
      dividerBottom = true,
      onClick = onDefaultCategoryClick,
      icon = AppIcons.Fluent.ChannelNotifications,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.default_vibrate),
      checked = state.isDefaultVibrateChecked,
      onCheckedChange = { onDefaultVibrateToggle() },
      subtitleOn = stringResource(R.string.vibrate_on_notification),
      subtitleOff = stringResource(R.string.do_not_vibrate_on_notification),
      dividerBottom = true,
      icon = AppIcons.Fluent.PhoneVibrate,
    )
    SettingsItem(
      title = stringResource(R.string.vibration_pattern),
      subtitle = state.defaultVibrationPatternName,
      dividerBottom = true,
      onClick = onDefaultVibrationPatternClick,
      icon = AppIcons.Fluent.PhoneVibrate,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.bypass_do_not_disturb),
      checked = state.isDefaultBypassDoNotDisturbChecked,
      onCheckedChange = { onDefaultBypassDoNotDisturbToggle() },
      subtitleOn = stringResource(R.string.bypass_do_not_disturb_enabled),
      subtitleOff = stringResource(R.string.bypass_do_not_disturb_disabled),
      dividerBottom = true,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.wake_screen),
      checked = state.isDefaultWakeScreenChecked,
      onCheckedChange = { onDefaultWakeScreenToggle() },
      subtitleOn = stringResource(R.string.wake_screen_enabled),
      subtitleOff = stringResource(R.string.wake_screen_disabled),
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.lock_screen_visibility),
      subtitle = state.defaultLockScreenVisibilityName,
      dividerBottom = true,
      onClick = onDefaultLockScreenVisibilityClick,
      icon = AppIcons.Fluent.LockShield,
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
