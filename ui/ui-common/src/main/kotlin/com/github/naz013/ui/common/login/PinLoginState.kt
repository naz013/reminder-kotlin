package com.github.naz013.ui.common.login

internal data class PinLoginState(
  val pin: String = "",
  val shuffleDigits: Boolean = false,
)

internal sealed class PinLoginEvent {
  data object Success : PinLoginEvent()

  data object ShowPinMismatch : PinLoginEvent()
}
