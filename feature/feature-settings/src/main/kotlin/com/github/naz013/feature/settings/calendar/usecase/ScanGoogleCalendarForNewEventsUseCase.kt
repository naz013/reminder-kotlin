package com.github.naz013.feature.settings.calendar.usecase

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.settings.calendar.CalendarSettingsPreferences
import com.github.naz013.googlecalendar.EventItem
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.logging.Logger
import com.github.naz013.notification.NotificationApi
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.GoogleCalendarEventRepository
import com.github.naz013.scheduler.JobSchedulerApi
import kotlinx.coroutines.withContext

/**
 * Scans the calendars selected in Settings for events the app hasn't seen yet and imports them
 * for read-only display (month/timeline views) - never as reminders. New events are deduped
 * against [GoogleCalendarEventRepository.knownDeviceEventIds], which includes dismissed
 * (user-deleted-from-app) rows, so a dismissed event never comes back. Events removed on the
 * device side (not a user app-delete) are cleaned up the same way a fresh scan discovers new ones.
 */
internal class ScanGoogleCalendarForNewEventsUseCase(
  private val prefs: CalendarSettingsPreferences,
  private val googleCalendarApi: GoogleCalendarApi,
  private val dateTimeManager: DateTimeManager,
  private val googleCalendarEventRepository: GoogleCalendarEventRepository,
  private val eventOccurrenceRepository: EventOccurrenceRepository,
  private val calculateGoogleCalendarEventOccurrencesUseCase: CalculateGoogleCalendarEventOccurrencesUseCase,
  private val jobSchedulerApi: JobSchedulerApi,
  private val notificationApi: NotificationApi,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val dispatcherProvider: DispatcherProvider,
) {
  suspend operator fun invoke() {
    if (!prefs.scanGoogleCalendarEvents) {
      Logger.w(TAG, "Google Calendar scanning is disabled in preferences.")
      return
    }
    val calendarIds = prefs.selectedGoogleCalendarIds
    if (calendarIds.isEmpty()) {
      Logger.w(TAG, "No Google Calendars selected in preferences.")
      return
    }
    scanCalendars(calendarIds)
    withContext(dispatcherProvider.main()) {
      appWidgetUpdater.updateCalendarWidget()
    }
  }

  suspend fun scanCalendars(calendarIds: Set<Long>) {
    val events = googleCalendarApi.getEvents(calendarIds.toList())
    Logger.i(TAG, "Found ${events.size} event(s) across ${calendarIds.size} selected calendar(s)")

    reconcileRemovedEvents(calendarIds, events.mapTo(mutableSetOf()) { it.id })

    if (events.isEmpty()) return

    val calendarNames = calendarIds.associateWith { googleCalendarApi.getCalendarById(it)?.name.orEmpty() }
    val knownIds = googleCalendarEventRepository.knownDeviceEventIds().toSet()
    val newEvents = events.filterNot { it.id in knownIds }

    newEvents.forEach { importEvent(it, calendarNames) }
    Logger.i(TAG, "Imported ${newEvents.size} new Google Calendar event(s)")
  }

  /** Rows this repository already tracks (visible, i.e. not dismissed) for the currently-selected
   *  calendars whose device event no longer shows up in this scan are gone on the device side -
   *  clean them up so they stop occupying the month/timeline views. This is unrelated to the
   *  dismissed-tombstone path: an event removed here is free to be re-imported if it reappears. */
  private suspend fun reconcileRemovedEvents(
    calendarIds: Set<Long>,
    currentDeviceIds: Set<Long>,
  ) {
    val tracked = googleCalendarEventRepository.getVisible().filter { it.calendarId in calendarIds }
    for (event in tracked) {
      if (event.deviceEventId !in currentDeviceIds) {
        Logger.i(TAG, "Google Calendar event no longer on device, removing: ${event.uuId}")
        eventOccurrenceRepository.deleteByEventId(event.uuId)
        googleCalendarEventRepository.deleteByDeviceEventId(event.deviceEventId)
        jobSchedulerApi.cancelGoogleCalendarEvent(event.uniqueId)
        notificationApi.cancel(event.uniqueId)
      }
    }
  }

  private suspend fun importEvent(
    item: EventItem,
    calendarNames: Map<Long, String>,
  ) {
    val startDateTime = dateTimeManager.localToUtc(dateTimeManager.fromMillis(item.dtStart))
    val endDateTime =
      item.dtEnd.takeIf { it > 0 }?.let { dateTimeManager.localToUtc(dateTimeManager.fromMillis(it)) }
    val event =
      GoogleCalendarEvent(
        deviceEventId = item.id,
        calendarId = item.calendarId,
        calendarName = calendarNames[item.calendarId].orEmpty(),
        title = item.title,
        description = item.description,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        allDay = item.allDay == 1,
        rrule = item.rrule,
      )
    googleCalendarEventRepository.save(event)
    calculateGoogleCalendarEventOccurrencesUseCase(event)
    val nowUtc = dateTimeManager.localToUtc(dateTimeManager.getCurrentDateTime())
    if (event.startDateTime.isAfter(nowUtc)) {
      jobSchedulerApi.scheduleGoogleCalendarEvent(event)
    }
  }

  companion object {
    private const val TAG = "ScanGoogleCalendarForNewEventsUseCase"
  }
}
