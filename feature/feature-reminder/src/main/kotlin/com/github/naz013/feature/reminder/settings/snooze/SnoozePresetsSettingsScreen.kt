package com.github.naz013.feature.reminder.settings.snooze

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.github.naz013.logic.reminder.snooze.SnoozePresetType
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSearchItemKeys
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem

@Composable
fun SnoozePresetsSettingsScreen(
  modifier: Modifier = Modifier,
  state: SnoozePresetsSettingsState,
  onPresetToggle: (SnoozePresetType) -> Unit,
  onEveningTimeClick: () -> Unit,
  onMorningTimeClick: () -> Unit,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState()),
  ) {
    SnoozePresetType.entries.forEach { preset ->
      SettingsSwitchItem(
        title = stringResource(preset.titleRes()),
        checked = preset in state.enabledPresets,
        onCheckedChange = { onPresetToggle(preset) },
        icon = AppIcons.Fluent.AlertSnooze,
        itemKey = if (preset == SnoozePresetType.entries.first()) {
          SettingsSearchItemKeys.REMINDERS_SNOOZE_PRESETS
        } else {
          null
        },
        dividerBottom = true,
      )
    }
    SettingsItem(
      title = stringResource(R.string.snooze_evening_time),
      icon = AppIcons.Builder.Timer,
      itemKey = SettingsSearchItemKeys.REMINDERS_SNOOZE_EVENING_TIME,
      dividerBottom = true,
      onClick = onEveningTimeClick,
      trailing = {
        Text(text = state.eveningTimeText, style = MaterialTheme.typography.titleLarge)
      },
    )
    SettingsItem(
      title = stringResource(R.string.snooze_morning_time),
      icon = AppIcons.Builder.Timer,
      itemKey = SettingsSearchItemKeys.REMINDERS_SNOOZE_MORNING_TIME,
      dividerBottom = true,
      onClick = onMorningTimeClick,
      trailing = {
        Text(text = state.morningTimeText, style = MaterialTheme.typography.titleLarge)
      },
    )
  }
}

private fun SnoozePresetType.titleRes(): Int =
  when (this) {
    SnoozePresetType.MINUTES_15 -> R.string.snooze_preset_15_minutes
    SnoozePresetType.HOUR_1 -> R.string.snooze_preset_1_hour
    SnoozePresetType.HOURS_3 -> R.string.snooze_preset_3_hours
    SnoozePresetType.THIS_EVENING -> R.string.snooze_preset_this_evening
    SnoozePresetType.TOMORROW_MORNING -> R.string.snooze_preset_tomorrow_morning
  }

@Preview(showBackground = true)
@Composable
private fun SnoozePresetsSettingsScreenPreview() {
  AppTheme {
    SnoozePresetsSettingsScreen(
      state = SnoozePresetsSettingsState(
        enabledPresets = setOf(SnoozePresetType.HOUR_1, SnoozePresetType.THIS_EVENING),
        eveningTimeText = "6:00 PM",
        morningTimeText = "8:00 AM",
      ),
      onPresetToggle = {},
      onEveningTimeClick = {},
      onMorningTimeClick = {},
    )
  }
}
