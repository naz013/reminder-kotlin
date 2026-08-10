package com.elementary.tasks.birthdays.preview

import com.elementary.tasks.core.data.ui.birthday.UiBirthdayPreview
import com.github.naz013.ui.tag.TagChipState

data class PreviewBirthdayState(
  val birthday: UiBirthdayPreview? = null,
  val isLoading: Boolean = false,
  val showDeleteConfirm: Boolean = false,
  val playConfetti: Boolean = false,
  val canShowAnimation: Boolean = true,
  val tags: List<TagChipState> = emptyList(),
)
