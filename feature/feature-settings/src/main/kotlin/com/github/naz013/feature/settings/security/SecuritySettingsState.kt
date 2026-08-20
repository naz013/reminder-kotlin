package com.github.naz013.feature.settings.security

internal data class SecuritySettingsState(
  val isPinChecked: Boolean = false,
  val isFingerprintChecked: Boolean = false,
  val isShuffleChecked: Boolean = false,
  val isTelephonyChecked: Boolean = false,
  val hasBiometricHardware: Boolean = false,
  val hasTelephony: Boolean = false,
)

internal sealed class SecuritySettingsEvent {
  data object OpenAddPin : SecuritySettingsEvent()

  data object OpenDisablePin : SecuritySettingsEvent()

  data object OpenChangePin : SecuritySettingsEvent()

  data object TryBiometricLogin : SecuritySettingsEvent()
}
