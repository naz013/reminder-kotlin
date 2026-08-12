package com.github.naz013.localbackup.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.localbackup.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalBackupScreen(
  state: LocalBackupState,
  onBackClick: () -> Unit,
  onPassphraseChange: (String) -> Unit,
  onConfirmPassphraseChange: (String) -> Unit,
  onActionClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isExport = state.mode == LocalBackupMode.EXPORT
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(if (isExport) R.string.backup_export_title else R.string.backup_import_title)) },
        navigationIcon = {
          MenuIconButton(icon = AppIcons.Builder.ArrowLeft, contentDescription = null, onClick = onBackClick)
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    }
  ) { padding ->
    Column(modifier = Modifier.padding(padding).padding(16.dp)) {
      Text(
        text = stringResource(if (isExport) R.string.backup_export_description else R.string.backup_import_description),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 16.dp)
      )

      OutlinedTextField(
        value = state.passphrase,
        onValueChange = onPassphraseChange,
        label = { Text(stringResource(R.string.backup_passphrase)) },
        visualTransformation = PasswordVisualTransformation(),
        isError = state.passphraseError,
        singleLine = true,
        enabled = state.status !is LocalBackupStatus.InProgress,
        modifier = Modifier.fillMaxWidth()
      )

      if (isExport) {
        OutlinedTextField(
          value = state.confirmPassphrase,
          onValueChange = onConfirmPassphraseChange,
          label = { Text(stringResource(R.string.backup_confirm_passphrase)) },
          visualTransformation = PasswordVisualTransformation(),
          isError = state.passphraseError,
          singleLine = true,
          enabled = state.status !is LocalBackupStatus.InProgress,
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
      }

      if (state.passphraseError) {
        val errorRes = if (isExport) R.string.backup_passphrase_mismatch else R.string.backup_passphrase_empty
        Text(
          text = stringResource(errorRes),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(top = 4.dp)
        )
      }

      when (val status = state.status) {
        is LocalBackupStatus.Idle -> Unit
        is LocalBackupStatus.InProgress -> {
          CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
        is LocalBackupStatus.Success -> {
          Text(
            text = status.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
          )
        }
        is LocalBackupStatus.Error -> {
          Text(
            text = stringResource(status.messageRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 16.dp)
          )
        }
      }

      Button(
        onClick = onActionClick,
        enabled = state.status !is LocalBackupStatus.InProgress && state.status !is LocalBackupStatus.Success,
        modifier = Modifier.padding(top = 24.dp)
      ) {
        Text(stringResource(if (isExport) R.string.backup_export_action else R.string.backup_import_action))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun LocalBackupScreenPreview() {
  AppTheme {
    LocalBackupScreen(
      state = LocalBackupState(mode = LocalBackupMode.EXPORT),
      onBackClick = {},
      onPassphraseChange = {},
      onConfirmPassphraseChange = {},
      onActionClick = {}
    )
  }
}
