package com.github.naz013.feature.reminder.build.reminder.decompose

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.DaysOfWeekBuilderItem
import com.github.naz013.feature.reminder.build.TimeBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2

class ByWeekdaysDecomposer(
  private val dateTimeManager: DateTimeManager,
  private val biFactory: BiFactory,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    val recurrence = reminder.recurrence as? RecurrenceRule.Weekly ?: return emptyList()
    val dateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) } ?: return emptyList()

    val weekBuilderItem =
      recurrence.weekdays
        .takeIf { it.isNotEmpty() }
        ?.let {
          biFactory.createWithValue(BiType.DAYS_OF_WEEK, it, DaysOfWeekBuilderItem::class.java)
        }

    return listOfNotNull(
      biFactory.createWithValue(BiType.TIME, dateTime.toLocalTime(), TimeBuilderItem::class.java),
      weekBuilderItem,
    )
  }
}
