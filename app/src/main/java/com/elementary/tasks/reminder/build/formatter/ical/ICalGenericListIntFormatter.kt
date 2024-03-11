package com.elementary.tasks.reminder.build.formatter.ical

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.formatter.Formatter
import org.threeten.bp.LocalDate

class ICalGenericListIntFormatter(
  private val context: Context
) : Formatter<List<Int>>() {

  private val date = LocalDate.now().withDayOfMonth(15)

  override fun format(list: List<Int>): String {
    return if (list.isEmpty()) {
      context.getString(R.string.builder_not_selected)
    } else {
      list.joinToString(",") { "$it" }
    }
  }
}
