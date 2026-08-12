package com.github.naz013.feature.note

import android.graphics.Typeface
import androidx.annotation.ColorInt
import com.github.naz013.ui.note.UiNoteImage

internal data class UiNotePreview(
  val id: String,
  val text: String,
  val title: String,
  val typeface: Typeface?,
  val images: List<UiNoteImage>,
  val uniqueId: Int,
  val textSize: Float,
  val titleTypeface: Typeface?,
  val titleTextSize: Float,
  val isArchived: Boolean,
)
