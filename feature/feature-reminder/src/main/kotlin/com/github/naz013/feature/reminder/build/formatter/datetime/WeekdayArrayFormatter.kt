package com.github.naz013.feature.reminder.build.formatter.datetime

import android.content.Context
import com.github.naz013.ui.common.R
import com.github.naz013.ui.notification.settings.Formatter

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
internal class WeekdayArrayFormatter(
  private val context: Context,
) : Formatter<List<Int>>() {
  override fun format(daysOfWeek: List<Int>): String {
    val sb = StringBuilder()
    daysOfWeek.forEachIndexed { index, i ->
      if (i == 1) {
        sb.append(getValue(index))
        sb.append(", ")
      }
    }
    return if (sb.endsWith(", ")) {
      sb.substring(0, sb.length - 2)
    } else {
      sb.toString()
    }
  }

  private fun getValue(dayOfWeek: Int): String =
    when (dayOfWeek) {
      0 -> context.getString(R.string.sun)
      1 -> context.getString(R.string.mon)
      2 -> context.getString(R.string.tue)
      3 -> context.getString(R.string.wed)
      4 -> context.getString(R.string.thu)
      5 -> context.getString(R.string.fri)
      6 -> context.getString(R.string.sat)
      else -> "NA"
    }
}
