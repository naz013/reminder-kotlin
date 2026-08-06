package com.github.naz013.insights.aggregator

import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.domain.history.EventHistoricalRecordType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class CompletionStatsCalculatorTest {

  // A Sunday, so the Monday-starting week containing it runs 2026-07-27..2026-08-02.
  private val today = LocalDate.of(2026, 8, 2)

  private fun record(
    eventId: String,
    date: LocalDate,
    type: EventHistoricalRecordType = EventHistoricalRecordType.Reminder
  ) = EventHistoricalRecord(
    id = "$eventId-$date-${date.hashCode()}",
    eventId = eventId,
    date = date,
    time = LocalTime.NOON,
    type = type
  )

  @Test
  fun `firedCounts tallies records per eventId and ignores other types`() {
    val records = listOf(
      record("r1", today),
      record("r1", today.minusDays(1)),
      record("r2", today),
      record("b1", today, type = EventHistoricalRecordType.Birthday),
    )

    val result = CompletionStatsCalculator.firedCounts(records).associateBy { it.eventId }

    assertEquals(2, result.getValue("r1").count)
    assertEquals(1, result.getValue("r2").count)
    assertEquals(null, result["b1"])
  }

  @Test
  fun `firedCounts is sorted with the most-fired reminder first`() {
    val records = listOf(
      record("r1", today),
      record("r2", today),
      record("r2", today.minusDays(1)),
      record("r2", today.minusDays(2)),
    )

    val result = CompletionStatsCalculator.firedCounts(records)

    assertEquals("r2", result.first().eventId)
  }

  @Test
  fun `busiestDayOfWeek counts records by day of week across all reminders`() {
    val sunday = today
    val saturday = today.minusDays(1)
    val records = listOf(
      record("r1", sunday),
      record("r2", sunday),
      record("r1", saturday),
    )

    val result = CompletionStatsCalculator.busiestDayOfWeek(records)

    assertEquals(2, result.getValue(DayOfWeek.SUNDAY))
    assertEquals(1, result.getValue(DayOfWeek.SATURDAY))
  }

  @Test
  fun `weeklyTrend buckets records into Monday-starting weeks ending with today's week`() {
    val records = listOf(
      record("r1", today),
      record("r1", today.minusWeeks(1)),
      record("r1", today.minusWeeks(1).minusDays(1)),
    )

    val result = CompletionStatsCalculator.weeklyTrend(records, weeks = 2, today = today)

    assertEquals(2, result.size)
    assertEquals(LocalDate.of(2026, 7, 20), result[0].weekStart)
    assertEquals(2, result[0].count)
    assertEquals(LocalDate.of(2026, 7, 27), result[1].weekStart)
    assertEquals(1, result[1].count)
  }

  @Test
  fun `weeklyTrend returns zero counts for weeks with no records`() {
    val result = CompletionStatsCalculator.weeklyTrend(emptyList(), weeks = 3, today = today)

    assertEquals(3, result.size)
    assertEquals(0, result.sumOf { it.count })
  }
}
