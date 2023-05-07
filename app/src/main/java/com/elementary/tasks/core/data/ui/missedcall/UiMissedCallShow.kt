package com.elementary.tasks.core.data.ui.missedcall

import android.graphics.drawable.Drawable
import android.net.Uri

@Deprecated("After R")
data class UiMissedCallShow(
  val number: String,
  val uniqueId: Int,
  val formattedTime: String,
  val name: String?,
  val photo: Uri?,
  val avatar: Drawable?
)
