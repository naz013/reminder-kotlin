package com.github.naz013.digest

import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderV2Repository
import org.threeten.bp.LocalDate

/**
 * Reads today's due reminders + birthdays from already-loaded local Room data and shapes them
 * into a bounded [DigestInput]. Every run reads live - there is no cache of its own.
 */
internal class DigestContentBuilder(
  private val reminderV2Repository: ReminderV2Repository,
  private val birthdayRepository: BirthdayRepository,
) {
  suspend fun buildDaily(today: LocalDate): DigestInput {
    val startOfDay = today.atStartOfDay()
    val endOfDay = today.plusDays(1).atStartOfDay()

    val reminders =
      reminderV2Repository
        .getActiveInRange(removed = false, from = startOfDay, to = endOfDay)
        .sortedBy { it.schedule.startDateTime }

    // Birthday.month is stored 0-indexed (Calendar-style), see CheckBirthdaysTask.
    val birthdays =
      birthdayRepository
        .getByDayMonth(day = today.dayOfMonth, month = today.monthValue - 1)
        .map { it.name }

    return DigestInput(
      reminders = reminders.take(MAX_REMINDER_ITEMS).map { DigestReminderItem(it.summary, it.schedule.startDateTime) },
      overflowCount = (reminders.size - MAX_REMINDER_ITEMS).coerceAtLeast(0),
      birthdays = birthdays,
    )
  }

  companion object {
    private const val MAX_REMINDER_ITEMS = 15
  }
}
