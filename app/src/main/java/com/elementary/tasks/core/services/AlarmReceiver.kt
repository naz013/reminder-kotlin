package com.elementary.tasks.core.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.birthdays.work.CheckBirthdaysWorker
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.work.BackupDataWorker
import org.dmfs.rfc5545.recur.Freq
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class AlarmReceiver : BaseBroadcast(), KoinComponent {

    private val calendarUtils: CalendarUtils by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Timber.d("onReceive: Action - $action, time - ${TimeUtil.getFullDateTime(System.currentTimeMillis(), true)}")
        if (action == null) return
        val service = Intent(context, AlarmReceiver::class.java)
        context.startService(service)
        when (action) {
            ACTION_SYNC_AUTO -> BackupDataWorker.schedule()
            ACTION_EVENTS_CHECK -> checkEvents(context)
            ACTION_BIRTHDAY_PERMANENT -> if (prefs.isBirthdayPermanentEnabled) {
                notifier.showBirthdayPermanent()
            }
        }
    }

    private fun checkEvents(context: Context) {
        if (Permissions.checkPermission(context, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
            CheckEventsAsync(context, prefs, calendarUtils).execute()
        }
    }

    fun enableBirthdayPermanentAlarm(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ACTION_BIRTHDAY_PERMANENT
        val alarmIntent = PendingIntent.getBroadcast(context, BIRTHDAY_PERMANENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager? ?: return
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val currTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 5)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        var time = calendar.timeInMillis
        while (currTime > time) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            time = calendar.timeInMillis
        }
        if (Module.isMarshmallow) {
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, alarmIntent)
        } else {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, alarmIntent)
        }
    }

    fun cancelBirthdayPermanentAlarm(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ACTION_BIRTHDAY_PERMANENT
        val alarmIntent = PendingIntent.getBroadcast(context, BIRTHDAY_PERMANENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmMgr?.cancel(alarmIntent)
    }

    fun enableBirthdayCheckAlarm() {
        val work = PeriodicWorkRequest.Builder(CheckBirthdaysWorker::class.java, 24, TimeUnit.HOURS, 1, TimeUnit.HOURS)
                .addTag("BD_CHECK")
                .build()
        WorkManager.getInstance().enqueue(work)
    }

    fun cancelBirthdayCheckAlarm() {
        WorkManager.getInstance().cancelAllWorkByTag("BD_CHECK")
    }

    fun enableEventCheck(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ACTION_EVENTS_CHECK
        val alarmIntent = PendingIntent.getBroadcast(context, EVENTS_CHECK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager? ?: return
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val interval = prefs.autoCheckInterval
        if (Module.isMarshmallow) {
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                    AlarmManager.INTERVAL_HOUR * interval, alarmIntent)
        } else {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                    AlarmManager.INTERVAL_HOUR * interval, alarmIntent)
        }
    }

    fun cancelEventCheck(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ACTION_EVENTS_CHECK
        val alarmIntent = PendingIntent.getBroadcast(context, EVENTS_CHECK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmMgr?.cancel(alarmIntent)
    }

    fun enableAutoSync(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ACTION_SYNC_AUTO
        val alarmIntent = PendingIntent.getBroadcast(context, AUTO_SYNC_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager? ?: return
        val calendar = Calendar.getInstance()
        val interval = prefs.autoBackupInterval
        calendar.timeInMillis = System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR * interval
        if (Module.isMarshmallow) {
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                    AlarmManager.INTERVAL_HOUR * interval, alarmIntent)
        } else {
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                    AlarmManager.INTERVAL_HOUR * interval, alarmIntent)
        }
    }

    fun cancelAutoSync(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ACTION_SYNC_AUTO
        val alarmIntent = PendingIntent.getBroadcast(context, AUTO_SYNC_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmMgr?.cancel(alarmIntent)
    }

    private class CheckEventsAsync constructor(private val mContext: Context, val prefs: Prefs, val calendarUtils: CalendarUtils) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            val currTime = System.currentTimeMillis()
            val calID = prefs.eventsCalendar
            val eventItems = calendarUtils.getEvents(calID)
            if (eventItems.isNotEmpty()) {
                val list = AppDb.getAppDatabase(mContext).calendarEventsDao().eventIds()
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
                        val def = AppDb.getAppDatabase(mContext).reminderGroupDao().defaultGroup()
                        var categoryId = ""
                        if (def != null) {
                            categoryId = def.groupUuId
                        }
                        val calendar = Calendar.getInstance()
                        var dtStart = item.dtStart
                        calendar.timeInMillis = dtStart
                        if (dtStart >= currTime) {
                            saveReminder(itemId, summary, dtStart, repeat, categoryId)
                        } else {
                            if (repeat > 0) {
                                do {
                                    calendar.timeInMillis = dtStart + repeat * AlarmManager.INTERVAL_DAY
                                    dtStart = calendar.timeInMillis
                                } while (dtStart < currTime)
                                saveReminder(itemId, summary, dtStart, repeat, categoryId)
                            }
                        }
                    }
                }
            }
            return null
        }

        private fun saveReminder(itemId: Long, summary: String, dtStart: Long, repeat: Long, categoryId: String) {
            val reminder = Reminder()
            reminder.type = Reminder.BY_DATE
            reminder.repeatInterval = repeat
            reminder.groupUuId = categoryId
            reminder.summary = summary
            reminder.eventTime = TimeUtil.getGmtFromDateTime(dtStart)
            reminder.startTime = TimeUtil.getGmtFromDateTime(dtStart)
            val appDb = AppDb.getAppDatabase(mContext)
            appDb.reminderDao().insert(reminder)
            EventControlFactory.getController(reminder).start()
            appDb.calendarEventsDao().insert(CalendarEvent(reminder.uuId, summary, itemId))
        }
    }

    companion object {
        private const val AUTO_SYNC_ID = Integer.MAX_VALUE - 1
        private const val BIRTHDAY_PERMANENT_ID = Integer.MAX_VALUE - 2
        private const val EVENTS_CHECK_ID = Integer.MAX_VALUE - 5

        private const val ACTION_BIRTHDAY_PERMANENT = "com.elementary.alarm.BIRTHDAY_PERMANENT"
        private const val ACTION_SYNC_AUTO = "com.elementary.alarm.SYNC_AUTO"
        private const val ACTION_EVENTS_CHECK = "com.elementary.alarm.EVENTS_CHECK"
    }
}
