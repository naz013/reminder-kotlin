package com.github.naz013.usecase.reminders.smartlist

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderSmartListPredicateTest {

  private val now = LocalDateTime.of(2026, 8, 2, 12, 0)

  private fun reminder(
    eventDateTime: LocalDateTime? = now,
    groupId: String? = "group-1",
    isActive: Boolean = true
  ) = ReminderV2(
    groupId = groupId,
    schedule = ReminderSchedule(startDateTime = now, eventDateTime = eventDateTime),
    isActive = isActive
  )

  @Test
  fun `today matches a reminder due later the same day`() {
    val result = ReminderSmartListPredicate.matches(
      SmartListFilter.TODAY,
      reminder(eventDateTime = now.plusHours(2)),
      now
    )
    assertTrue(result)
  }

  @Test
  fun `today does not match a reminder due tomorrow`() {
    val result = ReminderSmartListPredicate.matches(
      SmartListFilter.TODAY,
      reminder(eventDateTime = now.plusDays(1)),
      now
    )
    assertFalse(result)
  }

  @Test
  fun `today does not match a paused reminder due today`() {
    val result = ReminderSmartListPredicate.matches(
      SmartListFilter.TODAY,
      reminder(eventDateTime = now, isActive = false),
      now
    )
    assertFalse(result)
  }

  @Test
  fun `overdue matches a reminder due in the past`() {
    val result = ReminderSmartListPredicate.matches(
      SmartListFilter.OVERDUE,
      reminder(eventDateTime = now.minusMinutes(1)),
      now
    )
    assertTrue(result)
  }

  @Test
  fun `overdue does not match a reminder due right now`() {
    val result = ReminderSmartListPredicate.matches(
      SmartListFilter.OVERDUE,
      reminder(eventDateTime = now),
      now
    )
    assertFalse(result)
  }

  @Test
  fun `this week matches a reminder due in six days`() {
    val result = ReminderSmartListPredicate.matches(
      SmartListFilter.THIS_WEEK,
      reminder(eventDateTime = now.plusDays(6)),
      now
    )
    assertTrue(result)
  }

  @Test
  fun `this week does not match a reminder due in eight days`() {
    val result = ReminderSmartListPredicate.matches(
      SmartListFilter.THIS_WEEK,
      reminder(eventDateTime = now.plusDays(8)),
      now
    )
    assertFalse(result)
  }

  @Test
  fun `this week does not match a reminder that is already overdue`() {
    val result = ReminderSmartListPredicate.matches(
      SmartListFilter.THIS_WEEK,
      reminder(eventDateTime = now.minusDays(1)),
      now
    )
    assertFalse(result)
  }

  @Test
  fun `no group matches a reminder without a group regardless of active state`() {
    val result = ReminderSmartListPredicate.matches(
      SmartListFilter.NO_GROUP,
      reminder(groupId = null, isActive = false),
      now
    )
    assertTrue(result)
  }

  @Test
  fun `no group does not match a reminder assigned to a group`() {
    val result = ReminderSmartListPredicate.matches(
      SmartListFilter.NO_GROUP,
      reminder(groupId = "group-1"),
      now
    )
    assertFalse(result)
  }

  @Test
  fun `date based filters do not match a reminder with no event date`() {
    assertFalse(ReminderSmartListPredicate.matches(SmartListFilter.TODAY, reminder(eventDateTime = null), now))
    assertFalse(ReminderSmartListPredicate.matches(SmartListFilter.OVERDUE, reminder(eventDateTime = null), now))
    assertFalse(ReminderSmartListPredicate.matches(SmartListFilter.THIS_WEEK, reminder(eventDateTime = null), now))
  }
}
