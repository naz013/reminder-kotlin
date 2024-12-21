package com.elementary.tasks.reminder.build.reminder.decompose

import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.reminder.build.BuilderItem

class TypeDecomposer(
  private val byDateDecomposer: ByDateDecomposer,
  private val byTimerDecomposer: ByTimerDecomposer,
  private val byWeekdaysDecomposer: ByWeekdaysDecomposer,
  private val byDayOfMonthDecomposer: ByDayOfMonthDecomposer,
  private val byDayOfYearDecomposer: ByDayOfYearDecomposer,
  private val byLocationDecomposer: ByLocationDecomposer,
  private val iCalDecomposer: ICalDecomposer
) {

  suspend operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val type = UiReminderType(reminder.type)
    return when {
      type.isByDate() -> byDateDecomposer(reminder)
      type.isTimer() -> byTimerDecomposer(reminder)
      type.isByWeekday() -> byWeekdaysDecomposer(reminder)
      type.isMonthly() -> byDayOfMonthDecomposer(reminder)
      type.isYearly() -> byDayOfYearDecomposer(reminder)
      type.isGpsType() -> byLocationDecomposer(reminder)
      type.isRecur() -> iCalDecomposer(reminder)
      else -> emptyList()
    }
  }
}
