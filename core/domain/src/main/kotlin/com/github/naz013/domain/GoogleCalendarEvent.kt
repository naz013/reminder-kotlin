package com.github.naz013.domain

import org.threeten.bp.LocalDateTime
import java.util.Random
import java.util.UUID

/**
 * A calendar event imported (read-only, for display) from the device calendar provider - not a
 * reminder. [isDismissed] is a tombstone: once true the row is kept but filtered out of every
 * "show" query, so the scan that discovers new device events never re-imports it.
 */
data class GoogleCalendarEvent(
  val deviceEventId: Long,
  val calendarId: Long,
  val calendarName: String,
  val title: String,
  val description: String,
  val startDateTime: LocalDateTime,
  val endDateTime: LocalDateTime?,
  val allDay: Boolean,
  val rrule: String,
  val isDismissed: Boolean = false,
  val uuId: String = UUID.randomUUID().toString(),
  /** AlarmManager request code / notification id for this event's scheduled alert. */
  val uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
)
