package com.elementary.tasks.reminder.build

import com.elementary.tasks.reminder.build.bi.BuilderItemError

sealed class UiBuilderItem {
  abstract val key: Any
}

data class UiListBuilderItem(
  val builderItem: BuilderItem<*>,
  val state: UiLitBuilderItemState,
  val value: String,
  val errorText: String
) : UiBuilderItem() {
  override val key: Any = builderItem.biType
}

sealed class UiLitBuilderItemState {
  data object EmptyState : UiLitBuilderItemState()
  data object DoneState : UiLitBuilderItemState()
  data class ErrorState(
    val errors: List<BuilderItemError>
  ) : UiLitBuilderItemState()
}
