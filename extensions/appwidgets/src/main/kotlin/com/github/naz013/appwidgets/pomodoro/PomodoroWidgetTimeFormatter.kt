package com.github.naz013.appwidgets.pomodoro

import android.content.Context
import android.text.format.DateFormat
import java.util.Date

internal class PomodoroWidgetTimeFormatter(
  private val context: Context
) {

  fun formatClock(timeMillis: Long): String {
    return DateFormat.getTimeFormat(context).format(Date(timeMillis))
  }
}
