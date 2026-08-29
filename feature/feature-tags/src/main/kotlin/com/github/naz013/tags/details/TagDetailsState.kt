package com.github.naz013.tags.details

import androidx.compose.ui.graphics.Color

internal data class TagDetailsState(
  val isLoading: Boolean = true,
  val title: String = "",
  val color: Color = Color.Unspecified,
  val canDelete: Boolean = true,
  val searchQuery: String = "",
  val selectedType: TagContentType = TagContentType.ALL,
  val sections: List<TagDetailsSection> = emptyList(),
)

internal data class TagDetailsSection(
  val type: TagContentType,
  val items: List<TagDetailItem>,
)
