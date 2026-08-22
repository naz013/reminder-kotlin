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
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem

@Composable
internal fun OtherSettingsScreen(
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
  onBuyMeACoffeeClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState()),
  ) {
    SettingsItem(
      title = stringResource(R.string.privacy_policy),
      icon = AppIcons.Fluent.TabTrackingPrevention,
      dividerBottom = true,
      onClick = onPrivacyPolicyClick,
    )
    SettingsItem(
      title = stringResource(R.string.terms_and_conditions),
      icon = AppIcons.Fluent.DocumentOnePage,
      dividerBottom = true,
      onClick = onTermsClick,
    )
    SettingsItem(
      title = stringResource(R.string.troubleshooting),
      icon = AppIcons.Fluent.SendLogging,
      dividerBottom = true,
      onClick = onTroubleshootingClick,
    )
    SettingsItem(
      title = stringResource(R.string.feedback),
      icon = AppIcons.Fluent.PersonFeedback,
      dividerBottom = true,
      onClick = onFeedbackClick,
    )
    SettingsItem(
      title = stringResource(R.string.rate),
      icon = AppIcons.Fluent.Star,
      dividerBottom = true,
      onClick = onRateClick,
    )
    SettingsItem(
      title = stringResource(R.string.tell_friends),
      icon = AppIcons.Fluent.Share,
      dividerBottom = true,
      onClick = onTellFriendsClick,
    )
    SettingsItem(
      title = stringResource(R.string.whats_new),
      icon = AppIcons.RocketWhatsNew,
      dividerBottom = true,
      onClick = onWhatsNewClick,
    )
    if (state.isGeminiFunctionsVisible) {
      SettingsItem(
        title = stringResource(R.string.gemini_functions),
        icon = AppIcons.Fluent.Extension,
        locked = state.isGeminiFunctionsLocked,
        dividerBottom = true,
        onClick = onGeminiFunctionsClick,
      )
    }
    if (state.isBuyMeACoffeeVisible) {
      SettingsItem(
        title = stringResource(R.string.buy_me_a_coffee),
        subtitle = stringResource(R.string.buy_me_a_coffee_subtitle),
        icon = AppIcons.Fluent.DrinkCoffee,
        dividerBottom = true,
        onClick = onBuyMeACoffeeClick,
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
      icon = AppIcons.Fluent.Code,
      dividerBottom = true,
      onClick = onOssClick,
    )
    SettingsItem(
      title = stringResource(R.string.about),
      icon = AppIcons.Fluent.Info,
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
