package com.elementary.tasks.reminder.build.formatter.ical

import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.github.naz013.icalendar.FreqType

class ICalFreqFormatter(
  private val paramToTextAdapter: ParamToTextAdapter,
) : Formatter<FreqType>() {
  override fun format(freqType: FreqType): String = paramToTextAdapter.getFreqText(freqType)
}
