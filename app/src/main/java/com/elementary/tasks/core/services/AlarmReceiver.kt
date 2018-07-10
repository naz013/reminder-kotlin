package com.elementary.tasks.core.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask

import com.elementary.tasks.birthdays.work.CheckBirthdaysAsync
import com.elementary.tasks.core.async.BackupTask
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil

import org.dmfs.rfc5545.recur.Freq
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule

import java.util.Calendar

import androidx.legacy.content.WakefulBroadcastReceiver

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

class AlarmReceiver : WakefulBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        LogUtil.d(TAG, "onReceive: Action - " + action + ", time - " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true, true))
        if (action == null) return
        val service = Intent(context, AlarmReceiver::class.java)
        context.startService(service)
        when (action) {
            ACTION_SYNC_AUTO -> BackupTask(context).execute()
            ACTION_EVENTS_CHECK -> checkEvents(context)
            ACTION_BIRTHDAY_AUTO -> CheckBirthdaysAsync(context).execute()
            ACTION_BIRTHDAY_PERMANENT -> if (Prefs.getInstance(context).isBirthdayPermanentEnabled) {
                Notifier.showBirthdayPermanent(context)
            }
        }
    }

    private fun checkEvents(context: Context) {
        if (Permissions.checkPermission(context, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
            CheckEventsAsync(context).execute()
        }
    }

    fun enableBirthdayPermanentAlarm(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ACTION_BIRTHDAY_PERMANENT
        val alarmIntent = PendingIntent.getBroadcast(context, BIRTHDAY_PERMANENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager ?: return
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
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr?.cancel(alarmIntent)
    }

    fun enableBirthdayCheckAlarm(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ACTION_BIRTHDAY_AUTO
        val alarmIntent = PendingIntent.getBroadcast(context, BIRTHDAY_CHECK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager ?: return
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val currTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 2)
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

    fun cancelBirthdayCheckAlarm(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ACTION_BIRTHDAY_AUTO
        val alarmIntent = PendingIntent.getBroadcast(context, BIRTHDAY_CHECK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr?.cancel(alarmIntent)
    }

    fun enableEventCheck(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ACTION_EVENTS_CHECK
        val alarmIntent = PendingIntent.getBroadcast(context, EVENTS_CHECK_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager ?: return
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val interval = Prefs.getInstance(context).autoCheckInterval
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
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr?.cancel(alarmIntent)
    }

    fun enableAutoSync(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = ACTION_SYNC_AUTO
        val alarmIntent = PendingIntent.getBroadcast(context, AUTO_SYNC_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager ?: return
        val calendar = Calendar.getInstance()
        val interval = Prefs.getInstance(context).autoBackupInterval
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
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr?.cancel(alarmIntent)
    }

    private class CheckEventsAsync internal constructor(private val mContext: Context) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            val currTime = System.currentTimeMillis()
            val calID = Prefs.getInstance(mContext).eventsCalendar
            val eventItems = CalendarUtils.getEvents(mContext, calID)
            if (eventItems.size > 0) {
                val list = AppDb.getAppDatabase(mContext).calendarEventsDao().eventIds
                for (item in eventItems) {
                    val itemId = item.id
                    if (!list.contains(itemId)) {
                        val rrule = item.rrule
                        var repeat: Long = 0
                        if (rrule != null && !rrule.matches("".toRegex())) {
                            try {
                                val rule = RecurrenceRule(rrule)
                                val interval = rule.interval
                                val freq = rule.freq
                                if (freq === Freq.SECONDLY)
                                    repeat = interval * TimeCount.SECOND
                                else if (freq === Freq.MINUTELY)
                                    repeat = interval * TimeCount.MINUTE
                                else if (freq === Freq.HOURLY)
                                    repeat = interval * TimeCount.HOUR
                                else if (freq === Freq.WEEKLY)
                                    repeat = interval.toLong() * 7 * TimeCount.DAY
                                else if (freq === Freq.MONTHLY)
                                    repeat = interval.toLong() * 30 * TimeCount.DAY
                                else if (freq === Freq.YEARLY)
                                    repeat = interval.toLong() * 365 * TimeCount.DAY
                                else
                                    repeat = interval * TimeCount.DAY
                            } catch (e: InvalidRecurrenceRuleException) {
                                e.printStackTrace()
                            }

                        }
                        val summary = item.title
                        val def = AppDb.getAppDatabase(mContext).groupDao().default
                        var categoryId: String? = ""
                        if (def != null) {
                            categoryId = def.uuId
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

        private fun saveReminder(itemId: Long, summary: String, dtStart: Long, repeat: Long, categoryId: String?) {
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
            appDb.calendarEventsDao().insert(CalendarEvent(reminder.uniqueId, summary, itemId))
        }
    }

    companion object {

        private val AUTO_SYNC_ID = Integer.MAX_VALUE - 1
        private val BIRTHDAY_PERMANENT_ID = Integer.MAX_VALUE - 2
        private val BIRTHDAY_CHECK_ID = Integer.MAX_VALUE - 4
        private val EVENTS_CHECK_ID = Integer.MAX_VALUE - 5

        private val ACTION_BIRTHDAY_PERMANENT = "com.elementary.alarm.BIRTHDAY_PERMANENT"
        private val ACTION_BIRTHDAY_AUTO = "com.elementary.alarm.BIRTHDAY_AUTO"
        private val ACTION_SYNC_AUTO = "com.elementary.alarm.SYNC_AUTO"
        private val ACTION_EVENTS_CHECK = "com.elementary.alarm.EVENTS_CHECK"

        private val TAG = "AlarmReceiver"
    }
}
