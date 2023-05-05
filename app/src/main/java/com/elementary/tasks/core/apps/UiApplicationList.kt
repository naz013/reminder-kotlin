package com.elementary.tasks.core.apps

import android.graphics.drawable.Drawable

@Deprecated("After S")
data class UiApplicationList(
  val name: String?,
  val packageName: String?,
  val drawable: Drawable?
)
