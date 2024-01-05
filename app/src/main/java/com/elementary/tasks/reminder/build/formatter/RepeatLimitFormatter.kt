package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.R

class RepeatLimitFormatter(
  private val context: Context
) : Formatter<Int>() {

  override fun format(value: Int): String {
    return when {
      value < 0 -> context.getString(R.string.no_limits)
      value == 0 -> context.getString(R.string.builder_not_repeating)
      else -> "$value " + context.getString(R.string.builder_repeats)
    }
  }
}
