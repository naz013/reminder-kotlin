package com.github.naz013.repository

import com.github.naz013.domain.GoogleCalendarEvent

interface GoogleCalendarEventRepository {
  suspend fun save(event: GoogleCalendarEvent)

  suspend fun getById(id: String): GoogleCalendarEvent?

  suspend fun getByDeviceEventId(deviceEventId: Long): GoogleCalendarEvent?

  /** Non-dismissed events only - what the calendar/timeline screens should render. */
  suspend fun getVisible(): List<GoogleCalendarEvent>

  /**
   * Every device event id this repository has ever seen, dismissed or not - the dedup source for
   * the scan that discovers new device calendar events, so a dismissed event is never re-imported.
   */
  suspend fun knownDeviceEventIds(): List<Long>

  suspend fun markDismissed(id: String)

  /** Removes the local row entirely - for events the scan detects were removed on the device
   *  side (not a user-initiated app delete), which should be free to reappear if re-added later. */
  suspend fun deleteByDeviceEventId(deviceEventId: Long)

  suspend fun deleteAll()
}
