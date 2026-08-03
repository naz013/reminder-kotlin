package com.elementary.tasks.birthdays

import com.github.naz013.datecalc.BirthdayDateCalculatorImpl
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.usecase.reminders.smartlist.SmartListFilter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate

class BirthdaySmartListPredicateTest {

  private val predicate = BirthdaySmartListPredicate(BirthdayDateCalculatorImpl())
  private val today = LocalDate.of(2026, 8, 2)

  private fun birthday(
    day: Int,
    month: Int,
  ) = Birthday(day = day, month = month, syncState = SyncState.Synced)

  @Test
  fun `today matches a birthday on the same month and day`() {
    val result = predicate.matches(SmartListFilter.TODAY, birthday(day = 2, month = 8), today)
    assertTrue(result)
  }

  @Test
  fun `today does not match a birthday later in the month`() {
    val result = predicate.matches(SmartListFilter.TODAY, birthday(day = 10, month = 8), today)
    assertFalse(result)
  }

  @Test
  fun `this week matches a birthday six days out`() {
    val result = predicate.matches(SmartListFilter.THIS_WEEK, birthday(day = 8, month = 8), today)
    assertTrue(result)
  }

  @Test
  fun `this week does not match a birthday eight days out`() {
    val result = predicate.matches(SmartListFilter.THIS_WEEK, birthday(day = 10, month = 8), today)
    assertFalse(result)
  }

  @Test
  fun `this week wraps across a month boundary`() {
    val result = predicate.matches(SmartListFilter.THIS_WEEK, birthday(day = 3, month = 9), LocalDate.of(2026, 8, 30))
    assertTrue(result)
  }

  @Test
  fun `overdue never matches since birthdays always recur into the future`() {
    val result = predicate.matches(SmartListFilter.OVERDUE, birthday(day = 2, month = 8), today)
    assertFalse(result)
  }

  @Test
  fun `no group matches every birthday since they have no group concept`() {
    val result = predicate.matches(SmartListFilter.NO_GROUP, birthday(day = 15, month = 3), today)
    assertTrue(result)
  }
}
