package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.bi.CalendarDuration

class CalendarDurationFormatter(
  private val context: Context,
  private val dateTimeManager: DateTimeManager
) : Formatter<CalendarDuration>() {
  override fun format(calendarDuration: CalendarDuration): String {
    return if (calendarDuration.allDay) {
      context.getString(R.string.reminder_title_all_day)
    } else {
      if (calendarDuration.millis > 0L) {
        val parsedTime = dateTimeManager.parseRepeatTime(calendarDuration.millis)
        "${parsedTime.value} " + when (parsedTime.type) {
          DateTimeManager.MultiplierType.MONTH -> context.getString(R.string.months)
          DateTimeManager.MultiplierType.WEEK -> context.getString(R.string.weeks)
          DateTimeManager.MultiplierType.DAY -> context.getString(R.string.days)
          DateTimeManager.MultiplierType.HOUR -> context.getString(R.string.hours)
          DateTimeManager.MultiplierType.MINUTE -> context.getString(R.string._minutes)
          DateTimeManager.MultiplierType.SECOND -> context.getString(R.string.seconds)
        }
      } else {
        context.getString(R.string.not_selected)
      }
    }
  }
}
