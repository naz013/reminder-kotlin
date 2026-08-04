package com.elementary.tasks.googletasks.preview

import com.elementary.tasks.core.data.ui.google.UiGoogleTaskPreview

data class PreviewGoogleTaskState(
  val task: UiGoogleTaskPreview? = null,
  val isLoading: Boolean = false,
  val showDeleteConfirm: Boolean = false,
)
