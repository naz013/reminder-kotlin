package com.github.naz013.feature.reminder.build

internal data class UiSelectorItem(
  val builderItem: BuilderItem<*>,
  val state: UiSelectorItemState,
  val requiredMessage: String? = null,
)

internal sealed class UiSelectorItemState {
  data object UiSelectorAvailable : UiSelectorItemState()

  data class UiSelectorUnavailable(
    val message: String,
  ) : UiSelectorItemState()
}
