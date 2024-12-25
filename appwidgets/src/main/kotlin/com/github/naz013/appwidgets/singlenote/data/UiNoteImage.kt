package com.github.naz013.appwidgets.singlenote.data

internal data class UiNoteImage(
  val id: Int,
  val fileName: String,
  val filePath: String = "",
  val state: UiNoteImageState = UiNoteImageState.READY
)
