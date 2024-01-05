package com.elementary.tasks.reminder.build

data class UiSelectorItem(
  val builderItem: BuilderItem<*>,
  val state: UiSelectorItemState
)

sealed class UiSelectorItemState {
  data object UiSelectorAvailable : UiSelectorItemState()
  data class UiSelectorUnavailable(
    val message: String
  ) : UiSelectorItemState()
}
