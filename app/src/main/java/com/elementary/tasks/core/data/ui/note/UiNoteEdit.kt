package com.elementary.tasks.core.data.ui.note

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

data class UiNoteEdit(
  val id: String,
  val text: String,
  val typeface: Int,
  val images: List<UiNoteImage>,
  val colorPosition: Int,
  val colorPalette: Int,
  val opacity: Int
)
