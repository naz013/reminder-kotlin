package com.elementary.tasks.core.data.ui.note

import android.graphics.Typeface
import androidx.annotation.ColorInt

data class UiNoteListSelectable(
  val id: String,
  val text: String,
  @ColorInt val backgroundColor: Int,
  @ColorInt val textColor: Int,
  val typeface: Typeface?,
  val fontSize: Float,
  val images: List<UiNoteImage>,
  val dartIcon: Boolean
) {
  var isSelected: Boolean = false
}
