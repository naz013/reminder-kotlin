package com.github.naz013.feature.settings.general

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSearchItemKeys
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem

@Composable
internal fun GeneralSettingsScreen(
  modifier: Modifier = Modifier,
  state: GeneralSettingsState,
  onLanguageClick: () -> Unit,
  onThemeClick: () -> Unit,
  onTimeFormatClick: () -> Unit,
  onHeaderItemsClick: () -> Unit,
  onMetricToggle: (Boolean) -> Unit,
  onAnalyticsToggle: (Boolean) -> Unit,
  onDialogOptionSelected: (Int) -> Unit,
  onDialogDismiss: () -> Unit,
  onHapticToggle: (Boolean) -> Unit,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()),
  ) {
    SettingsItem(
      title = stringResource(R.string.application_language),
      subtitle = state.languageName,
      icon = painterResource(R.drawable.ic_fluent_local_language),
      itemKey = SettingsSearchItemKeys.GENERAL_LANGUAGE,
      dividerBottom = true,
      onClick = onLanguageClick,
    )
    SettingsItem(
      title = stringResource(R.string.dark_mode),
      subtitle = state.themeName,
      icon = painterResource(R.drawable.ic_fluent_dark_theme),
      itemKey = SettingsSearchItemKeys.GENERAL_DARK_MODE,
      dividerBottom = true,
      onClick = onThemeClick,
    )
    SettingsItem(
      title = stringResource(R.string._24_hour_format),
      subtitle = state.timeFormatName,
      icon = painterResource(R.drawable.ic_builder_time),
      itemKey = SettingsSearchItemKeys.GENERAL_TIME_FORMAT,
      dividerBottom = true,
      onClick = onTimeFormatClick,
    )
    SettingsItem(
      title = stringResource(R.string.header_items),
      subtitle = stringResource(R.string.header_items_subtitle),
      icon = painterResource(R.drawable.ic_fluent_grid),
      dividerBottom = true,
      onClick = onHeaderItemsClick,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.metric_units),
      checked = state.isMetricChecked,
      onCheckedChange = onMetricToggle,
      subtitleOn = stringResource(R.string.use_metric_system),
      subtitleOff = stringResource(R.string.use_imperial_system),
      icon = painterResource(R.drawable.ic_fluent_math_formula),
      itemKey = SettingsSearchItemKeys.GENERAL_METRIC_UNITS,
      dividerBottom = true,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.analytics),
      checked = state.isAnalyticsChecked,
      onCheckedChange = onAnalyticsToggle,
      subtitleOn = stringResource(R.string.collecting_of_app_analytics_is_enabled),
      subtitleOff = stringResource(R.string.collecting_of_app_analytics_is_disabled),
      icon = painterResource(R.drawable.ic_fluent_data_area),
      itemKey = SettingsSearchItemKeys.GENERAL_ANALYTICS,
      dividerBottom = true,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.haptic_feedback),
      checked = state.hapticFeedbackEnabled,
      onCheckedChange = onHapticToggle,
      subtitleOn = stringResource(R.string.use_haptic_feedback_for_interactions),
      subtitleOff = stringResource(R.string.do_not_use_haptic_feedback_for_interactions),
      icon = painterResource(R.drawable.ic_fluent_phone_vibrate),
      itemKey = SettingsSearchItemKeys.GENERAL_HAPTIC_FEEDBACK,
      dividerBottom = true,
    )
  }

  val dialog = state.dialog
  if (dialog != null) {
    SingleChoiceDialog(
      dialog = dialog,
      onOptionSelected = onDialogOptionSelected,
      onDismiss = onDialogDismiss,
    )
  }
}

@Composable
private fun SingleChoiceDialog(
  dialog: GeneralSettingsDialog,
  onOptionSelected: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(dialog.title) },
    text = {
      Column(
        modifier =
          Modifier
            .heightIn(max = 400.dp)
            .verticalScroll(rememberScrollState())
            .selectableGroup(),
      ) {
        dialog.options.forEachIndexed { index, option ->
          val selected = index == dialog.selectedIndex
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
              Modifier
                .fillMaxWidth()
                .selectable(
                  selected = selected,
                  onClick = { onOptionSelected(index) },
                  role = Role.RadioButton,
                )
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
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
    },
  )
}
