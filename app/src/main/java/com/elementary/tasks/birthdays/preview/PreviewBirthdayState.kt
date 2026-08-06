package com.elementary.tasks.birthdays.preview

import com.elementary.tasks.core.data.ui.birthday.UiBirthdayPreview

data class PreviewBirthdayState(
  val birthday: UiBirthdayPreview? = null,
  val isLoading: Boolean = false,
  val showDeleteConfirm: Boolean = false,
  val playConfetti: Boolean = false,
  val canShowAnimation: Boolean = true,
)
