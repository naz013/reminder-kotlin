package com.elementary.tasks.settings.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem
import com.github.naz013.ui.common.theme.ThemeProvider

@Composable
fun CalendarSettingsScreen(
  state: CalendarSettingsState,
  onFirstDayClick: () -> Unit,
  onFirstDayOptionSelected: (Int) -> Unit,
  onTodayColorClick: () -> Unit,
  onReminderColorClick: () -> Unit,
  onBirthdayColorClick: () -> Unit,
  onSelectCalendarClick: () -> Unit,
  onCalendarResetClick: () -> Unit,
  onExportToggle: () -> Unit,
  onScanToggle: () -> Unit,
  onDialogDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState()),
  ) {
    SettingsItem(
      title = stringResource(R.string.first_day_of_the_week),
      subtitle = state.firstDayName,
      icon = painterResource(R.drawable.ic_fluent_calendar_week_start),
      dividerBottom = true,
      onClick = onFirstDayClick,
    )

    SettingsSectionHeader(stringResource(R.string.appearance))

    SettingsItem(
      title = stringResource(R.string.today_color),
      icon = painterResource(R.drawable.ic_fluent_color),
      dividerBottom = true,
      onClick = onTodayColorClick,
      trailing = { ColorSwatch(Color(ThemeProvider.colorTodayCalendar(context, state.todayColorIndex))) },
    )
    SettingsItem(
      title = stringResource(R.string.reminders_color),
      icon = painterResource(R.drawable.ic_fluent_color_fill),
      dividerBottom = true,
      onClick = onReminderColorClick,
      trailing = { ColorSwatch(Color(ThemeProvider.colorReminderCalendar(context, state.reminderColorIndex))) },
    )
    SettingsItem(
      title = stringResource(R.string.birthdays_color),
      icon = painterResource(R.drawable.ic_fluent_food_cake),
      dividerBottom = true,
      onClick = onBirthdayColorClick,
      trailing = { ColorSwatch(Color(ThemeProvider.colorBirthdayCalendar(context, state.birthdayColorIndex))) },
    )

    SettingsSectionHeader(stringResource(R.string.google_calendar))

    SettingsItem(
      title = stringResource(R.string.choose_calendar),
      subtitle = state.selectedCalendarName,
      icon = painterResource(R.drawable.ic_fluent_calendar_star),
      dividerBottom = true,
      onClick = onSelectCalendarClick,
      trailing = if (state.isCalendarSelected) {
        { TextButton(onClick = onCalendarResetClick) { Text(stringResource(R.string.reset_calendar)) } }
      } else {
        null
      },
    )
    SettingsSwitchItem(
      title = stringResource(R.string.add_reminders_to_google_calendar),
      checked = state.isExportChecked,
      onCheckedChange = { onExportToggle() },
      icon = painterResource(R.drawable.ic_builder_google_calendar_add),
      enabled = state.isCalendarSelected,
      dividerBottom = true,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.scan_google_calendar_for_the_new_events),
      checked = state.isScanChecked,
      onCheckedChange = { onScanToggle() },
      icon = painterResource(R.drawable.ic_fluent_calendar_sync),
      enabled = state.isCalendarSelected,
      dividerBottom = true,
    )
  }

  val dialog = state.dialog
  if (dialog is CalendarSettingsDialog.FirstDay) {
    AlertDialog(
      onDismissRequest = onDialogDismiss,
      title = { Text(stringResource(R.string.first_day_of_the_week)) },
      text = {
        Column(modifier = Modifier.selectableGroup()) {
          dialog.options.forEachIndexed { index, option ->
            val selected = index == dialog.selectedIndex
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = { onFirstDayOptionSelected(index) }, role = Role.RadioButton)
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
      confirmButton = { TextButton(onClick = onDialogDismiss) { Text(stringResource(R.string.cancel)) } },
    )
  }
}

@Composable
private fun ColorSwatch(color: Color) {
  Box(
    modifier = Modifier
      .size(24.dp)
      .background(color, CircleShape),
  )
}
