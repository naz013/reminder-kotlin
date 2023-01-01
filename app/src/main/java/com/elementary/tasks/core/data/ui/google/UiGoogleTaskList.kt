package com.elementary.tasks.core.data.ui.google

import android.graphics.Bitmap

data class UiGoogleTaskList(
  val id: String,
  val text: String,
  val notes: String?,
  val dueDate: String?,
  val statusIcon: Bitmap?
)
