package com.github.naz013.feature.reminder.apps

import android.graphics.Bitmap

@Deprecated("After S")
internal data class UiApplicationList(
  val name: String,
  val packageName: String,
  val icon: Bitmap?,
)
