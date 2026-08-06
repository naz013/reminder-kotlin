package com.github.naz013.feature.note

data class UiNoteImage(
  val id: Int,
  val fileName: String,
  val filePath: String = "",
  val state: UiNoteImageState = UiNoteImageState.READY
)

enum class UiNoteImageState {
  LOADING,
  READY,
  ERROR
}
