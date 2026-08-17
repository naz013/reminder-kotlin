package com.github.naz013.feature.settings.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.platform.SystemInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

class SecuritySettingsViewModel(
  private val prefs: SecuritySettingsPreferences,
  analyticsEventSender: AnalyticsEventSender,
  private val systemInfo: SystemInfo,
) : ViewModel() {

  private val _state = MutableStateFlow(SecuritySettingsState())
  val state = _state.stateInWhileSubscribed(SecuritySettingsState())
    .onStart { loadState() }
  val navigationEvent: LiveData<Event<SecuritySettingsEvent>> field = mutableLiveEventOf()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.SECURITY_SETTINGS))
  }

  fun onPinRowClick() {
    val event = if (_state.value.isPinChecked) {
      SecuritySettingsEvent.OpenDisablePin
    } else {
      SecuritySettingsEvent.OpenAddPin
    }
    navigationEvent.value = Event(event)
  }

  fun onChangePinClick() {
    navigationEvent.value = Event(SecuritySettingsEvent.OpenChangePin)
  }

  fun onBiometricAuthClicked() {
    navigationEvent.value = Event(SecuritySettingsEvent.TryBiometricLogin)
  }

  fun onBiometricAuthSuccess() {
    prefs.useFingerprint = !prefs.useFingerprint
    loadState()
  }

  fun onShuffleToggle() {
    prefs.shufflePinView = !prefs.shufflePinView
    loadState()
  }

  fun onTelephonyToggle() {
    prefs.isTelephonyEnabled = !prefs.isTelephonyEnabled
    loadState()
  }

  private fun loadState() {
    if (!systemInfo.hasTelephony) {
      prefs.isTelephonyEnabled = false
    }
    _state.update { buildState() }
  }

  private fun buildState(): SecuritySettingsState =
    SecuritySettingsState(
      isPinChecked = prefs.hasPinCode,
      isFingerprintChecked = prefs.useFingerprint,
      isShuffleChecked = prefs.shufflePinView,
      isTelephonyChecked = prefs.isTelephonyEnabled,
      hasBiometricHardware = systemInfo.hasBiometricHardware,
      hasTelephony = systemInfo.hasTelephony,
    )
}
