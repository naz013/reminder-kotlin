package com.github.naz013.feature.settings.security

data class DisablePinState(
  val pin: String = "",
)

sealed class DisablePinEvent {
  data object ShowPinMismatch : DisablePinEvent()

  data object PinCleared : DisablePinEvent()
}
