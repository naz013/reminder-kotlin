package com.github.naz013.feature.settings.backup

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
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader

/**
 * Groups the three backup-related destinations (cloud sync settings, local encrypted export,
 * local encrypted import) under one Settings entry instead of three separate top-level rows.
 */
@Composable
fun BackupSettingsScreen(
  isLocalBackupLocked: Boolean,
  onCloudBackupClick: () -> Unit,
  onExportBackupClick: () -> Unit,
  onImportBackupClick: () -> Unit,
  onLocalBackupLockedClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
  ) {
    SettingsItem(
      title = stringResource(R.string.cloud_backup),
      icon = AppIcons.Fluent.CloudBackup,
      dividerBottom = true,
      onClick = onCloudBackupClick,
    )
    SettingsSectionHeader(stringResource(R.string.local_backup))

    if (isLocalBackupLocked) {
      SettingsItem(
        title = stringResource(R.string.local_backup),
        subtitle = stringResource(R.string.local_backup_locked_description),
        icon = AppIcons.Fluent.FolderMove,
        locked = true,
        dividerBottom = true,
        onClick = onLocalBackupLockedClick,
      )
    } else {
      SettingsItem(
        title = stringResource(R.string.backup_export_title),
        icon = AppIcons.Fluent.FolderMove,
        dividerBottom = true,
        onClick = onExportBackupClick,
      )
      SettingsItem(
        title = stringResource(R.string.backup_import_title),
        icon = AppIcons.Fluent.DocumentTopRight,
        dividerBottom = true,
        onClick = onImportBackupClick,
      )
    }
  }
}
