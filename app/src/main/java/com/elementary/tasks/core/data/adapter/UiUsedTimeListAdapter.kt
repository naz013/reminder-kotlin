package com.elementary.tasks.core.data.adapter

import com.elementary.tasks.core.data.models.UsedTime
import com.elementary.tasks.core.data.ui.UiUsedTimeList

class UiUsedTimeListAdapter {

  fun convert(usedTime: UsedTime): UiUsedTimeList {
    return UiUsedTimeList(
      timeString = usedTime.timeString,
      timeMills = usedTime.timeMills
    )
  }
}
