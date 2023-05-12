package com.elementary.tasks.core.data.ui.note

data class UiNoteEdit(
  val id: String,
  val text: String,
  val typeface: Int,
  val images: List<UiNoteImage>,
  val colorPosition: Int,
  val colorPalette: Int,
  val opacity: Int,
  val fontSize: Int
)
