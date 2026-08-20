package com.github.naz013.feature.settings.calendar.usecase

import android.app.AlarmManager
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.CalendarEvent
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.settings.calendar.CalendarSettingsPreferences
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.repository.CalendarEventRepository
import com.github.naz013.repository.GroupV2Repository
import kotlinx.coroutines.withContext
import org.dmfs.rfc5545.recur.Freq
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule
import java.util.Calendar
import com.github.naz013.domain.reminder.v2.RecurrenceRule as ReminderRecurrenceRule

internal class ScanGoogleCalendarForNewEventsUseCase(
  private val prefs: CalendarSettingsPreferences,
  private val googleCalendarApi: GoogleCalendarApi,
  private val dateTimeManager: DateTimeManager,
  private val calendarEventRepository: CalendarEventRepository,
  private val groupV2Repository: GroupV2Repository,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val dispatcherProvider: DispatcherProvider,
) {
  suspend operator fun invoke() {
    if (!prefs.scanGoogleCalendarEvents) {
      Logger.w(TAG, "Google Calendar scanning is disabled in preferences.")
      return
    }
    val calendarId = prefs.googleCalendarReminderId
    if (calendarId <= 0) {
      Logger.w(TAG, "No Google Calendar reminder ID set in preferences.")
      return
    }
    scanCalendar(calendarId)
    withContext(dispatcherProvider.main()) {
      appWidgetUpdater.updateCalendarWidget()
    }
  }

  suspend fun scanCalendar(calendarId: Long) {
    val events = googleCalendarApi.getEvents(listOf(calendarId))
    if (events.isEmpty()) {
      Logger.w(TAG, "No events found for Google Calendar ID: $calendarId")
      return
    }
    Logger.i(TAG, "Found ${events.size} events in Google Calendar (ID: $calendarId)")

    val currTime = System.currentTimeMillis()
    var eventsCount = 0

    val alreadyImportedIds = calendarEventRepository.eventIds()
    val newEvents = events.filterNot { alreadyImportedIds.contains(it.id) }

    val groupId = groupV2Repository.defaultGroup()?.uuId
    Logger.i(TAG, "Using reminder group ID: $groupId")

    for (item in newEvents) {
      val rrule = item.rrule
      var repeat: Long = 0
      Logger.d(TAG, "Importing event: rrule=$rrule, dtStart=${item.dtStart}")
      if (rrule != "" && !rrule.matches("".toRegex())) {
        try {
          val rule = RecurrenceRule(rrule)
          val interval = rule.interval
          val freq = rule.freq
          repeat =
            when {
              freq === Freq.SECONDLY -> interval * DateTimeManager.SECOND
              freq === Freq.MINUTELY -> interval * DateTimeManager.MINUTE
              freq === Freq.HOURLY -> interval * DateTimeManager.HOUR
              freq === Freq.WEEKLY -> interval.toLong() * 7 * DateTimeManager.DAY
              freq === Freq.MONTHLY -> interval.toLong() * 30 * DateTimeManager.DAY
              freq === Freq.YEARLY -> interval.toLong() * 365 * DateTimeManager.DAY
              else -> interval * DateTimeManager.DAY
            }
        } catch (e: InvalidRecurrenceRuleException) {
          Logger.e(TAG, "Failed to parse recurrence rule: $rrule", e)
        }
      }
      val summary = item.title
      val calendar = Calendar.getInstance()
      var dtStart = item.dtStart
      calendar.timeInMillis = dtStart
      if (dtStart >= currTime) {
        eventsCount += 1
        saveReminder(
          itemId = item.id,
          summary = summary,
          dtStart = dtStart,
          repeat = repeat,
          categoryId = groupId,
          allDay = item.allDay == 1,
        )
      } else {
        if (repeat > 0) {
          do {
            calendar.timeInMillis = dtStart + repeat * AlarmManager.INTERVAL_DAY
            dtStart = calendar.timeInMillis
          } while (dtStart < currTime)
          eventsCount += 1
          saveReminder(
            itemId = item.id,
            summary = summary,
            dtStart = dtStart,
            repeat = repeat,
            categoryId = groupId,
            allDay = item.allDay == 1,
          )
        }
      }
    }
    Logger.i(TAG, "Imported $eventsCount new events from Google Calendar (ID: $calendarId)")
  }

  private suspend fun saveReminder(
    itemId: Long,
    summary: String,
    dtStart: Long,
    repeat: Long,
    categoryId: String?,
    allDay: Boolean,
  ) {
    val eventDateTime = dateTimeManager.localToUtc(dateTimeManager.fromMillis(dtStart))
    val reminder = ReminderV2(
      groupId = categoryId,
      summary = summary,
      recurrence = if (repeat > 0) ReminderRecurrenceRule.Daily(repeatInterval = repeat) else ReminderRecurrenceRule.Once,
      schedule = ReminderSchedule(startDateTime = eventDateTime, eventDateTime = eventDateTime),
      sync = SyncMetadata(syncState = SyncState.WaitingForUpload),
    )
    activateReminderUseCase(reminder)
    calendarEventRepository.save(
      CalendarEvent(
        reminderId = reminder.uuId,
        event = summary,
        eventId = itemId,
        allDay = allDay,
      ),
    )
  }

  companion object {
    private const val TAG = "ScanGoogleCalendarForNewEventsUseCase"
  }
}
