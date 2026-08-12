package com.elementary.tasks.reminder.build.formatter

import com.github.naz013.ui.notification.settings.Formatter
import android.content.Context
import com.elementary.tasks.R

class WakeScreenFormatter(
  private val context: Context,
) : Formatter<Boolean>() {
  override fun format(wakeScreen: Boolean): String =
    context.getString(if (wakeScreen) R.string.wake_screen_enabled else R.string.wake_screen_disabled)
}
