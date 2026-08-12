package com.github.naz013.logic.reminder.smartlist

import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

/**
 * TODAY/OVERDUE/THIS_WEEK only match reminders that are still active (a paused reminder won't
 * fire, so it has no meaningful due-date bucket); NO_GROUP intentionally ignores active state
 * since it's a categorization filter, not a scheduling one.
 */
object ReminderSmartListPredicate {

  fun matches(
    filter: SmartListFilter,
    reminder: ReminderV2,
    now: LocalDateTime
  ): Boolean {
    return when (filter) {
      SmartListFilter.TODAY -> reminder.isActive && isToday(reminder, now)
      SmartListFilter.OVERDUE -> reminder.isActive && isOverdue(reminder, now)
      SmartListFilter.THIS_WEEK -> reminder.isActive && isThisWeek(reminder, now)
      SmartListFilter.NO_GROUP -> reminder.groupId == null
    }
  }

  private fun isToday(reminder: ReminderV2, now: LocalDateTime): Boolean {
    val eventDateTime = reminder.schedule.eventDateTime ?: return false
    return eventDateTime.toLocalDate() == now.toLocalDate()
  }

  private fun isOverdue(reminder: ReminderV2, now: LocalDateTime): Boolean {
    val eventDateTime = reminder.schedule.eventDateTime ?: return false
    return eventDateTime.isBefore(now)
  }

  private fun isThisWeek(reminder: ReminderV2, now: LocalDateTime): Boolean {
    val eventDateTime = reminder.schedule.eventDateTime ?: return false
    return !eventDateTime.isBefore(now) && eventDateTime.isBefore(now.plusDays(7))
  }
}
