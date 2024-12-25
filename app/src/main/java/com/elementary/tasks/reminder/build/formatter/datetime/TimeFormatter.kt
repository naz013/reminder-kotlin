package com.elementary.tasks.reminder.build.formatter.datetime

import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.formatter.Formatter
import org.threeten.bp.LocalTime

class TimeFormatter(
  private val dateTimeManager: DateTimeManager
) : Formatter<LocalTime>() {
  override fun format(time: LocalTime): String {
    return dateTimeManager.getTime(time)
  }
}
