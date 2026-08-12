package com.github.naz013.ui.notification.settings

import android.content.Context
import com.github.naz013.ui.common.R

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
