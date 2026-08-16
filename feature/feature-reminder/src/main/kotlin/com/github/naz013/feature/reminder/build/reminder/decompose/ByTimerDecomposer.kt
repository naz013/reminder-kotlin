package com.github.naz013.feature.reminder.build.reminder.decompose

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.RepeatTimeBuilderItem
import com.github.naz013.feature.reminder.build.TimerBuilderItem
import com.github.naz013.feature.reminder.build.TimerExclusionBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.feature.reminder.build.bi.TimerExclusion
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2

class ByTimerDecomposer(
  private val biFactory: BiFactory,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    val recurrence = reminder.recurrence as? RecurrenceRule.Countdown ?: return emptyList()
    val notification = reminder.notification

    val timerExclusion =
      reminder
        .takeIf {
          notification.activeHours.isNullOrEmpty() ||
            !notification.quietHoursFrom.isNullOrEmpty() ||
            !notification.quietHoursTo.isNullOrEmpty()
        }?.let {
          TimerExclusion(
            notification.activeHours.orEmpty(),
            notification.quietHoursFrom.orEmpty(),
            notification.quietHoursTo.orEmpty(),
          )
        }?.let {
          biFactory.createWithValue(
            BiType.COUNTDOWN_TIMER_EXCLUSION,
            it,
            TimerExclusionBuilderItem::class.java,
          )
        }

    val repeatTime =
      recurrence.repeatInterval
        .takeIf { it > 0 }
        ?.let { biFactory.createWithValue(BiType.REPEAT_TIME, it, RepeatTimeBuilderItem::class.java) }

    return listOfNotNull(
      biFactory.createWithValue(
        BiType.COUNTDOWN_TIMER,
        recurrence.after,
        TimerBuilderItem::class.java,
      ),
      timerExclusion,
      repeatTime,
    )
  }
}
