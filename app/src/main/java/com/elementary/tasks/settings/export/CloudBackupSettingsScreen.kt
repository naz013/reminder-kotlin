package com.elementary.tasks.settings.export

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.dialog.SingleChoiceDialog

@Composable
fun CloudBackupSettingsScreen(
  state: CloudBackupSettingsState,
  onCloudServicesClick: () -> Unit,
  onAutoBackupIntervalClick: () -> Unit,
  onAutoBackupIntervalSelected: (Int) -> Unit,
  onNetworkTypeClick: () -> Unit,
  onNetworkTypeSelected: (Int) -> Unit,
  onEraseClick: () -> Unit,
  onEraseConfirmed: () -> Unit,
  onBackupNowClick: () -> Unit,
  onSyncNowClick: () -> Unit,
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
    SettingsItem(
      title = stringResource(R.string.cloud_services),
      icon = painterResource(R.drawable.ic_fluent_cloud),
      dividerBottom = true,
      onClick = onCloudServicesClick,
    )
    SettingsItem(
      title = stringResource(R.string.automatically_backup),
      subtitle = state.autoBackupStateName,
      icon = painterResource(R.drawable.ic_fluent_cloud_backup),
      enabled = state.hasAnyCloudApi,
      dividerBottom = true,
      onClick = onAutoBackupIntervalClick,
    )
    SettingsItem(
      title = stringResource(R.string.which_network_to_use_for_sync),
      subtitle = state.networkTypeName,
      icon = painterResource(R.drawable.ic_fluent_network_check),
      enabled = state.hasAnyCloudApi,
      dividerBottom = true,
      onClick = onNetworkTypeClick,
    )
    SettingsItem(
      title = stringResource(R.string.erase_cloud_data),
      icon = painterResource(R.drawable.ic_fluent_broom),
      enabled = state.hasAnyCloudApi,
      dividerBottom = true,
      onClick = onEraseClick,
    )

    if (state.isInProgress) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(16.dp),
      ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(
          text = stringResource(R.string.please_wait),
          style = MaterialTheme.typography.titleLarge,
          modifier =
            Modifier
              .align(Alignment.CenterHorizontally)
              .padding(top = 16.dp),
        )
      }
    }

    OutlinedButton(
      onClick = onBackupNowClick,
      enabled = state.hasAnyCloudApi && !state.isInProgress,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 16.dp),
    ) {
      Text(stringResource(R.string.backup_data_now))
    }
    OutlinedButton(
      onClick = onSyncNowClick,
      enabled = state.hasAnyCloudApi && !state.isInProgress,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(16.dp),
    ) {
      Text(stringResource(R.string.sync_data_now))
    }
  }

  when (val dialog = state.dialog) {
    is CloudBackupDialog.AutoBackupInterval -> {
      SingleChoiceDialog(
        title = stringResource(R.string.automatically_backup),
        options = dialog.options,
        selectedIndex = dialog.selectedIndex,
        onOptionSelected = onAutoBackupIntervalSelected,
        onDismiss = onDialogDismiss,
      )
    }

    is CloudBackupDialog.NetworkType -> {
      SingleChoiceDialog(
        title = stringResource(R.string.select_network_type),
        options = dialog.options,
        selectedIndex = dialog.selectedIndex,
        onOptionSelected = onNetworkTypeSelected,
        onDismiss = onDialogDismiss,
      )
    }

    CloudBackupDialog.EraseConfirm -> {
      AlertDialog(
        onDismissRequest = onDialogDismiss,
        title = { Text(stringResource(R.string.erase_cloud_data)) },
        text = { Text(stringResource(R.string.erase_cloud_data_message)) },
        confirmButton = { TextButton(onClick = onEraseConfirmed) { Text(stringResource(R.string.erase)) } },
        dismissButton = { TextButton(onClick = onDialogDismiss) { Text(stringResource(R.string.cancel)) } },
      )
    }

    null -> Unit
  }
}
