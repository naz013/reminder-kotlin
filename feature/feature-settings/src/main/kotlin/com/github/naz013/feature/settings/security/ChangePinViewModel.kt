package com.github.naz013.feature.settings.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ChangePinViewModel(
  private val prefs: SecuritySettingsPreferences,
) : ViewModel() {
  val state: StateFlow<ChangePinState> field = MutableStateFlow(ChangePinState())
  val navigationEvent: LiveData<Event<ChangePinEvent>> field = mutableLiveEventOf()

  private var firstPin = ""

  fun onDigitClick(digit: Int) {
    val pin = state.value.pin
    if (pin.length >= PIN_LENGTH) return
    val updated = pin + digit
    if (updated.length < PIN_LENGTH) {
      state.update { it.copy(pin = updated) }
      return
    }
    when (state.value.stage) {
      ChangePinStage.OLD -> {
        if (prefs.pinCode == updated) {
          state.update { ChangePinState(stage = ChangePinStage.INPUT) }
        } else {
          navigationEvent.value = Event(ChangePinEvent.ShowPinMismatch)
          state.update { it.copy(pin = "") }
        }
      }

      ChangePinStage.INPUT -> {
        firstPin = updated
        state.update { ChangePinState(stage = ChangePinStage.REPEAT) }
      }

      ChangePinStage.REPEAT -> {
        if (firstPin == updated) {
          prefs.pinCode = updated
          navigationEvent.value = Event(ChangePinEvent.PinSaved)
        } else {
          navigationEvent.value = Event(ChangePinEvent.ShowPinMismatch)
          state.update { ChangePinState(stage = ChangePinStage.INPUT) }
        }
        firstPin = ""
      }
    }
  }

  fun onDeleteClick() {
    state.update { it.copy(pin = "") }
  }

  companion object {
    private const val PIN_LENGTH = 6
  }
}
