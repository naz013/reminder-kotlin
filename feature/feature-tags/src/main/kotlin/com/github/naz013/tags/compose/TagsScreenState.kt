package com.github.naz013.tags.compose

import androidx.compose.ui.graphics.Color
import com.github.naz013.ui.common.selection.Selectable

internal data class TagsScreenState(
  val listState: TagsListState = TagsListState.Loading,
  val selectedCount: Int = 0,
)

internal sealed interface TagsListState {
  data object Loading : TagsListState

  data object Empty : TagsListState

  data class Ready(
    val tags: List<TagState>
  ) : TagsListState
}

internal data class TagState(
  override val id: String,
  val name: String,
  val color: Color,
  /** Whether this tag is currently open in the two-pane layout's detail pane. */
  val isHighlighted: Boolean = false,
  override val isSelected: Boolean = false,
) : Selectable<TagState> {
  override fun withSelected(selected: Boolean) = copy(isSelected = selected)
}

internal fun TagsScreenState.withSelectedItem(selectedItemId: String?): TagsScreenState {
  val ready = listState as? TagsListState.Ready ?: return this
  return copy(
    listState = TagsListState.Ready(ready.tags.map { it.copy(isHighlighted = it.id == selectedItemId) }),
  )
}

internal enum class TagMenuAction { EDIT, DELETE }
