package com.github.naz013.feature.settings.security

internal data class DisablePinState(
  val pin: String = "",
)

internal sealed class DisablePinEvent {
  data object ShowPinMismatch : DisablePinEvent()

  data object PinCleared : DisablePinEvent()
}
