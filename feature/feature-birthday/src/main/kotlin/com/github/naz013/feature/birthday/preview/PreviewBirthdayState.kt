package com.github.naz013.feature.birthday.preview

import com.github.naz013.ui.birthday.UiBirthdayPreview
import com.github.naz013.ui.tag.TagChipState

data class PreviewBirthdayState(
  val birthday: UiBirthdayPreview? = null,
  val isLoading: Boolean = false,
  val showDeleteConfirm: Boolean = false,
  val playConfetti: Boolean = false,
  val canShowAnimation: Boolean = true,
  val tags: List<TagChipState> = emptyList(),
)
