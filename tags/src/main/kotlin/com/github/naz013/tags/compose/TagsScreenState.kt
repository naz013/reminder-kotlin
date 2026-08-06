package com.github.naz013.tags.compose

import androidx.compose.ui.graphics.Color

data class TagsScreenState(
  val listState: TagsListState = TagsListState.Loading
)

sealed interface TagsListState {
  data object Loading : TagsListState

  data object Empty : TagsListState

  data class Ready(
    val tags: List<TagState>
  ) : TagsListState
}

data class TagState(
  val id: String,
  val name: String,
  val color: Color,
)
