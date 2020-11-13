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
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import org.dmfs.rfc5545.recur.Freq
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@KoinApiExtension
class CheckEventsWorker(
  context: Context,
  workerParams: WorkerParameters
) : Worker(context, workerParams), KoinComponent {

  private val calendarUtils: CalendarUtils by inject()
  private val appDb: AppDb by inject()
  private val prefs: Prefs by inject()

  override fun doWork(): Result {
    if (Permissions.checkPermission(applicationContext, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
      launchCheckEvents(applicationContext)
    }
    return Result.success()
  }

  private fun launchCheckEvents(context: Context) {
    launchDefault {
      val currTime = System.currentTimeMillis()
      val eventItems = calendarUtils.getEvents(prefs.trackCalendarIds)
      if (eventItems.isNotEmpty()) {
        val list = AppDb.getAppDatabase(context).calendarEventsDao().eventIds()
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
            val def = AppDb.getAppDatabase(context).reminderGroupDao().defaultGroup()
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
    EventControlFactory.getController(reminder).start()
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
