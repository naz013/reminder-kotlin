package com.elementary.tasks.reminder.lists.filter.daterange

import com.elementary.tasks.core.filter.FilterInstance
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDate

class ReminderDateRangeFilterInstance(
  private val dateTimeManager: DateTimeManager,
  private val startDate: LocalDate?,
  private val endDate: LocalDate?
) : FilterInstance<Reminder> {
  override fun filter(t: Reminder): Boolean {
    if ((startDate == null && endDate == null) || endDate?.isBefore(startDate) == true) return true
    val reminderDate = dateTimeManager.fromGmtToLocal(t.eventTime)?.toLocalDate() ?: return true

    return reminderDate.isAfterStartDate() && reminderDate.isBeforeEndDate()
  }

  private fun LocalDate.isAfterStartDate(): Boolean {
    return startDate == null || !this.isBefore(startDate)
  }

  private fun LocalDate.isBeforeEndDate(): Boolean {
    return endDate == null || !this.isAfter(endDate)
  }
}
