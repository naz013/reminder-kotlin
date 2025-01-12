package com.elementary.tasks.reminder.build.formatter.ical

import com.github.naz013.icalendar.FreqType
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter

class ICalFreqFormatter(
  private val paramToTextAdapter: ParamToTextAdapter
) : Formatter<FreqType>() {

  override fun format(freqType: FreqType): String {
    return paramToTextAdapter.getFreqText(freqType)
  }
}
