package com.github.naz013.googlecalendar

import com.github.naz013.domain.reminder.v2.ReminderV2

interface GoogleCalendarApi {

  /**
   * Add event to calendar.
   */
  suspend fun addEvent(reminder: ReminderV2)

  suspend fun deleteEvents(id: String)

  fun deleteEvent(id: Long)

  suspend fun loadEvents(reminderId: String): List<EventItem>

  fun getCalendarsList(): List<CalendarItem>

  fun getCalendarById(id: Long): CalendarItem?

  fun getEvents(ids: List<Long>): List<EventItem>
}
