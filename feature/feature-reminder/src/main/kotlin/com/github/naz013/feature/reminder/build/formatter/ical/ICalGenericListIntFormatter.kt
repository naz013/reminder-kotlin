package com.github.naz013.feature.reminder.build.formatter.ical

import android.content.Context
import com.github.naz013.ui.common.R
import com.github.naz013.ui.notification.settings.Formatter
import org.threeten.bp.LocalDate

class ICalGenericListIntFormatter(
  private val context: Context,
) : Formatter<List<Int>>() {
  private val date = LocalDate.now().withDayOfMonth(15)

  override fun format(list: List<Int>): String =
    if (list.isEmpty()) {
      context.getString(R.string.builder_not_selected)
    } else {
      list.joinToString(",") { "$it" }
    }
}
