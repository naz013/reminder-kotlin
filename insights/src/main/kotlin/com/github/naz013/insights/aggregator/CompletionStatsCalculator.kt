package com.github.naz013.insights.aggregator

import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.domain.history.EventHistoricalRecordType
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

object CompletionStatsCalculator {

  data class FiredCount(
    val eventId: String,
    val count: Int
  )

  data class WeeklyTrendPoint(
    val weekStart: LocalDate,
    val count: Int
  )

  fun firedCounts(records: List<EventHistoricalRecord>): List<FiredCount> =
    reminderRecords(records)
      .groupBy { it.eventId }
      .map { (eventId, recordsForEvent) -> FiredCount(eventId, recordsForEvent.size) }
      .sortedByDescending { it.count }

  fun busiestDayOfWeek(records: List<EventHistoricalRecord>): Map<DayOfWeek, Int> =
    reminderRecords(records)
      .groupingBy { it.date.dayOfWeek }
      .eachCount()

  /** [weeks] Monday-starting buckets ending with the week containing [today]. */
  fun weeklyTrend(
    records: List<EventHistoricalRecord>,
    weeks: Int,
    today: LocalDate
  ): List<WeeklyTrendPoint> {
    val dates = reminderRecords(records).map { it.date }
    val currentWeekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
    return (weeks - 1 downTo 0).map { weeksAgo ->
      val weekStart = currentWeekStart.minusWeeks(weeksAgo.toLong())
      val weekEndExclusive = weekStart.plusDays(7)
      val count = dates.count { !it.isBefore(weekStart) && it.isBefore(weekEndExclusive) }
      WeeklyTrendPoint(weekStart, count)
    }
  }

  private fun reminderRecords(records: List<EventHistoricalRecord>): List<EventHistoricalRecord> =
    records.filter { it.type == EventHistoricalRecordType.Reminder }
}
