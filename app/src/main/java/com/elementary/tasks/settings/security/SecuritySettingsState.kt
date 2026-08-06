package com.elementary.tasks.settings.security

data class SecuritySettingsState(
  val isPinChecked: Boolean = false,
  val isFingerprintChecked: Boolean = false,
  val isShuffleChecked: Boolean = false,
  val isTelephonyChecked: Boolean = false,
  val hasBiometricHardware: Boolean = false,
  val hasTelephony: Boolean = false,
)

sealed class SecuritySettingsEvent {
  data object OpenAddPin : SecuritySettingsEvent()

  data object OpenDisablePin : SecuritySettingsEvent()

  data object OpenChangePin : SecuritySettingsEvent()

  data object TryBiometricLogin : SecuritySettingsEvent()
}
