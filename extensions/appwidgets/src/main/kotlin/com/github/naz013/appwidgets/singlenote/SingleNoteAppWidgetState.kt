package com.github.naz013.appwidgets.singlenote

import android.graphics.Bitmap

internal data class SingleNoteAppWidgetState(
  val widgetId: Int,
  val noteId: String?,
  val bitmap: Bitmap?
)
