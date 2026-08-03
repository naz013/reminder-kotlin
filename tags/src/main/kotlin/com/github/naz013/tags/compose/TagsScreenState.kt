package com.github.naz013.tags.compose

import com.github.naz013.domain.Tag

data class TagsScreenState(
  val listState: TagsListState = TagsListState.Loading
)

sealed interface TagsListState {
  data object Loading : TagsListState

  data object Empty : TagsListState

  data class Ready(
    val tags: List<Tag>
  ) : TagsListState
}
