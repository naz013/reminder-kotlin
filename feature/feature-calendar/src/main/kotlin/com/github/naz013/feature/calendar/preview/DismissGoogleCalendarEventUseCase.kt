package com.github.naz013.feature.calendar.preview

import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.logging.Logger
import com.github.naz013.notification.NotificationApi
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.GoogleCalendarEventRepository
import com.github.naz013.scheduler.JobSchedulerApi

/**
 * Removes a Google Calendar event from the app - always: clears its occurrence(s) so it stops
 * showing on the month/timeline views, cancels its scheduled alarm and any currently-shown
 * notification, and marks it dismissed (a tombstone, not a delete) so the next scan never
 * re-imports it. [alsoDeleteFromDeviceCalendar] additionally removes the underlying device
 * calendar event itself, which - for a calendar backed by a Google account - propagates the
 * deletion to Google's servers via the device's own sync adapter.
 */
internal class DismissGoogleCalendarEventUseCase(
  private val googleCalendarEventRepository: GoogleCalendarEventRepository,
  private val eventOccurrenceRepository: EventOccurrenceRepository,
  private val jobSchedulerApi: JobSchedulerApi,
  private val notificationApi: NotificationApi,
  private val googleCalendarApi: GoogleCalendarApi,
) {
  suspend operator fun invoke(
    event: GoogleCalendarEvent,
    alsoDeleteFromDeviceCalendar: Boolean,
  ) {
    Logger.i(
      TAG,
      "Dismissing Google Calendar event: ${event.uuId}, alsoDeleteFromDeviceCalendar=$alsoDeleteFromDeviceCalendar",
    )
    eventOccurrenceRepository.deleteByEventId(event.uuId)
    jobSchedulerApi.cancelGoogleCalendarEvent(event.uniqueId)
    notificationApi.cancel(event.uniqueId)
    googleCalendarEventRepository.markDismissed(event.uuId)
    if (alsoDeleteFromDeviceCalendar) {
      googleCalendarApi.deleteEvent(event.deviceEventId)
    }
  }

  companion object {
    private const val TAG = "DismissGoogleCalendarEventUseCase"
  }
}
