package com.github.naz013.feature.settings.digest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem
import com.github.naz013.ui.common.datetime.rememberDateTimePicker
import org.threeten.bp.LocalTime

@Composable
internal fun DigestSettingsScreen(
  state: DigestSettingsState,
  onDailyToggle: () -> Unit,
  onHourSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val dateTimePicker = rememberDateTimePicker()
  val timePickerTitle = stringResource(R.string.ai_digest_time)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    SettingsSwitchItem(
      title = stringResource(R.string.ai_digest_daily_toggle),
      checked = state.isDailyEnabled,
      onCheckedChange = { onDailyToggle() },
      icon = AppIcons.Fluent.CalendarAgenda,
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.ai_digest_time),
      subtitle = "%02d:00".format(state.hour),
      icon = AppIcons.Fluent.ClockAlarm,
      enabled = state.isDailyEnabled,
      dividerBottom = true,
      onClick = {
        dateTimePicker.showTimePicker(
          time = LocalTime.of(state.hour, 0),
          title = timePickerTitle,
          onTimeSelected = { time -> onHourSelected(time.hour) },
        )
      },
    )
  }
}
