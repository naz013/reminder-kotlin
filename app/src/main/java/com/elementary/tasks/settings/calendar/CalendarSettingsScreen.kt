package com.elementary.tasks.settings.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem
import com.github.naz013.ui.common.compose.foundation.dialog.SingleChoiceDialog
import com.github.naz013.ui.common.compose.foundation.dialog.rememberColorPickerDialogDispatcher

@Composable
fun CalendarSettingsScreen(
  state: CalendarSettingsState,
  onFirstDayClick: () -> Unit,
  onFirstDayOptionSelected: (Int) -> Unit,
  onTodayColorClick: () -> Unit,
  onReminderColorClick: () -> Unit,
  onBirthdayColorClick: () -> Unit,
  onColorOptionSelected: (Int) -> Unit,
  onSelectCalendarClick: () -> Unit,
  onGoogleCalendarOptionSelected: (Int) -> Unit,
  onCalendarResetClick: () -> Unit,
  onExportToggle: () -> Unit,
  onScanToggle: () -> Unit,
  onDialogDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colorPickerDialogDispatcher = rememberColorPickerDialogDispatcher()

  Column(
    modifier =
      modifier
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
      trailing = { ColorSwatch(state.todayColor) },
    )
    SettingsItem(
      title = stringResource(R.string.reminders_color),
      icon = painterResource(R.drawable.ic_fluent_color_fill),
      dividerBottom = true,
      onClick = onReminderColorClick,
      trailing = { ColorSwatch(state.reminderColor) },
    )
    SettingsItem(
      title = stringResource(R.string.birthdays_color),
      icon = painterResource(R.drawable.ic_fluent_food_cake),
      dividerBottom = true,
      onClick = onBirthdayColorClick,
      trailing = { ColorSwatch(state.birthdayColor) },
    )

    SettingsSectionHeader(stringResource(R.string.google_calendar))

    SettingsItem(
      title = stringResource(R.string.choose_calendar),
      subtitle = state.selectedCalendarName,
      icon = painterResource(R.drawable.ic_fluent_calendar_star),
      dividerBottom = true,
      onClick = onSelectCalendarClick,
      trailing =
        if (state.isCalendarSelected) {
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

  when (val dialog = state.dialog) {
    is CalendarSettingsDialog.FirstDay -> {
      SingleChoiceDialog(
        title = stringResource(R.string.first_day_of_the_week),
        options = dialog.options,
        selectedIndex = dialog.selectedIndex,
        onOptionSelected = onFirstDayOptionSelected,
        onDismiss = onDialogDismiss,
      )
    }

    is CalendarSettingsDialog.SelectGoogleCalendar -> {
      SingleChoiceDialog(
        title = stringResource(R.string.choose_calendar),
        options = dialog.calendars.map { it.name.orEmpty() },
        selectedIndex = dialog.selectedPosition,
        onOptionSelected = onGoogleCalendarOptionSelected,
        onDismiss = onDialogDismiss,
      )
    }

    is CalendarSettingsDialog.ColorPicker -> {
      colorPickerDialogDispatcher.showDialog(
        title = dialog.title,
        colors = dialog.colors,
        selectedIndex = dialog.selectedIndex,
        onColorSelected = onColorOptionSelected,
        hapticFeedbackEnabled = dialog.hapticFeedback,
        onDismissRequest = onDialogDismiss,
      )
    }

    null -> Unit
  }
}

@Composable
private fun ColorSwatch(color: Color) {
  Box(
    modifier =
      Modifier
        .size(24.dp)
        .background(color, CircleShape),
  )
}
