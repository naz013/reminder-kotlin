package com.github.naz013.ui.notification.settings

import android.content.Context
import com.github.naz013.ui.common.R

class LockScreenVisibilityFormatter(
  private val context: Context,
) : Formatter<Int>() {
  override fun format(lockScreenVisibility: Int): String =
    when (lockScreenVisibility) {
      0 -> context.getString(R.string.lock_screen_visibility_public)
      1 -> context.getString(R.string.lock_screen_visibility_private)
      2 -> context.getString(R.string.lock_screen_visibility_secret)
      else -> "NA"
    }
}
