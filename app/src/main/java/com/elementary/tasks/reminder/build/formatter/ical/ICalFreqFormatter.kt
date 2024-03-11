package com.elementary.tasks.reminder.build.formatter.ical

import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter

class ICalFreqFormatter(
  private val paramToTextAdapter: ParamToTextAdapter
) : Formatter<FreqType>() {

  override fun format(freqType: FreqType): String {
    return paramToTextAdapter.getFreqText(freqType)
  }
}
