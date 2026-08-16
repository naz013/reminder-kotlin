package com.github.naz013.feature.reminder.build.reminder.decompose

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.DayOfYearBuilderItem
import com.github.naz013.feature.reminder.build.RepeatIntervalBuilderItem
import com.github.naz013.feature.reminder.build.TimeBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDate

class ByDayOfYearDecomposer(
  private val dateTimeManager: DateTimeManager,
  private val biFactory: BiFactory,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    val recurrence = reminder.recurrence as? RecurrenceRule.Yearly ?: return emptyList()
    val dateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) } ?: return emptyList()

    val dayOfYear =
      runCatching {
        LocalDate.of(LocalDate.now().year, recurrence.monthOfYear + 1, recurrence.dayOfMonth)
      }.getOrNull()
        ?.let {
          biFactory.createWithValue(
            BiType.DAY_OF_YEAR,
            it.dayOfYear,
            DayOfYearBuilderItem::class.java,
          )
        }

    val repeatInterval =
      biFactory.createWithValue(
        BiType.REPEAT_INTERVAL,
        recurrence.repeatInterval,
        RepeatIntervalBuilderItem::class.java,
      )

    return listOfNotNull(
      biFactory.createWithValue(BiType.TIME, dateTime.toLocalTime(), TimeBuilderItem::class.java),
      dayOfYear,
      repeatInterval,
    )
  }
}
