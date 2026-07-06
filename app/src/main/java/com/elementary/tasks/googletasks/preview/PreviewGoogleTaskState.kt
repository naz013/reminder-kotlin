package com.elementary.tasks.googletasks.preview

import com.elementary.tasks.core.data.ui.google.UiGoogleTaskPreview

data class PreviewGoogleTaskState(
  val task: UiGoogleTaskPreview? = null,
  val isLoading: Boolean = false,
)

sealed interface PreviewGoogleTaskEvent {
  data object Deleted : PreviewGoogleTaskEvent
}
