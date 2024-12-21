package com.elementary.tasks.reminder.build.logic.builderstate

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.BuilderState
import com.elementary.tasks.reminder.build.EmptyState
import com.elementary.tasks.reminder.build.ReadyState
import com.github.naz013.domain.reminder.BiType

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
