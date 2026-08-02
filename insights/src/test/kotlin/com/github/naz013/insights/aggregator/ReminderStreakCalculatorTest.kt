package com.github.naz013.insights.aggregator

import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.domain.history.EventHistoricalRecordType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class ReminderStreakCalculatorTest {

  private val today = LocalDate.of(2026, 8, 2)

  private fun record(
    eventId: String,
    date: LocalDate,
    type: EventHistoricalRecordType = EventHistoricalRecordType.Reminder
  ) = EventHistoricalRecord(
    id = "$eventId-$date",
    eventId = eventId,
    date = date,
    time = LocalTime.NOON,
    type = type
  )

  @Test
  fun `ignores records that are not of type Reminder`() {
    val records = listOf(record("b1", today, type = EventHistoricalRecordType.Birthday))

    val result = ReminderStreakCalculator.calculate(records, today)

    assertTrue(result.isEmpty())
  }

  @Test
  fun `current streak counts consecutive days ending today`() {
    val records = listOf(
      record("r1", today.minusDays(2)),
      record("r1", today.minusDays(1)),
      record("r1", today),
    )

    val result = ReminderStreakCalculator.calculate(records, today)

    assertEquals(3, result.single().currentStreakDays)
  }

  @Test
  fun `current streak still counts when today has not fired yet`() {
    val records = listOf(
      record("r1", today.minusDays(2)),
      record("r1", today.minusDays(1)),
    )

    val result = ReminderStreakCalculator.calculate(records, today)

    assertEquals(2, result.single().currentStreakDays)
  }

  @Test
  fun `current streak is broken by a gap of more than one day`() {
    val records = listOf(
      record("r1", today.minusDays(5)),
      record("r1", today.minusDays(1)),
      record("r1", today),
    )

    val result = ReminderStreakCalculator.calculate(records, today)

    assertEquals(2, result.single().currentStreakDays)
  }

  @Test
  fun `current streak is zero when the last fire was more than a day ago`() {
    val records = listOf(record("r1", today.minusDays(3)))

    val result = ReminderStreakCalculator.calculate(records, today)

    assertEquals(0, result.single().currentStreakDays)
  }

  @Test
  fun `multiple records on the same day only count once toward the streak`() {
    val records = listOf(
      record("r1", today),
      record("r1", today),
      record("r1", today.minusDays(1)),
    )

    val result = ReminderStreakCalculator.calculate(records, today)

    assertEquals(2, result.single().currentStreakDays)
  }

  @Test
  fun `longest streak can exceed the current streak`() {
    val records = listOf(
      record("r1", today.minusDays(10)),
      record("r1", today.minusDays(9)),
      record("r1", today.minusDays(8)),
      record("r1", today.minusDays(7)),
      record("r1", today),
    )

    val result = ReminderStreakCalculator.calculate(records, today)

    assertEquals(4, result.single().longestStreakDays)
    assertEquals(1, result.single().currentStreakDays)
  }

  @Test
  fun `lastFiredDate is the most recent distinct date`() {
    val records = listOf(
      record("r1", today.minusDays(5)),
      record("r1", today.minusDays(1)),
    )

    val result = ReminderStreakCalculator.calculate(records, today)

    assertEquals(today.minusDays(1), result.single().lastFiredDate)
  }

  @Test
  fun `computes streaks independently per eventId`() {
    val records = listOf(
      record("r1", today),
      record("r2", today.minusDays(10)),
    )

    val result = ReminderStreakCalculator.calculate(records, today).associateBy { it.eventId }

    assertEquals(1, result.getValue("r1").currentStreakDays)
    assertEquals(0, result.getValue("r2").currentStreakDays)
  }
}
