package com.elementary.tasks.core.data.ui.note

import android.graphics.Typeface
import androidx.annotation.ColorInt

data class UiNotePreview(
  val id: String,
  val text: String,
  @ColorInt val backgroundColor: Int,
  val opacity: Int,
  val typeface: Typeface?,
  val images: List<UiNoteImage>,
  val uniqueId: Int,
  val textSize: Float
)
