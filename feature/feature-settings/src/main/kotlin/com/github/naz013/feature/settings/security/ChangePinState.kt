package com.github.naz013.feature.settings.security

data class ChangePinState(
  val stage: ChangePinStage = ChangePinStage.OLD,
  val pin: String = "",
)

enum class ChangePinStage { OLD, INPUT, REPEAT }

sealed class ChangePinEvent {
  data object ShowPinMismatch : ChangePinEvent()

  data object PinSaved : ChangePinEvent()
}
