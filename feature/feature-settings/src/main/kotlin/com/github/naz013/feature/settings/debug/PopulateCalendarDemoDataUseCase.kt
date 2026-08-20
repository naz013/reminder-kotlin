package com.github.naz013.feature.settings.debug

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID
import kotlin.random.Random

/**
 * Debug-only generator that populates the Month/Day/3-day/7-day calendar views with demo data, at
 * either [Scale.NORMAL] (a realistic amount of everyday data) or [Scale.MASSIVE] (a stress-test
 * load, to see how those views hold up with huge amounts of data).
 *
 * Reminders and birthdays are saved through the normal repositories, but their
 * [EventOccurrence] rows are computed directly here (via [expandRecurrenceDates]) rather than
 * through the real `CalculateReminderOccurrencesUseCase`/`CalculateBirthdayOccurrencesUseCase`
 * (which each resolve one item at a time via a WorkManager job) - enqueueing thousands of
 * individual jobs for [Scale.MASSIVE] would be impractically slow for a debug button.
 */
internal class PopulateCalendarDemoDataUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val birthdayRepository: BirthdayRepository,
  private val eventOccurrenceRepository: EventOccurrenceRepository,
  private val groupV2Repository: GroupV2Repository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(scale: Scale): Result {
    val today = LocalDate.now()
    val windowStart = today.minusDays(WINDOW_DAYS_BACK)
    val windowEnd = today.plusDays(WINDOW_DAYS_FORWARD)
    val groupId = groupV2Repository.defaultGroup()?.uuId
    val random = Random(System.nanoTime())

    val reminders = buildReminders(scale.reminderMix, today, windowStart, windowEnd, groupId, random)
    reminders.forEach { reminderV2Repository.save(it.reminder) }

    val birthdays = buildBirthdays(scale.birthdayCount, windowStart, windowEnd, random)
    birthdays.forEach { birthdayRepository.save(it) }

    val occurrences = mutableListOf<EventOccurrence>()
    reminders.forEach { generated ->
      expandRecurrenceDates(generated.reminder.recurrence, generated.anchorDate, windowStart, windowEnd).forEach { date ->
        occurrences +=
          EventOccurrence(
            id = UUID.randomUUID().toString(),
            eventId = generated.reminder.uuId,
            date = date,
            time = generated.anchorTime,
            type = OccurrenceType.Reminder,
          )
      }
    }
    birthdays.forEach { birthday ->
      (windowStart.year..windowEnd.year)
        .mapNotNull { year -> runCatching { LocalDate.of(year, birthday.month + 1, birthday.day) }.getOrNull() }
        .filter { !it.isBefore(windowStart) && !it.isAfter(windowEnd) }
        .forEach { date ->
          occurrences +=
            EventOccurrence(
              id = UUID.randomUUID().toString(),
              eventId = birthday.uuId,
              date = date,
              time = DEFAULT_BIRTHDAY_TIME,
              type = OccurrenceType.Birthday,
            )
        }
    }
    eventOccurrenceRepository.saveAll(occurrences)

    return Result(reminderCount = reminders.size, birthdayCount = birthdays.size, occurrenceCount = occurrences.size)
  }

  private fun buildReminders(
    mix: ReminderMix,
    today: LocalDate,
    windowStart: LocalDate,
    windowEnd: LocalDate,
    groupId: String?,
    random: Random,
  ): List<GeneratedReminder> {
    val startDateTime = dateTimeManager.getCurrentDateTime()
    val windowSpanDays = daysBetween(windowStart, windowEnd)
    val nearStart = maxOf(windowStart, today.minusDays(NEAR_WINDOW_DAYS_BACK))
    val nearEnd = minOf(windowEnd, today.plusDays(NEAR_WINDOW_DAYS_FORWARD))
    val nearSpanDays = daysBetween(nearStart, nearEnd)

    // Most anchors land in a ~3-week window around today so the demo data is immediately visible
    // without paging away from it; the rest scatter across the full window for paging variety
    // (and, at Scale.MASSIVE, to still have plenty of far-out data to stress-test with).
    fun randomDate(): LocalDate =
      if (random.nextInt(100) < NEAR_WINDOW_BIAS_PERCENT) {
        nearStart.plusDays(random.nextInt(nearSpanDays + 1).toLong())
      } else {
        windowStart.plusDays(random.nextInt(windowSpanDays + 1).toLong())
      }
    fun randomTime(): LocalTime = LocalTime.of(random.nextInt(24), listOf(0, 15, 30, 45).random(random))
    fun schedule(dateTime: LocalDateTime) =
      ReminderSchedule(startDateTime = startDateTime, eventDateTime = dateTimeManager.localToUtc(dateTime))

    fun buildOne(
      label: String,
      index: Int,
      recurrence: RecurrenceRule,
    ): GeneratedReminder {
      val date = randomDate()
      val time = randomTime()
      val reminder =
        ReminderV2(
          summary = "$label demo reminder #${index + 1}",
          schedule = schedule(LocalDateTime.of(date, time)),
          recurrence = recurrence,
          groupId = groupId,
        )
      return GeneratedReminder(reminder = reminder, anchorDate = date, anchorTime = time)
    }

    val generated = mutableListOf<GeneratedReminder>()
    repeat(mix.oneTime) { generated += buildOne("One-time", it, RecurrenceRule.Once) }
    repeat(mix.daily) { generated += buildOne("Daily", it, RecurrenceRule.Daily(repeatInterval = 1)) }
    repeat(mix.weekly) { i ->
      val weekday = random.nextInt(7)
      generated += buildOne("Weekly", i, RecurrenceRule.Weekly(weekdays = List(7) { if (it == weekday) 1 else 0 }))
    }
    repeat(mix.monthly) { i ->
      generated += buildOne("Monthly", i, RecurrenceRule.Monthly(dayOfMonth = random.nextInt(1, 29)))
    }
    repeat(mix.yearly) { i ->
      val month = random.nextInt(12)
      val day = random.nextInt(1, 29)
      generated += buildOne("Yearly", i, RecurrenceRule.Yearly(dayOfMonth = day, monthOfYear = month))
    }
    return generated
  }

  private fun buildBirthdays(
    count: Int,
    windowStart: LocalDate,
    windowEnd: LocalDate,
    random: Random,
  ): List<Birthday> {
    val windowSpanDays = daysBetween(windowStart, windowEnd)
    return (0 until count).map { i ->
      // First half land inside the demo window so they're immediately visible; the rest scatter
      // across the whole year for variety when paging further out in Month view.
      val monthDay =
        if (i < count / 2) {
          windowStart.plusDays(random.nextInt(windowSpanDays + 1).toLong())
        } else {
          LocalDate.of(2000, 1, 1).plusDays(random.nextLong(365))
        }
      val birthYear = 1950 + random.nextInt(60)
      val daysInMonth = LocalDate.of(birthYear, monthDay.monthValue, 1).lengthOfMonth()
      val date = LocalDate.of(birthYear, monthDay.monthValue, minOf(monthDay.dayOfMonth, daysInMonth))
      Birthday(
        name = "Demo birthday #${i + 1}",
        date = dateTimeManager.formatBirthdayDate(date),
        day = date.dayOfMonth,
        month = date.monthValue - 1,
        dayMonth = "${date.dayOfMonth}|${date.monthValue - 1}",
        syncState = SyncState.Synced,
      )
    }
  }

  private fun daysBetween(
    start: LocalDate,
    end: LocalDate,
  ): Int = ChronoUnit.DAYS.between(start, end).toInt()

  data class Result(
    val reminderCount: Int,
    val birthdayCount: Int,
    val occurrenceCount: Int,
  )

  private data class GeneratedReminder(
    val reminder: ReminderV2,
    val anchorDate: LocalDate,
    val anchorTime: LocalTime,
  )

  data class ReminderMix(
    val oneTime: Int,
    val daily: Int,
    val weekly: Int,
    val monthly: Int,
    val yearly: Int,
  ) {
    val total: Int get() = oneTime + daily + weekly + monthly + yearly
  }

  enum class Scale(
    val reminderMix: ReminderMix,
    val birthdayCount: Int,
  ) {
    NORMAL(reminderMix = ReminderMix(oneTime = 8, daily = 6, weekly = 6, monthly = 6, yearly = 6), birthdayCount = 6),
    MASSIVE(reminderMix = ReminderMix(oneTime = 2000, daily = 60, weekly = 60, monthly = 60, yearly = 60), birthdayCount = 500),
    ;

    val reminderCount: Int get() = reminderMix.total
  }

  companion object {
    private const val WINDOW_DAYS_BACK = 30L
    private const val WINDOW_DAYS_FORWARD = 90L
    private const val NEAR_WINDOW_DAYS_BACK = 7L
    private const val NEAR_WINDOW_DAYS_FORWARD = 14L
    private const val NEAR_WINDOW_BIAS_PERCENT = 65
    private val DEFAULT_BIRTHDAY_TIME = LocalTime.of(9, 0)
  }
}
