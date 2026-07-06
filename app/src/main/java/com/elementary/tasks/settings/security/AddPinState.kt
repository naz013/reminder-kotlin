package com.elementary.tasks.settings.security

data class AddPinState(
  val stage: AddPinStage = AddPinStage.INPUT,
  val pin: String = "",
)

enum class AddPinStage { INPUT, REPEAT }

sealed class AddPinEvent {
  data object ShowPinMismatch : AddPinEvent()

  data object PinSaved : AddPinEvent()
}
