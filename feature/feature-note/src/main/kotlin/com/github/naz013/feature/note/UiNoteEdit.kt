package com.github.naz013.feature.note

import com.github.naz013.ui.note.UiNoteImage

internal data class UiNoteEdit(
  val id: String,
  val text: String,
  val typeface: Int,
  val title: String,
  val titleTypeface: Int,
  val titleFontSize: Int,
  val images: List<UiNoteImage>,
  val colorPosition: Int,
  val colorPalette: Int,
  val opacity: Int,
  val fontSize: Int,
  val isArchived: Boolean,
)
