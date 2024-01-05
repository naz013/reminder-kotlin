package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.R

class PriorityFormatter(
  private val context: Context
) : Formatter<Int>() {

  override fun format(priority: Int): String {
    return when (priority) {
      0 -> context.getString(R.string.priority_lowest)
      1 -> context.getString(R.string.priority_low)
      2 -> context.getString(R.string.priority_normal)
      3 -> context.getString(R.string.priority_high)
      4 -> context.getString(R.string.priority_highest)
      else -> "NA"
    }
  }
}
