package com.elementary.tasks.reminder.build.reminder.decompose

import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.BiType
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.RepeatTimeBuilderItem
import com.elementary.tasks.reminder.build.TimerBuilderItem
import com.elementary.tasks.reminder.build.bi.TimerExclusion
import com.elementary.tasks.reminder.build.TimerExclusionBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory

class ByTimerDecomposer(
  private val biFactory: BiFactory
) {

  suspend operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val timerExclusion =
      reminder.takeIf { it.hours.isEmpty() || it.from.isNotEmpty() || it.to.isNotEmpty() }
        ?.let { TimerExclusion(it.hours, it.from, it.to) }
        ?.let {
          biFactory.createWithValue(
            BiType.COUNTDOWN_TIMER_EXCLUSION,
            it,
            TimerExclusionBuilderItem::class.java
          )
        }

    val repeatTime = reminder.repeatInterval.takeIf { it > 0 }
      ?.let { biFactory.createWithValue(BiType.REPEAT_TIME, it, RepeatTimeBuilderItem::class.java) }

    return listOfNotNull(
      biFactory.createWithValue(
        BiType.COUNTDOWN_TIMER,
        reminder.after,
        TimerBuilderItem::class.java
      ),
      timerExclusion,
      repeatTime
    )
  }
}
