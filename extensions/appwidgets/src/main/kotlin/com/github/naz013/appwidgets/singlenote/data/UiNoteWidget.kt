package com.github.naz013.appwidgets.singlenote.data

import android.graphics.Bitmap

internal data class UiNoteWidget(
  val id: String,
  val uniqueId: Int,
  val bitmap: Bitmap?,
  val settingsIcon: Bitmap?
)
