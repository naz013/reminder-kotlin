package com.github.naz013.feature.settings.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem

@Composable
fun SecuritySettingsScreen(
  state: SecuritySettingsState,
  onPinRowClick: () -> Unit,
  onChangePinClick: () -> Unit,
  onFingerprintClick: () -> Unit,
  onShuffleToggle: () -> Unit,
  onTelephonyToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()),
  ) {
    SettingsSwitchItem(
      title = stringResource(R.string.pin_protection),
      checked = state.isPinChecked,
      onCheckedChange = { onPinRowClick() },
      subtitleOn = stringResource(R.string.protect_application_with_pin),
      subtitleOff = stringResource(R.string.do_not_use_pin_protection),
      icon = painterResource(R.drawable.ic_fluent_password),
      dividerBottom = true,
    )
    SettingsItem(
      title = stringResource(R.string.change_pin),
      icon = painterResource(R.drawable.ic_fluent_edit),
      enabled = state.isPinChecked,
      dividerBottom = true,
      onClick = onChangePinClick,
    )
    if (state.hasBiometricHardware) {
      SettingsSwitchItem(
        title = stringResource(R.string.fingerprint),
        checked = state.isFingerprintChecked,
        onCheckedChange = { onFingerprintClick() },
        subtitleOn = stringResource(R.string.allow_fingerprint_login),
        subtitleOff = stringResource(R.string.do_not_use_fingerprint_to_login),
        icon = painterResource(R.drawable.ic_fluent_fingerprint),
        enabled = state.isPinChecked,
        dividerBottom = true,
      )
    }
    SettingsSwitchItem(
      title = stringResource(R.string.shuffle_digits),
      checked = state.isShuffleChecked,
      onCheckedChange = { onShuffleToggle() },
      subtitleOn = stringResource(R.string.shuffle_digits_during_pin_login),
      subtitleOff = stringResource(R.string.do_not_shuffle_digits_during_pin_login),
      enabled = state.isPinChecked,
      dividerBottom = true,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.phone_calls_and_sms),
      checked = state.isTelephonyChecked,
      onCheckedChange = { onTelephonyToggle() },
      subtitleOn = stringResource(R.string.allow_phone_call_and_sms_func),
      subtitleOff = stringResource(R.string.hide_that_functionality),
      icon = painterResource(R.drawable.ic_fluent_phone),
      enabled = state.hasTelephony,
      dividerBottom = true,
    )
  }
}
