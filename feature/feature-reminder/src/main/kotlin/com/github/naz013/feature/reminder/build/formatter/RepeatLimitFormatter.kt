package com.github.naz013.feature.reminder.build.formatter

import com.github.naz013.ui.notification.settings.Formatter
import android.content.Context
import com.github.naz013.ui.common.R

class RepeatLimitFormatter(
  private val context: Context,
) : Formatter<Int>() {
  override fun format(value: Int): String =
    when {
      value < 0 -> context.getString(R.string.no_limits)
      value == 0 -> context.getString(R.string.builder_not_repeating)
      else -> "$value " + context.getString(R.string.builder_repeats)
    }
}
