package com.elementary.tasks.reminder.build.formatter.ical

import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.github.naz013.ui.notification.settings.Formatter
import com.github.naz013.icalendar.FreqType

class ICalFreqFormatter(
  private val paramToTextAdapter: ParamToTextAdapter,
) : Formatter<FreqType>() {
  override fun format(freqType: FreqType): String = paramToTextAdapter.getFreqText(freqType)
}
