package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.R

class CategoryFormatter(
  private val context: Context,
) : Formatter<Int>() {
  override fun format(category: Int): String =
    when (category) {
      0 -> context.getString(R.string.notification_category_default)
      1 -> context.getString(R.string.notification_category_alarm)
      2 -> context.getString(R.string.notification_category_event)
      3 -> context.getString(R.string.notification_category_call)
      else -> "NA"
    }
}
