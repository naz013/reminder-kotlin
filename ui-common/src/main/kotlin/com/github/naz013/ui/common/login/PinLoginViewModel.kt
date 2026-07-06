package com.github.naz013.ui.common.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

internal class PinLoginViewModel(
  private val authPreferences: AuthPreferences,
) : ViewModel() {

  val state: StateFlow<PinLoginState> field = MutableStateFlow(
    PinLoginState(shuffleDigits = authPreferences.shufflePinView),
  )

  private val eventChannel = Channel<PinLoginEvent>(Channel.BUFFERED)
  val navigationEvent: Flow<PinLoginEvent> = eventChannel.receiveAsFlow()

  fun onDigitClick(digit: Int) {
    val pin = state.value.pin
    if (pin.length >= PIN_LENGTH) return
    val updated = pin + digit
    if (updated.length < PIN_LENGTH) {
      state.update { it.copy(pin = updated) }
      return
    }
    if (updated == authPreferences.pinCode) {
      eventChannel.trySend(PinLoginEvent.Success)
    } else {
      eventChannel.trySend(PinLoginEvent.ShowPinMismatch)
      state.update { it.copy(pin = "") }
    }
  }

  fun onDeleteClick() {
    state.update { it.copy(pin = "") }
  }

  fun onFingerprintSucceeded() {
    eventChannel.trySend(PinLoginEvent.Success)
  }

  companion object {
    private const val PIN_LENGTH = 6
  }
}
