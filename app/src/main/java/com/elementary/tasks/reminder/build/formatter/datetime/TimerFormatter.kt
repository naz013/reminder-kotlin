package com.elementary.tasks.reminder.build.formatter.datetime

import android.content.Context
import com.elementary.tasks.R
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.formatter.Formatter

class TimerFormatter(
  private val context: Context
) : Formatter<Long>() {
  override fun format(millis: Long): String {
    val timeString = DateTimeManager.generateViewAfterString(millis, divider = "")
    return if (timeString.length == 6) {
      val hours = timeString.substring(0, 2) + context.getString(R.string.h)
      val minutes = " " + timeString.substring(2, 4) + context.getString(R.string.m)
      val seconds = " " + timeString.substring(4, 6) + context.getString(R.string.c)
      "$hours$minutes$seconds"
    } else {
      timeString
    }
  }
}
