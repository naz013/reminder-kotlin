package com.github.naz013.feature.settings.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class AddPinViewModel(
  private val prefs: SecuritySettingsPreferences,
) : ViewModel() {
  val state: StateFlow<AddPinState> field = MutableStateFlow(AddPinState())
  val navigationEvent: LiveData<Event<AddPinEvent>> field = mutableLiveEventOf()

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
      AddPinStage.INPUT -> {
        firstPin = updated
        state.update { AddPinState(stage = AddPinStage.REPEAT) }
      }

      AddPinStage.REPEAT -> {
        if (firstPin == updated) {
          prefs.pinCode = updated
          navigationEvent.value = Event(AddPinEvent.PinSaved)
        } else {
          navigationEvent.value = Event(AddPinEvent.ShowPinMismatch)
          state.update { AddPinState(stage = AddPinStage.INPUT) }
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
