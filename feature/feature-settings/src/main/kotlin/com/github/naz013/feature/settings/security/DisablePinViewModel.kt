package com.github.naz013.feature.settings.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class DisablePinViewModel(
  private val prefs: SecuritySettingsPreferences,
) : ViewModel() {
  val state: StateFlow<DisablePinState> field = MutableStateFlow(DisablePinState())
  val navigationEvent: LiveData<Event<DisablePinEvent>> field = mutableLiveEventOf()

  fun onDigitClick(digit: Int) {
    val pin = state.value.pin
    if (pin.length >= PIN_LENGTH) return
    val updated = pin + digit
    if (updated.length < PIN_LENGTH) {
      state.update { it.copy(pin = updated) }
      return
    }
    if (prefs.pinCode == updated) {
      prefs.pinCode = ""
      navigationEvent.value = Event(DisablePinEvent.PinCleared)
    } else {
      navigationEvent.value = Event(DisablePinEvent.ShowPinMismatch)
      state.update { it.copy(pin = "") }
    }
  }

  fun onDeleteClick() {
    state.update { it.copy(pin = "") }
  }

  companion object {
    private const val PIN_LENGTH = 6
  }
}
