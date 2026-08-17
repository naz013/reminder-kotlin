package com.github.naz013.feature.reminder.build.formatter

import com.github.naz013.ui.notification.settings.Formatter
import android.content.Context
import com.github.naz013.feature.reminder.util.LED

internal class LedColorFormatter(
  private val context: Context,
) : Formatter<Int>() {
  override fun format(ledColor: Int): String = LED.getTitle(context, ledColor)
}
