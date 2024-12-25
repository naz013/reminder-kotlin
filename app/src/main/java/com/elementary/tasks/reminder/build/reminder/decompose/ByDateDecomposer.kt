package com.elementary.tasks.reminder.build.reminder.decompose

import com.github.naz013.domain.Reminder
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.RepeatTimeBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory

class ByDateDecomposer(
  private val dateTimeManager: DateTimeManager,
  private val biFactory: BiFactory
) {

  suspend operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val dateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return emptyList()

    val repeatTime = reminder.repeatInterval.takeIf { it > 0 }
      ?.let { biFactory.createWithValue(BiType.REPEAT_TIME, it, RepeatTimeBuilderItem::class.java) }

    return listOfNotNull(
      biFactory.createWithValue(BiType.DATE, dateTime.toLocalDate(), DateBuilderItem::class.java),
      biFactory.createWithValue(BiType.TIME, dateTime.toLocalTime(), TimeBuilderItem::class.java),
      repeatTime
    )
  }
}
