package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.core.utils.LED

class LedColorFormatter(
  private val context: Context
) : Formatter<Int>() {

  override fun format(ledColor: Int): String {
    return LED.getTitle(context, ledColor)
  }
}
