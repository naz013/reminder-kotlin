package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.R

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
