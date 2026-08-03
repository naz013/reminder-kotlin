package com.elementary.tasks.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem

@Composable
fun SettingsHubScreen(
  modifier: Modifier = Modifier,
  state: SettingsHubState,
  onBuyProClick: () -> Unit,
  onUpdateClick: () -> Unit,
  onGeneralClick: () -> Unit,
  onBackupClick: () -> Unit,
  onCalendarClick: () -> Unit,
  onRemindersClick: () -> Unit,
  onBirthdaysClick: () -> Unit,
  onSecurityClick: () -> Unit,
  onNotesClick: () -> Unit,
  onOtherClick: () -> Unit,
  onDeveloperClick: () -> Unit,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()),
  ) {
    if (state.isBuyProBadgeVisible) {
      Text(
        text = stringResource(R.string.pro_version),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.tertiary,
        modifier =
          Modifier
            .clickable(onClick = onBuyProClick)
            .padding(8.dp),
      )
    }
    if (state.saleMessage != null) {
      Text(
        text = state.saleMessage,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(8.dp),
      )
    }
    if (state.updateMessage != null) {
      Text(
        text = state.updateMessage,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier =
          Modifier
            .fillMaxWidth()
            .clickable(onClick = onUpdateClick)
            .padding(8.dp),
      )
    }
    if (state.internalMessage != null) {
      Text(
        text = state.internalMessage,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(8.dp),
      )
    }
    if (state.isPlayServicesWarningVisible) {
      Text(
        text = stringResource(R.string.google_play_services_not_found_some_functionality_is_disabled),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.error,
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(8.dp),
      )
    }
    if (state.isDoNotDisturbActive) {
      Icon(
        painter = painterResource(R.drawable.ic_moon),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.size(56.dp),
      )
    }

    SettingsItem(
      title = stringResource(R.string.general),
      icon = painterResource(R.drawable.ic_fluent_system),
      dividerBottom = true,
      onClick = onGeneralClick,
    )
    SettingsItem(
      title = stringResource(R.string.backup),
      icon = AppIcons.Fluent.CloudSyncComplete,
      dividerBottom = true,
      onClick = onBackupClick,
    )
    SettingsItem(
      title = stringResource(R.string.calendar),
      icon = painterResource(R.drawable.ic_builder_by_monthday),
      dividerBottom = true,
      onClick = onCalendarClick,
    )
    SettingsItem(
      title = stringResource(R.string.reminders_),
      icon = painterResource(R.drawable.ic_fluent_clock_alarm),
      dividerBottom = true,
      onClick = onRemindersClick,
    )
    SettingsItem(
      title = stringResource(R.string.birthdays),
      icon = painterResource(R.drawable.ic_fluent_food_cake),
      dividerBottom = true,
      onClick = onBirthdaysClick,
    )
    SettingsItem(
      title = stringResource(R.string.security),
      icon = painterResource(R.drawable.ic_fluent_lock),
      dividerBottom = true,
      onClick = onSecurityClick,
    )
    SettingsItem(
      title = stringResource(R.string.notes),
      icon = painterResource(R.drawable.ic_fluent_note),
      dividerBottom = true,
      onClick = onNotesClick,
    )
    SettingsItem(
      title = stringResource(R.string.other),
      icon = painterResource(R.drawable.ic_fluent_launcher_settings),
      dividerBottom = true,
      onClick = onOtherClick,
    )
    if (BuildConfig.DEBUG) {
      SettingsItem(
        title = "Developer",
        dividerBottom = true,
        onClick = onDeveloperClick,
      )
    }
  }
}
