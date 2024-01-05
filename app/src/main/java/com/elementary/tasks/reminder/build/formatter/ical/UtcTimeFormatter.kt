package com.elementary.tasks.reminder.build.formatter.ical

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import com.elementary.tasks.reminder.build.formatter.Formatter

class UtcTimeFormatter(
  private val dateTimeManager: DateTimeManager,
  private val context: Context
) : Formatter<UtcDateTime>() {
  override fun format(dateTime: UtcDateTime): String {
    return dateTime.dateTime?.toLocalTime()?.let {
      dateTimeManager.getTime(it)
    } ?: context.getString(R.string.builder_not_selected)
  }
}
