package com.elementary.tasks.core.data.ui.note

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

data class UiNoteList(
  val id: String,
  val text: String,
  @ColorInt val backgroundColor: Int,
  @ColorInt val textColor: Int,
  val moreIcon: Drawable?,
  val typeface: Typeface?,
  val fontSize: Float,
  val formattedDateTime: String,
  val images: List<UiNoteImage>,
  val colorPosition: Int,
  val colorPalette: Int,
  val uniqueId: Int
)
