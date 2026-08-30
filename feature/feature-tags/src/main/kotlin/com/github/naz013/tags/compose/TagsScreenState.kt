package com.github.naz013.tags.compose

import androidx.compose.ui.graphics.Color

internal data class TagsScreenState(
  val listState: TagsListState = TagsListState.Loading
)

internal sealed interface TagsListState {
  data object Loading : TagsListState

  data object Empty : TagsListState

  data class Ready(
    val tags: List<TagState>
  ) : TagsListState
}

internal data class TagState(
  val id: String,
  val name: String,
  val color: Color,
  val isSelected: Boolean = false,
)

internal fun TagsScreenState.withSelectedItem(selectedItemId: String?): TagsScreenState {
  val ready = listState as? TagsListState.Ready ?: return this
  return copy(
    listState = TagsListState.Ready(ready.tags.map { it.copy(isSelected = it.id == selectedItemId) }),
  )
}

internal enum class TagMenuAction { EDIT, DELETE }
