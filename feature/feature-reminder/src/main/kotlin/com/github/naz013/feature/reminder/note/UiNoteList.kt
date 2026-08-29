package com.github.naz013.feature.reminder.note

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import com.github.naz013.ui.note.UiNoteImage

internal data class UiNoteList(
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
  val uniqueId: Int,
)
