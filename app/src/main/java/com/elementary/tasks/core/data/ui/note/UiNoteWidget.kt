package com.elementary.tasks.core.data.ui.note

import android.graphics.Bitmap

data class UiNoteWidget(
  val id: String,
  val uniqueId: Int,
  val bitmap: Bitmap,
  val settingsIcon: Bitmap?
)
