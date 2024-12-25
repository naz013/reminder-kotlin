package com.elementary.tasks.reminder.build.reminder.decompose

import com.github.naz013.domain.Reminder
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DayOfMonthBuilderItem
import com.elementary.tasks.reminder.build.RepeatIntervalBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory

class ByDayOfMonthDecomposer(
  private val dateTimeManager: DateTimeManager,
  private val biFactory: BiFactory
) {

  suspend operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val dateTIme = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return emptyList()

    val dayOfMonth = biFactory.createWithValue(
      BiType.DAY_OF_MONTH,
      reminder.dayOfMonth,
      DayOfMonthBuilderItem::class.java
    )

    val repeatInterval = biFactory.createWithValue(
      BiType.REPEAT_INTERVAL,
      reminder.repeatInterval,
      RepeatIntervalBuilderItem::class.java
    )

    return listOfNotNull(
      biFactory.createWithValue(BiType.TIME, dateTIme.toLocalTime(), TimeBuilderItem::class.java),
      dayOfMonth,
      repeatInterval
    )
  }
}
