package com.github.naz013.repository

import com.github.naz013.domain.CalendarEvent

interface CalendarEventRepository {
  suspend fun save(calendarEvent: CalendarEvent)

  suspend fun getAll(): List<CalendarEvent>
  suspend fun getByReminderId(id: String): List<CalendarEvent>
  suspend fun eventIds(): List<Long>

  suspend fun delete(id: String)
}
