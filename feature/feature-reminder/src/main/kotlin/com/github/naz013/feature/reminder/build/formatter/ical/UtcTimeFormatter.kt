package com.github.naz013.feature.reminder.build.formatter.ical

import android.content.Context
import com.github.naz013.ui.common.R
import com.github.naz013.ui.notification.settings.Formatter
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.icalendar.UtcDateTime

class UtcTimeFormatter(
  private val dateTimeManager: DateTimeManager,
  private val context: Context,
) : Formatter<UtcDateTime>() {
  override fun format(dateTime: UtcDateTime): String =
    dateTime.dateTime?.toLocalTime()?.let {
      dateTimeManager.getTime(it)
    } ?: context.getString(R.string.builder_not_selected)
}
