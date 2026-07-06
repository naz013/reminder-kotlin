package com.elementary.tasks.settings.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SecuritySettingsViewModel(
  private val prefs: Prefs,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {

  val state: StateFlow<SecuritySettingsState> field = MutableStateFlow(buildState())
  val navigationEvent: LiveData<Event<SecuritySettingsEvent>> field = mutableLiveEventOf()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.SECURITY_SETTINGS))
  }

  /** Re-reads PIN/telephony state - called on every resume since Add/Disable Pin sub-screens
   *  change [Prefs.hasPinCode] outside this ViewModel, and telephony access must be force-disabled
   *  whenever the device has no telephony hardware. */
  fun onResume(hasTelephony: Boolean) {
    if (!hasTelephony) {
      prefs.isTelephonyEnabled = false
    }
    refreshState()
  }

  fun onPinRowClick() {
    val event = if (state.value.isPinChecked) {
      SecuritySettingsEvent.OpenDisablePin
    } else {
      SecuritySettingsEvent.OpenAddPin
    }
    navigationEvent.value = Event(event)
  }

  fun onChangePinClick() {
    navigationEvent.value = Event(SecuritySettingsEvent.OpenChangePin)
  }

  fun onFingerprintClick() {
    navigationEvent.value = Event(SecuritySettingsEvent.TryFingerprintLogin)
  }

  fun onFingerprintAuthSucceeded() {
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

  private fun buildState(): SecuritySettingsState = SecuritySettingsState(
    isPinChecked = prefs.hasPinCode,
    isFingerprintChecked = prefs.useFingerprint,
    isShuffleChecked = prefs.shufflePinView,
    isTelephonyChecked = prefs.isTelephonyEnabled,
  )
}
