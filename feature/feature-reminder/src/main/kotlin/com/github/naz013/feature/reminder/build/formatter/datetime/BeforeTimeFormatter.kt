package com.github.naz013.feature.reminder.build.formatter.datetime

import android.content.Context
import com.github.naz013.ui.common.R
import com.github.naz013.ui.notification.settings.Formatter
import com.github.naz013.datecalc.DateTimeManager

class BeforeTimeFormatter(
  private val context: Context,
  private val dateTimeManager: DateTimeManager,
) : Formatter<Long>() {
  override fun format(millis: Long): String {
    val parsedBeforeTime = dateTimeManager.parseBeforeTime(millis)
    return "${parsedBeforeTime.value} " +
      when (parsedBeforeTime.type) {
        DateTimeManager.MultiplierType.MONTH -> context.getString(R.string.months)
        DateTimeManager.MultiplierType.WEEK -> context.getString(R.string.weeks)
        DateTimeManager.MultiplierType.DAY -> context.getString(R.string.days)
        DateTimeManager.MultiplierType.HOUR -> context.getString(R.string.hours)
        DateTimeManager.MultiplierType.MINUTE -> context.getString(R.string._minutes)
        DateTimeManager.MultiplierType.SECOND -> context.getString(R.string.seconds)
      }
  }
}
