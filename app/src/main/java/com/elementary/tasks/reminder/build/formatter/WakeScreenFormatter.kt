package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.R

class WakeScreenFormatter(
  private val context: Context,
) : Formatter<Boolean>() {
  override fun format(wakeScreen: Boolean): String =
    context.getString(if (wakeScreen) R.string.wake_screen_enabled else R.string.wake_screen_disabled)
}
