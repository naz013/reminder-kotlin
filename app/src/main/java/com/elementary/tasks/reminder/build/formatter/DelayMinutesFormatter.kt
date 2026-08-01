package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.R

class DelayMinutesFormatter(
  private val context: Context,
) : Formatter<Int>() {
  override fun format(delayMinutes: Int): String = context.getString(R.string.x_minutes, delayMinutes.toString())
}
