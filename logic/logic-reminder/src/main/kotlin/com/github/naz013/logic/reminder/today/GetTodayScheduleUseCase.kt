package com.github.naz013.logic.reminder.today

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.CalendarEventRepository
import com.github.naz013.repository.GoogleCalendarEventRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderV2Repository
import org.dmfs.rfc5545.DateTime
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit
import java.util.TimeZone

/**
 * Builds the unified schedule for the day of [now]: active reminders due that day plus overdue ones
 * from earlier days, birthdays, device calendar events (recurring ones expanded) and Google tasks
 * due that day, with mirrored calendar events / tasks removed in favour of the reminder.
 */
class GetTodayScheduleUseCase(
  private val dateTimeManager: DateTimeManager,
  private val reminderV2Repository: ReminderV2Repository,
  private val birthdayRepository: BirthdayRepository,
  private val googleCalendarEventRepository: GoogleCalendarEventRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val calendarEventRepository: CalendarEventRepository,
) {
  suspend operator fun invoke(now: LocalDateTime): List<TodayItem> {
    val dayStart = now.toLocalDate().atStartOfDay()
    val dayEnd = dayStart.plusDays(1)

    val reminders = loadReminders(dayEnd, dayStart)
    val calendarEvents = loadCalendarEvents(dayStart, dayEnd)
    val tasks = loadTasks(dayStart, dayEnd)
    val deduped = dedupToday(
      reminders = reminders,
      calendarEvents = calendarEvents,
      tasks = tasks,
      calendarLinks = calendarEventRepository.getAll(),
    )
    val birthdays = loadBirthdays(now)

    return (deduped.reminders + birthdays + deduped.calendarEvents + deduped.tasks)
      .sortedWith(compareBy<TodayItem> { it.bucket }.thenBy { it.dateTime })
  }

  private suspend fun loadReminders(
    dayEnd: LocalDateTime,
    dayStart: LocalDateTime,
  ): List<TodayItem.Reminder> = reminderV2Repository
    .getAll(active = true, removed = false)
    .mapNotNull { toReminderItem(it, dayStart, dayEnd) }

  private fun toReminderItem(
    reminder: ReminderV2,
    dayStart: LocalDateTime,
    dayEnd: LocalDateTime,
  ): TodayItem.Reminder? {
    val dateTime = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) } ?: return null
    if (!dateTime.isBefore(dayEnd)) return null
    return TodayItem.Reminder(
      reminder = reminder,
      dateTime = dateTime,
      isOverdue = dateTime.isBefore(dayStart),
    )
  }

  private suspend fun loadBirthdays(now: LocalDateTime): List<TodayItem.Birthday> {
    val time = dateTimeManager.getBirthdayLocalTime()
    val bucket = if (time != null) TodayBucket.Timed else TodayBucket.AllDay
    val dateTime = now.toLocalDate().atTime(time ?: LocalTime.MIDNIGHT)
    return birthdayRepository
      .getAll(dateTimeManager.getBirthdayDayMonth(now))
      .filterNot { it.showedYear == now.year }
      .map { TodayItem.Birthday(it, dateTime, bucket) }
  }

  private suspend fun loadTasks(
    dayStart: LocalDateTime,
    dayEnd: LocalDateTime,
  ): List<TodayItem.Task> = googleTaskRepository
    .getAll()
    .filter { it.dueDate > 0 && it.del == 0 && !it.isCompleted() }
    .mapNotNull { task ->
      val due = dateTimeManager.fromMillis(task.dueDate)
      if (due.isBefore(dayStart) || !due.isBefore(dayEnd)) return@mapNotNull null
      val bucket = if (due.toLocalTime() == LocalTime.MIDNIGHT) TodayBucket.Anytime else TodayBucket.Timed
      TodayItem.Task(task, due, bucket)
    }

  private suspend fun loadCalendarEvents(
    dayStart: LocalDateTime,
    dayEnd: LocalDateTime,
  ): List<TodayItem.CalendarEvent> = googleCalendarEventRepository
    .getVisible()
    .flatMap { event -> occurrencesOnDay(event, dayStart, dayEnd) }

  private fun occurrencesOnDay(
    event: GoogleCalendarEvent,
    dayStart: LocalDateTime,
    dayEnd: LocalDateTime,
  ): List<TodayItem.CalendarEvent> {
    val start = dateTimeManager.utcToLocal(event.startDateTime)
    val end = event.endDateTime?.let { dateTimeManager.utcToLocal(it) }
    val duration = end?.let { ChronoUnit.MILLIS.between(start, it) }?.coerceAtLeast(0L) ?: 0L
    val starts = if (event.rrule.isBlank()) {
      listOf(start)
    } else {
      expandRecurrence(event.rrule, start, dayStart, duration, dayEnd)
    }
    return starts
      .filter { it.isBefore(dayEnd) && (!it.isBefore(dayStart) || it.plus(duration, ChronoUnit.MILLIS).isAfter(dayStart)) }
      .map { occurrenceStart ->
        val occurrenceEnd = if (end != null) occurrenceStart.plus(duration, ChronoUnit.MILLIS) else null
        TodayItem.CalendarEvent(event, occurrenceStart, occurrenceEnd)
      }
  }

  private fun expandRecurrence(
    rrule: String,
    start: LocalDateTime,
    dayStart: LocalDateTime,
    durationMillis: Long,
    dayEnd: LocalDateTime,
  ): List<LocalDateTime> {
    val startMillis = dateTimeManager.toMillis(start)
    val windowStart = dateTimeManager.toMillis(dayStart) - durationMillis
    val windowEnd = dateTimeManager.toMillis(dayEnd)
    return try {
      val iterator = RecurrenceRule(rrule).iterator(DateTime(TimeZone.getDefault(), startMillis))
      if (windowStart > startMillis) {
        iterator.fastForward(DateTime(TimeZone.getDefault(), windowStart))
      }
      val result = mutableListOf<LocalDateTime>()
      var guard = 0
      while (iterator.hasNext() && guard++ < MAX_ITERATIONS) {
        val next = iterator.nextMillis()
        if (next >= windowEnd) break
        result.add(dateTimeManager.fromMillis(next))
      }
      result
    } catch (e: InvalidRecurrenceRuleException) {
      Logger.e(TAG, "Failed to parse recurrence rule: $rrule", e)
      listOf(start)
    }
  }

  companion object {
    private const val TAG = "GetTodayScheduleUseCase"
    private const val MAX_ITERATIONS = 500
  }
}
