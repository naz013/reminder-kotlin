package com.elementary.tasks.core.apps

import android.graphics.Bitmap

@Deprecated("After S")
data class UiApplicationList(
  val name: String,
  val packageName: String,
  val icon: Bitmap?,
)
