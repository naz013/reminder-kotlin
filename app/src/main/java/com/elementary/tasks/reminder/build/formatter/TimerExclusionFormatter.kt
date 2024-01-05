package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.bi.TimerExclusion

class TimerExclusionFormatter(
  private val context: Context,
  private val dateTimeManager: DateTimeManager
) : Formatter<TimerExclusion>() {
  override fun format(timerExclusion: TimerExclusion): String {
    return if (timerExclusion.hours.isNotEmpty()) {
      context.getString(R.string.hours) + " " + timerExclusion.hours.joinToString(separator = ", ")
    } else if (timerExclusion.from.isNotEmpty() && timerExclusion.to.isNotEmpty()) {
      val fromTime = dateTimeManager.toLocalTime(timerExclusion.from)
      val toTime = dateTimeManager.toLocalTime(timerExclusion.to)
      if (fromTime == null || toTime == null) {
        context.getString(R.string.builder_not_selected)
      } else {
        context.getString(R.string.from) + " " + dateTimeManager.getTime(fromTime) + " - " +
          context.getString(R.string.to) + " " + dateTimeManager.getTime(toTime)
      }
    } else {
      context.getString(R.string.builder_not_selected)
    }
  }
}
