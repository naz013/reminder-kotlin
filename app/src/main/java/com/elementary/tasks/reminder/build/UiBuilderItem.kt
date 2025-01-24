package com.elementary.tasks.reminder.build

import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.reminder.build.bi.BuilderItemError

sealed class UiBuilderItem {
  abstract val key: Any
  abstract val builderItem: BuilderItem<*>
  abstract val state: UiListBuilderItemState
  abstract val value: String
  abstract val errorText: String
}

data class UiListBuilderItem(
  override val builderItem: BuilderItem<*>,
  override val state: UiListBuilderItemState,
  override val value: String,
  override val errorText: String
) : UiBuilderItem() {
  override val key: Any = builderItem.biType
}

data class UiListNoteBuilderItem(
  override val builderItem: NoteBuilderItem,
  override val state: UiListBuilderItemState,
  override val value: String,
  override val errorText: String,
  val noteData: UiNoteList?
) : UiBuilderItem() {
  override val key: Any = builderItem.biType
}

sealed class UiListBuilderItemState {
  data object EmptyState : UiListBuilderItemState()
  data object DoneState : UiListBuilderItemState()
  data class ErrorState(
    val errors: List<BuilderItemError>
  ) : UiListBuilderItemState()
}
