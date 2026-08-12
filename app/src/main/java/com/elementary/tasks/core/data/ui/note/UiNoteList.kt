package com.elementary.tasks.core.data.ui.note

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import com.github.naz013.ui.note.UiNoteImage

data class UiNoteList(
  val id: String,
  val text: String,
  val title: String,
  @ColorInt val backgroundColor: Int,
  @ColorInt val textColor: Int,
  val moreIcon: Drawable?,
  val typeface: Typeface?,
  val fontSize: Float,
  val titleTypeface: Typeface?,
  val titleFontSize: Float,
  val formattedDateTime: String,
  val images: List<UiNoteImage>,
  val colorPosition: Int,
  val colorPalette: Int,
  val uniqueId: Int,
)
