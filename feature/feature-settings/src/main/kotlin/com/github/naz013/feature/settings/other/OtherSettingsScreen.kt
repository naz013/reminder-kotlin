package com.github.naz013.feature.settings.other

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem

@Composable
fun OtherSettingsScreen(
  state: OtherSettingsState,
  onPrivacyPolicyClick: () -> Unit,
  onTermsClick: () -> Unit,
  onTroubleshootingClick: () -> Unit,
  onFeedbackClick: () -> Unit,
  onRateClick: () -> Unit,
  onTellFriendsClick: () -> Unit,
  onWhatsNewClick: () -> Unit,
  onPermissionsClick: () -> Unit,
  onAllowPermissionClick: () -> Unit,
  onOssClick: () -> Unit,
  onAboutClick: () -> Unit,
  onAboutDialogDismiss: () -> Unit,
  onGeminiFunctionsClick: () -> Unit,
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
      title = stringResource(R.string.privacy_policy),
      icon = painterResource(R.drawable.ic_fluent_tab_tracking_prevention),
      dividerBottom = true,
      onClick = onPrivacyPolicyClick,
    )
    SettingsItem(
      title = stringResource(R.string.terms_and_conditions),
      icon = painterResource(R.drawable.ic_fluent_document_one_page),
      dividerBottom = true,
      onClick = onTermsClick,
    )
    SettingsItem(
      title = stringResource(R.string.troubleshooting),
      icon = painterResource(R.drawable.ic_fluent_send_logging),
      dividerBottom = true,
      onClick = onTroubleshootingClick,
    )
    SettingsItem(
      title = stringResource(R.string.feedback),
      icon = painterResource(R.drawable.ic_fluent_person_feedback),
      dividerBottom = true,
      onClick = onFeedbackClick,
    )
    SettingsItem(
      title = stringResource(R.string.rate),
      icon = painterResource(R.drawable.ic_fluent_star),
      dividerBottom = true,
      onClick = onRateClick,
    )
    SettingsItem(
      title = stringResource(R.string.tell_friends),
      icon = painterResource(R.drawable.ic_fluent_share),
      dividerBottom = true,
      onClick = onTellFriendsClick,
    )
    SettingsItem(
      title = stringResource(R.string.whats_new),
      icon = painterResource(R.drawable.ic_rocket_whats_new),
      dividerBottom = true,
      onClick = onWhatsNewClick,
    )
    if (state.isGeminiFunctionsVisible) {
      SettingsItem(
        title = stringResource(R.string.gemini_functions),
        icon = painterResource(R.drawable.ic_fluent_apps),
        locked = state.isGeminiFunctionsLocked,
        dividerBottom = true,
        onClick = onGeminiFunctionsClick,
      )
    }
    SettingsItem(
      title = stringResource(R.string.permissions),
      dividerBottom = true,
      onClick = onPermissionsClick,
    )
    SettingsItem(
      title = stringResource(R.string.allow_permission),
      dividerBottom = true,
      onClick = onAllowPermissionClick,
    )
    SettingsItem(
      title = stringResource(R.string.open_source_licenses),
      icon = painterResource(R.drawable.ic_fluent_code),
      dividerBottom = true,
      onClick = onOssClick,
    )
    SettingsItem(
      title = stringResource(R.string.about),
      icon = painterResource(R.drawable.ic_fluent_info),
      dividerBottom = true,
      onClick = onAboutClick,
    )
  }

  val aboutDialog = state.aboutDialog
  if (aboutDialog != null) {
    AlertDialog(
      onDismissRequest = onAboutDialogDismiss,
      title = { Text(aboutDialog.appName) },
      text = {
        Column {
          Text(text = aboutDialog.version, style = MaterialTheme.typography.bodyLarge)
          Text(text = aboutDialog.translators, style = MaterialTheme.typography.bodyMedium)
        }
      },
      confirmButton = { TextButton(onClick = onAboutDialogDismiss) { Text(stringResource(R.string.ok)) } },
    )
  }
}
