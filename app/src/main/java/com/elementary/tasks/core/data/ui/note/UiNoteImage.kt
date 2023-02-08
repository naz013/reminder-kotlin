package com.elementary.tasks.core.data.ui.note

data class UiNoteImage(
  val id: Int,
  val fileName: String,
  val filePath: String = "",
  val state: UiNoteImageState = UiNoteImageState.READY
)
