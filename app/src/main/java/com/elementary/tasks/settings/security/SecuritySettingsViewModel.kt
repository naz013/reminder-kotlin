package com.elementary.tasks.settings.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.system.SystemInfo
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SecuritySettingsViewModel(
  private val prefs: Prefs,
  analyticsEventSender: AnalyticsEventSender,
  private val systemInfo: SystemInfo,
) : ViewModel() {
  val state: StateFlow<SecuritySettingsState> field = MutableStateFlow(buildState())
  val navigationEvent: LiveData<Event<SecuritySettingsEvent>> field = mutableLiveEventOf()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.SECURITY_SETTINGS))
    if (!systemInfo.hasTelephony) {
      prefs.isTelephonyEnabled = false
    }
    refreshState()
  }

  fun onPinRowClick() {
    val event =
      if (state.value.isPinChecked) {
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
    refreshState()
  }

  fun onShuffleToggle() {
    prefs.shufflePinView = !prefs.shufflePinView
    refreshState()
  }

  fun onTelephonyToggle() {
    prefs.isTelephonyEnabled = !prefs.isTelephonyEnabled
    refreshState()
  }

  private fun refreshState() {
    state.update { buildState() }
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
