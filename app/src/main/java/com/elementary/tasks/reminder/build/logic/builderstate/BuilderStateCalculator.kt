package com.elementary.tasks.reminder.build.logic.builderstate

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.BuilderState
import com.elementary.tasks.reminder.build.EmptyState
import com.elementary.tasks.reminder.build.ReadyState
import com.elementary.tasks.reminder.build.bi.BiType

class BuilderStateCalculator {

  operator fun invoke(
    type: Int,
    itemsMap: Map<BiType, BuilderItem<*>>
  ): BuilderState {
    return if (type != 0) {
      ReadyState
    } else {
      EmptyState
    }
  }
}
