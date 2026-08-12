package com.elementary.tasks.reminder.build.formatter.ical

import android.content.Context
import com.elementary.tasks.R
import com.github.naz013.ui.notification.settings.Formatter
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.icalendar.UtcDateTime

class UtcDateFormatter(
  private val dateTimeManager: DateTimeManager,
  private val context: Context,
) : Formatter<UtcDateTime>() {
  override fun format(dateTime: UtcDateTime): String =
    dateTime.dateTime?.toLocalDate()?.let {
      dateTimeManager.getDate(it)
    } ?: context.getString(R.string.builder_not_selected)
}
