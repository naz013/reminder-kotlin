package com.elementary.tasks.reminder.work

import android.app.AlarmManager
import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.datetime.TimeCount
import com.elementary.tasks.core.utils.datetime.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import org.dmfs.rfc5545.recur.Freq
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule
import java.util.*

class CheckEventsWorker(
  private val appDb: AppDb,
  private val prefs: Prefs,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  context: Context,
  workerParams: WorkerParameters
) : Worker(context, workerParams) {

  override fun doWork(): Result {
    if (Permissions.checkPermission(applicationContext, Permissions.READ_CALENDAR,
        Permissions.WRITE_CALENDAR)) {
      launchCheckEvents()
    }
    return Result.success()
  }

  private fun launchCheckEvents() {
    launchDefault {
      val currTime = System.currentTimeMillis()
      val eventItems = googleCalendarUtils.getEvents(prefs.trackCalendarIds)
      if (eventItems.isNotEmpty()) {
        val list = appDb.calendarEventsDao().eventIds()
        for (item in eventItems) {
          val itemId = item.id
          if (!list.contains(itemId)) {
            val rrule = item.rrule
            var repeat: Long = 0
            if (!rrule.matches("".toRegex())) {
              try {
                val rule = RecurrenceRule(rrule)
                val interval = rule.interval
                val freq = rule.freq
                repeat = when {
                  freq === Freq.SECONDLY -> interval * TimeCount.SECOND
                  freq === Freq.MINUTELY -> interval * TimeCount.MINUTE
                  freq === Freq.HOURLY -> interval * TimeCount.HOUR
                  freq === Freq.WEEKLY -> interval.toLong() * 7 * TimeCount.DAY
                  freq === Freq.MONTHLY -> interval.toLong() * 30 * TimeCount.DAY
                  freq === Freq.YEARLY -> interval.toLong() * 365 * TimeCount.DAY
                  else -> interval * TimeCount.DAY
                }
              } catch (e: InvalidRecurrenceRuleException) {
                e.printStackTrace()
              }

            }
            val summary = item.title
            val def = appDb.reminderGroupDao().defaultGroup()
            var categoryId = ""
            if (def != null) {
              categoryId = def.groupUuId
            }
            val calendar = Calendar.getInstance()
            var dtStart = item.dtStart
            calendar.timeInMillis = dtStart
            if (dtStart >= currTime) {
              saveReminder(itemId, summary, dtStart, repeat, categoryId, item.calendarId)
            } else {
              if (repeat > 0) {
                do {
                  calendar.timeInMillis = dtStart + repeat * AlarmManager.INTERVAL_DAY
                  dtStart = calendar.timeInMillis
                } while (dtStart < currTime)
                saveReminder(itemId, summary, dtStart, repeat, categoryId, item.calendarId)
              }
            }
          }
        }
      }
    }
  }

  private fun saveReminder(itemId: Long, summary: String, dtStart: Long, repeat: Long,
                           categoryId: String, calendarId: Long) {
    val reminder = Reminder()
    reminder.type = Reminder.BY_DATE
    reminder.repeatInterval = repeat
    reminder.groupUuId = categoryId
    reminder.summary = summary
    reminder.calendarId = calendarId
    reminder.eventTime = TimeUtil.getGmtFromDateTime(dtStart)
    reminder.startTime = TimeUtil.getGmtFromDateTime(dtStart)
    appDb.reminderDao().insert(reminder)
    eventControlFactory.getController(reminder).start()
    appDb.calendarEventsDao().insert(CalendarEvent(reminder.uuId, summary, itemId))
  }

  companion object {
    private const val TAG = "CheckEventsWorker"

    fun schedule(context: Context) {
      val work = OneTimeWorkRequest.Builder(CheckEventsWorker::class.java)
        .addTag(TAG)
        .build()
      WorkManager.getInstance(context).enqueue(work)
    }
  }
}
