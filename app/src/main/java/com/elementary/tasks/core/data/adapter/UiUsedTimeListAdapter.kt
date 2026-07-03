package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.data.ui.UiUsedTimeList
import com.github.naz013.domain.UsedTime

class UiUsedTimeListAdapter {
  fun convert(usedTime: UsedTime): UiUsedTimeList =
    UiUsedTimeList(
      timeString = usedTime.timeString,
      timeMills = usedTime.timeMills,
    )
}
