package com.github.naz013.feature.reminder.build

import com.github.naz013.feature.reminder.note.UiNoteList
import com.github.naz013.feature.reminder.build.bi.BuilderItemError

internal sealed class UiBuilderItem {
  abstract val key: Any
  abstract val builderItem: BuilderItem<*>
  abstract val state: UiListBuilderItemState
  abstract val value: String
  abstract val errorText: String
}

internal data class UiListBuilderItem(
  override val builderItem: BuilderItem<*>,
  override val state: UiListBuilderItemState,
  override val value: String,
  override val errorText: String,
) : UiBuilderItem() {
  override val key: Any = builderItem.biType
}

internal data class UiListNoteBuilderItem(
  override val builderItem: NoteBuilderItem,
  override val state: UiListBuilderItemState,
  override val value: String,
  override val errorText: String,
  val noteData: UiNoteList?,
) : UiBuilderItem() {
  override val key: Any = builderItem.biType
}

internal sealed class UiListBuilderItemState {
  data object EmptyState : UiListBuilderItemState()

  data object DoneState : UiListBuilderItemState()

  data class ErrorState(
    val errors: List<BuilderItemError>,
  ) : UiListBuilderItemState()
}
