package com.elementary.tasks.reminder.build.formatter.datetime

import com.github.naz013.ui.notification.settings.Formatter
import com.github.naz013.datecalc.DateTimeManager
import org.threeten.bp.LocalTime

class TimeFormatter(
  private val dateTimeManager: DateTimeManager,
) : Formatter<LocalTime>() {
  override fun format(time: LocalTime): String = dateTimeManager.getTime(time)
}
