package com.github.naz013.logic.reminder.today

import com.github.naz013.domain.CalendarEvent

internal data class DedupedToday(
  val reminders: List<TodayItem.Reminder>,
  val calendarEvents: List<TodayItem.CalendarEvent>,
  val tasks: List<TodayItem.Task>,
)

/**
 * Reminders win: a calendar event or Google task that is only a mirror of a reminder is dropped and
 * the reminder is flagged instead. Calendar events match by exported device event id first, then
 * by calendar + start + title (the "From Reminder" description is localized, so it is not compared).
 */
internal fun dedupToday(
  reminders: List<TodayItem.Reminder>,
  calendarEvents: List<TodayItem.CalendarEvent>,
  tasks: List<TodayItem.Task>,
  calendarLinks: List<CalendarEvent>,
  matchCandidates: List<TodayItem.Reminder> = reminders,
): DedupedToday {
  val remindersById = matchCandidates.associateBy { it.reminder.uuId }
  val linkedInCalendar = mutableSetOf<String>()
  val linkedInTasks = mutableSetOf<String>()

  val keptEvents = calendarEvents.filter { item ->
    val matched = findReminderForEvent(item, remindersById, matchCandidates, calendarLinks)
    if (matched != null) linkedInCalendar.add(matched.reminder.uuId)
    matched == null
  }

  val keptTasks = tasks.filter { item ->
    val matched = remindersById[item.task.uuId]
    if (matched != null) linkedInTasks.add(matched.reminder.uuId)
    matched == null
  }

  val flagged = reminders.map {
    it.copy(
      alsoInCalendar = it.reminder.uuId in linkedInCalendar,
      alsoInTasks = it.reminder.uuId in linkedInTasks,
    )
  }
  return DedupedToday(flagged, keptEvents, keptTasks)
}

private fun findReminderForEvent(
  item: TodayItem.CalendarEvent,
  remindersById: Map<String, TodayItem.Reminder>,
  reminders: List<TodayItem.Reminder>,
  calendarLinks: List<CalendarEvent>,
): TodayItem.Reminder? {
  val event = item.event
  val byId = calendarLinks
    .firstOrNull { it.eventId == event.deviceEventId }
    ?.let { remindersById[it.reminderId] }
  if (byId != null) return byId
  return reminders.firstOrNull {
    it.reminder.calendarExport?.calendarId == event.calendarId &&
      it.dateTime == item.dateTime &&
      it.reminder.summary.trim().equals(event.title.trim(), ignoreCase = true)
  }
}
