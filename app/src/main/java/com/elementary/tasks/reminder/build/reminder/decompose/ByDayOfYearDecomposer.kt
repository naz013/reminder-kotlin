package com.elementary.tasks.reminder.build.reminder.decompose

import com.github.naz013.domain.Reminder
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DayOfYearBuilderItem
import com.elementary.tasks.reminder.build.RepeatIntervalBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import org.threeten.bp.LocalDate

class ByDayOfYearDecomposer(
  private val dateTimeManager: DateTimeManager,
  private val biFactory: BiFactory
) {

  suspend operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val dateTIme = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: return emptyList()

    val dayOfYear = runCatching {
      LocalDate.of(LocalDate.now().year, reminder.monthOfYear + 1, reminder.dayOfMonth)
    }.getOrNull()
      ?.let {
        biFactory.createWithValue(
          BiType.DAY_OF_YEAR,
          it.dayOfYear,
          DayOfYearBuilderItem::class.java
        )
      }

    val repeatInterval = biFactory.createWithValue(
      BiType.REPEAT_INTERVAL,
      reminder.repeatInterval,
      RepeatIntervalBuilderItem::class.java
    )

    return listOfNotNull(
      biFactory.createWithValue(BiType.TIME, dateTIme.toLocalTime(), TimeBuilderItem::class.java),
      dayOfYear,
      repeatInterval
    )
  }
}
