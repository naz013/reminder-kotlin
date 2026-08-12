package com.elementary.tasks.reminder.build.formatter

import com.github.naz013.ui.notification.settings.Formatter
import android.content.Context
import com.elementary.tasks.core.utils.LED

class LedColorFormatter(
  private val context: Context,
) : Formatter<Int>() {
  override fun format(ledColor: Int): String = LED.getTitle(context, ledColor)
}
