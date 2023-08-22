package com.elementary.tasks.core.utils

import android.app.AlarmManager
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.dmfs.rfc5545.recur.Freq
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule
import timber.log.Timber
import java.util.Calendar

class EventImportProcessor(
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val appDb: AppDb,
  private val dateTimeManager: DateTimeManager,
  private val eventControlFactory: EventControlFactory
) {

  fun importEventsFor(ids: List<Long>): Result {
    val currTime = System.currentTimeMillis()
    var eventsCount = 0
    val eventItems = googleCalendarUtils.getEvents(ids)
    Timber.d("import: eventItems=${eventItems.size}")
    if (eventItems.isNotEmpty()) {
      val list = appDb.calendarEventsDao().eventIds()
      for (item in eventItems) {
        val itemId = item.id
        if (!list.contains(itemId)) {
          val rrule = item.rrule
          var repeat: Long = 0
          Timber.d("import: rrule=$rrule, dtStart=${item.dtStart}")
          if (rrule != "" && !rrule.matches("".toRegex())) {
            try {
              val rule = RecurrenceRule(rrule)
              val interval = rule.interval
              val freq = rule.freq
              repeat = when {
                freq === Freq.SECONDLY -> interval * DateTimeManager.SECOND
                freq === Freq.MINUTELY -> interval * DateTimeManager.MINUTE
                freq === Freq.HOURLY -> interval * DateTimeManager.HOUR
                freq === Freq.WEEKLY -> interval.toLong() * 7 * DateTimeManager.DAY
                freq === Freq.MONTHLY -> interval.toLong() * 30 * DateTimeManager.DAY
                freq === Freq.YEARLY -> interval.toLong() * 365 * DateTimeManager.DAY
                else -> interval * DateTimeManager.DAY
              }
            } catch (e: InvalidRecurrenceRuleException) {
              e.printStackTrace()
            }
          }
          val summary = item.title
          val group = appDb.reminderGroupDao().defaultGroup()
          var categoryId = ""
          if (group != null) {
            categoryId = group.groupUuId
          }
          val calendar = Calendar.getInstance()
          var dtStart = item.dtStart
          calendar.timeInMillis = dtStart
          if (dtStart >= currTime) {
            eventsCount += 1
            saveReminder(
              itemId = itemId,
              summary = summary,
              dtStart = dtStart,
              repeat = repeat,
              categoryId = categoryId,
              calendarId = item.calendarId,
              allDay = item.allDay == 1
            )
          } else {
            if (repeat > 0) {
              do {
                calendar.timeInMillis = dtStart + repeat * AlarmManager.INTERVAL_DAY
                dtStart = calendar.timeInMillis
              } while (dtStart < currTime)
              eventsCount += 1
              saveReminder(
                itemId = itemId,
                summary = summary,
                dtStart = dtStart,
                repeat = repeat,
                categoryId = categoryId,
                calendarId = item.calendarId,
                allDay = item.allDay == 1
              )
            }
          }
        }
      }
    }
    return Result(eventsCount)
  }

  private fun saveReminder(
    itemId: Long,
    summary: String,
    dtStart: Long,
    repeat: Long,
    categoryId: String,
    calendarId: Long,
    allDay: Boolean
  ) {
    val reminder = Reminder().apply {
      this.type = Reminder.BY_DATE
      this.repeatInterval = repeat
      this.groupUuId = categoryId
      this.summary = summary
      this.calendarId = calendarId
      this.eventTime = dateTimeManager.getGmtFromDateTime(dateTimeManager.fromMillis(dtStart))
      this.startTime = dateTimeManager.getGmtFromDateTime(dateTimeManager.fromMillis(dtStart))
      this.allDay = allDay
    }
    appDb.reminderDao().insert(reminder)
    eventControlFactory.getController(reminder).start()
    appDb.calendarEventsDao().insert(
      CalendarEvent(
        reminderId = reminder.uuId,
        event = summary,
        eventId = itemId,
        allDay = allDay
      )
    )
  }

  data class Result(
    val importCount: Int
  )
}
