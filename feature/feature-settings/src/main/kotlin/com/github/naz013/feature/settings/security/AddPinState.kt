package com.github.naz013.feature.settings.security

internal data class AddPinState(
  val stage: AddPinStage = AddPinStage.INPUT,
  val pin: String = "",
)

internal enum class AddPinStage { INPUT, REPEAT }

internal sealed class AddPinEvent {
  data object ShowPinMismatch : AddPinEvent()

  data object PinSaved : AddPinEvent()
}
