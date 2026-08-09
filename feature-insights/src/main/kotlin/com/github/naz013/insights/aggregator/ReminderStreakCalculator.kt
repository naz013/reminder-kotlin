package com.github.naz013.insights.aggregator

import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.domain.history.EventHistoricalRecordType
import org.threeten.bp.LocalDate

/**
 * A "streak day" is any day with at least one [EventHistoricalRecord] of type Reminder for a
 * given eventId - this is "times fired", not a true completed/not-completed signal, since
 * reminders have no such concept. See the Insights feature plan for this trade-off.
 */
internal object ReminderStreakCalculator {

  data class Streak(
    val eventId: String,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val lastFiredDate: LocalDate
  )

  fun calculate(records: List<EventHistoricalRecord>, today: LocalDate): List<Streak> {
    return records
      .asSequence()
      .filter { it.type == EventHistoricalRecordType.Reminder }
      .groupBy { it.eventId }
      .map { (eventId, recordsForEvent) -> calculateForReminder(eventId, recordsForEvent, today) }
  }

  private fun calculateForReminder(
    eventId: String,
    records: List<EventHistoricalRecord>,
    today: LocalDate
  ): Streak {
    val distinctDates = records.map { it.date }.distinct().sorted()
    return Streak(
      eventId = eventId,
      currentStreakDays = currentStreak(distinctDates, today),
      longestStreakDays = longestStreak(distinctDates),
      lastFiredDate = distinctDates.last()
    )
  }

  /** Counts backward from today; if the reminder hasn't fired yet today, starts from yesterday
   * instead so an in-progress streak isn't reported as broken before the day is even over. */
  private fun currentStreak(sortedDistinctDates: List<LocalDate>, today: LocalDate): Int {
    val dateSet = sortedDistinctDates.toHashSet()
    var cursor = if (dateSet.contains(today)) today else today.minusDays(1)
    var streak = 0
    while (dateSet.contains(cursor)) {
      streak++
      cursor = cursor.minusDays(1)
    }
    return streak
  }

  private fun longestStreak(sortedDistinctDates: List<LocalDate>): Int {
    if (sortedDistinctDates.isEmpty()) return 0
    var longest = 1
    var current = 1
    for (i in 1 until sortedDistinctDates.size) {
      current = if (sortedDistinctDates[i - 1].plusDays(1) == sortedDistinctDates[i]) current + 1 else 1
      longest = maxOf(longest, current)
    }
    return longest
  }
}
