package com.elementary.tasks.core.data.ui.note

import android.graphics.Bitmap
import androidx.annotation.ColorInt

data class UiNoteNotification(
  val id: String,
  val text: String,
  @ColorInt
  val backgroundColor: Int,
  @ColorInt
  val textColor: Int,
  val image: Bitmap? = null,
  val uniqueId: Int
)
