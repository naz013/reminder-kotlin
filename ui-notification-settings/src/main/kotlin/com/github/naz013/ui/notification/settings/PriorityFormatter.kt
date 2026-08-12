package com.github.naz013.ui.notification.settings

import android.content.Context
import com.github.naz013.ui.common.R

class PriorityFormatter(
  private val context: Context,
) : Formatter<Int>() {
  override fun format(priority: Int): String =
    when (priority) {
      0 -> context.getString(R.string.priority_lowest)
      1 -> context.getString(R.string.priority_low)
      2 -> context.getString(R.string.priority_normal)
      3 -> context.getString(R.string.priority_high)
      4 -> context.getString(R.string.priority_highest)
      else -> "NA"
    }
}
