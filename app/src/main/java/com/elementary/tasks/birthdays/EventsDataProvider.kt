package com.elementary.tasks.birthdays

import android.app.AlarmManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.elementary.tasks.birthdays.work.CheckBirthdaysAsync
import com.elementary.tasks.core.calendar.Events
import com.elementary.tasks.core.calendar.FlextHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import hirondelle.date4j.DateTime
import java.text.ParseException
import java.util.*

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
class EventsDataProvider(context: Context, private val isReminder: Boolean, private val isFeature: Boolean) {
    @Volatile
    var isReady: Boolean = false
        private set
    private val observers = mutableListOf<Callback>()
    private val map = mutableMapOf<DateTime, Events>()
    private val mHandler = Handler(Looper.getMainLooper())

    val events: Map<DateTime, Events>
        get() = map

    init {
        this.isReady = false
        Thread { loadEvents(context) }.start()
    }

    fun addObserver(callback: Callback) {
        if (!observers.contains(callback)) observers.add(callback)
    }

    fun removeObserver(callback: Callback) {
        if (observers.contains(callback)) observers.add(callback)
    }

    private fun notifyObservers(callbacks: List<Callback>) {
        mHandler.post {
            if (!observers.isEmpty()) {
                for (callback in callbacks) {
                    callback.onReady()
                }
            }
        }
    }

    private fun setEvent(eventTime: Long, summary: String, color: Int, type: Events.Type) {
        val key = FlextHelper.convertToDateTime(eventTime)
        if (map.containsKey(key)) {
            var ev = map[key]
            if (ev == null) {
                ev = Events(summary, color, type, eventTime)
            } else {
                ev.addEvent(summary, color, type, eventTime)
            }
            map[key] = ev
        } else {
            val events = Events(summary, color, type, eventTime)
            map[key] = events
        }
    }

    private fun loadEvents(context: Context) {
        map.clear()
        val cs = ThemeUtil.getInstance(context)
        val bColor = cs.getColor(cs.colorBirthdayCalendar())
        val timeCount = TimeCount.getInstance(context)
        if (isReminder) {
            val rColor = cs.getColor(cs.colorReminderCalendar())
            val reminders = AppDb.getAppDatabase(context).reminderDao().getAll(true, false)
            for (item in reminders) {
                val mType = item.type
                val summary = item.summary
                var eventTime = item.dateTime
                if (!Reminder.isGpsType(mType) && eventTime > 0) {
                    val repeatTime = item.repeatInterval
                    val limit = item.repeatLimit.toLong()
                    val count = item.eventCount
                    val isLimited = limit > 0
                    setEvent(eventTime, summary, rColor, Events.Type.REMINDER)
                    if (isFeature) {
                        val calendar1 = Calendar.getInstance()
                        calendar1.timeInMillis = item.startDateTime
                        if (Reminder.isBase(mType, Reminder.BY_WEEK)) {
                            var days: Long = 0
                            var max = Configs.MAX_DAYS_COUNT
                            if (isLimited) {
                                max = limit - count
                            }
                            val list = item.weekdays
                            do {
                                calendar1.timeInMillis = calendar1.timeInMillis + AlarmManager.INTERVAL_DAY
                                eventTime = calendar1.timeInMillis
                                if (eventTime == item.dateTime) {
                                    continue
                                }
                                val weekDay = calendar1.get(Calendar.DAY_OF_WEEK)
                                if (list[weekDay - 1] == 1) {
                                    days++
                                    setEvent(eventTime, summary, rColor, Events.Type.REMINDER)
                                }
                            } while (days < max)
                        } else if (Reminder.isBase(mType, Reminder.BY_MONTH)) {
                            var days: Long = 0
                            var max = Configs.MAX_DAYS_COUNT
                            if (isLimited) {
                                max = limit - count
                            }
                            do {
                                item.eventTime = TimeUtil.getGmtFromDateTime(eventTime)
                                eventTime = timeCount.getNextMonthDayTime(item)
                                if (eventTime == item.dateTime) {
                                    continue
                                }
                                calendar1.timeInMillis = eventTime
                                days++
                                setEvent(eventTime, summary, rColor, Events.Type.REMINDER)
                            } while (days < max)
                        } else {
                            if (repeatTime == 0L) {
                                continue
                            }
                            var days: Long = 0
                            var max = Configs.MAX_DAYS_COUNT
                            if (isLimited) {
                                max = limit - count
                            }
                            do {
                                calendar1.timeInMillis = calendar1.timeInMillis + repeatTime
                                eventTime = calendar1.timeInMillis
                                if (eventTime == item.dateTime) {
                                    continue
                                }
                                days++
                                setEvent(eventTime, summary, rColor, Events.Type.REMINDER)
                            } while (days < max)
                        }
                    }
                }
            }
        }
        val list = AppDb.getAppDatabase(context).birthdaysDao().all()
        LogUtil.d(TAG, "Count BD" + list.size)
        for (item in list) {
            var date: Date? = null
            try {
                date = CheckBirthdaysAsync.DATE_FORMAT.parse(item.date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            val year = calendar.get(Calendar.YEAR)
            if (date != null) {
                calendar.time = date
                var i = -1
                while (i < 2) {
                    calendar.set(Calendar.YEAR, year + i)
                    setEvent(calendar.timeInMillis, item.name, bColor, Events.Type.BIRTHDAY)
                    i++
                }
            }
        }
        isReady = true
        notifyObservers(ArrayList(observers))
    }

    interface Callback {
        fun onReady()
    }

    companion object {

        private const val TAG = "EventsDataProvider"
    }
}
