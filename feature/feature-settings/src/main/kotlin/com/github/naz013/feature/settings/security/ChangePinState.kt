package com.github.naz013.feature.settings.security

internal data class ChangePinState(
  val stage: ChangePinStage = ChangePinStage.OLD,
  val pin: String = "",
)

internal enum class ChangePinStage { OLD, INPUT, REPEAT }

internal sealed class ChangePinEvent {
  data object ShowPinMismatch : ChangePinEvent()

  data object PinSaved : ChangePinEvent()
}
