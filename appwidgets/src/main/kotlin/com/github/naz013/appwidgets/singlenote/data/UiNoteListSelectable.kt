package com.github.naz013.appwidgets.singlenote.data

import android.graphics.Typeface
import androidx.annotation.ColorInt

internal data class UiNoteListSelectable(
  val id: String,
  val text: String,
  @param:ColorInt val backgroundColor: Int,
  @param:ColorInt val textColor: Int,
  val typeface: Typeface?,
  val fontSize: Float,
  val images: List<UiNoteImage>,
  val dartIcon: Boolean
) {
  var isSelected: Boolean = false
}
