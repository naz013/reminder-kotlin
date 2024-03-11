package com.elementary.tasks.reminder.build.reminder.decompose

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.bi.BiType
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DaysOfWeekBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory

class ByWeekdaysDecomposer(
  private val dateTimeManager: DateTimeManager,
  private val biFactory: BiFactory
) {

  operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val dateTIme = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return emptyList()

    val weekBuilderItem = reminder.weekdays.takeIf { it.isNotEmpty() }
      ?.let {
        biFactory.createWithValue(BiType.DAYS_OF_WEEK, it, DaysOfWeekBuilderItem::class.java)
      }

    return listOfNotNull(
      biFactory.createWithValue(BiType.TIME, dateTIme.toLocalTime(), TimeBuilderItem::class.java),
      weekBuilderItem
    )
  }
}
