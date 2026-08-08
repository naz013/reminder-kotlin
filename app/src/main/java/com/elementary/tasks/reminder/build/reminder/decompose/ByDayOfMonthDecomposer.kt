package com.elementary.tasks.reminder.build.reminder.decompose

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DayOfMonthBuilderItem
import com.elementary.tasks.reminder.build.RepeatIntervalBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2

class ByDayOfMonthDecomposer(
  private val dateTimeManager: DateTimeManager,
  private val biFactory: BiFactory,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    val recurrence = reminder.recurrence as? RecurrenceRule.Monthly ?: return emptyList()
    val dateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) } ?: return emptyList()

    val dayOfMonth =
      biFactory.createWithValue(
        BiType.DAY_OF_MONTH,
        recurrence.dayOfMonth,
        DayOfMonthBuilderItem::class.java,
      )

    val repeatInterval =
      biFactory.createWithValue(
        BiType.REPEAT_INTERVAL,
        recurrence.repeatInterval,
        RepeatIntervalBuilderItem::class.java,
      )

    return listOfNotNull(
      biFactory.createWithValue(BiType.TIME, dateTime.toLocalTime(), TimeBuilderItem::class.java),
      dayOfMonth,
      repeatInterval,
    )
  }
}
