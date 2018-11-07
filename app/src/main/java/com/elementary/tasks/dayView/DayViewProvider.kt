package com.elementary.tasks.dayView

import android.app.AlarmManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.dayView.day.EventModel
import timber.log.Timber
import java.text.ParseException
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import javax.inject.Inject
import kotlin.Comparator

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
class DayViewProvider(private val mContext: Context) {

    private val data = ArrayList<EventModel>()
    private var hour: Int = 0
    private var minute: Int = 0
    private var isFeature: Boolean = false
    private var isBirthdays: Boolean = false
    private var isReminders: Boolean = false
    @Volatile
    private var isDataChanged: Boolean = false
    @Volatile
    var isReady: Boolean = false
        private set
    @Volatile
    var isInProgress = false
        private set
    private val mHandler = Handler(Looper.getMainLooper())
    private val map = HashMap<Callback, CancelableRunnable>()
    private val observers = ArrayList<InitCallback>()

    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var themeUtil: ThemeUtil
    @Inject
    lateinit var timeCount: TimeCount

    init {
        ReminderApp.appComponent.inject(this)
        this.isDataChanged = true
    }

    fun addObserver(callback: InitCallback) {
        if (!observers.contains(callback)) observers.add(callback)
        if (isReady) callback.onFinish()
    }

    fun removeObserver(callback: InitCallback) {
        observers.remove(callback)
    }

    private fun notifyInitFinish() {
        mHandler.post { for (callback in observers) callback.onFinish() }
    }

    fun setDataChanged(dataChanged: Boolean) {
        isDataChanged = dataChanged
    }

    fun setBirthdays(isBirthdays: Boolean) {
        this.isBirthdays = isBirthdays
    }

    fun setReminders(isReminders: Boolean) {
        this.isReminders = isReminders
    }

    fun setTime(time: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        this.hour = calendar.get(Calendar.HOUR_OF_DAY)
        this.minute = calendar.get(Calendar.MINUTE)
    }

    fun setFeature(isFeature: Boolean) {
        this.isFeature = isFeature
    }

    fun removeCallback(callback: Callback) {
        if (map.containsKey(callback)) {
            val runnable = map[callback]
            if (runnable != null) {
                runnable.setCallback(null)
                runnable.setCanceled(true)
            }
            map.remove(callback)
        }
    }

    fun findMatches(day: Int, month: Int, year: Int, sort: Boolean, callback: Callback) {
        removeCallback(callback)
        val runnable = CancelableRunnable(day, month, year, sort, callback)
        map[callback] = runnable
        Thread(runnable).start()
    }

    fun fillArray() {
        if ((isDataChanged || data.isEmpty()) && !isInProgress) {
            isReady = false
            isInProgress = true
            Thread {
                data.clear()
                if (isBirthdays) {
                    loadBirthdays()
                }
                if (isReminders) {
                    loadReminders()
                }
                isReady = true
                isInProgress = false
                isDataChanged = false
                notifyInitFinish()
            }.start()
        }
    }

    private fun loadBirthdays() {
        val list = AppDb.getAppDatabase(mContext).birthdaysDao().all()
        val color = themeUtil.getColor(themeUtil.colorBirthdayCalendar())
        for (item in list) {
            var date: Date? = null
            try {
                date = TimeUtil.BIRTH_DATE_FORMAT.parse(item.date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (date != null) {
                val calendar1 = Calendar.getInstance()
                calendar1.time = date
                val bDay = calendar1.get(Calendar.DAY_OF_MONTH)
                val bMonth = calendar1.get(Calendar.MONTH)
                val bYear = calendar1.get(Calendar.YEAR)
                calendar1.timeInMillis = System.currentTimeMillis()
                calendar1.set(Calendar.MONTH, bMonth)
                calendar1.set(Calendar.DAY_OF_MONTH, bDay)
                calendar1.set(Calendar.HOUR_OF_DAY, hour)
                calendar1.set(Calendar.MINUTE, minute)
                data.add(EventModel(EventModel.BIRTHDAY, item, bDay, bMonth, bYear, color))
            }
        }
    }

    private fun loadReminders() {
        val allGroups = AppDb.getAppDatabase(mContext).reminderGroupDao().all()
        val map = HashMap<String, Int>()
        for (item in allGroups) {
            map[item.groupUuId] = item.groupColor
        }
        val reminders = AppDb.getAppDatabase(mContext).reminderDao().getAll(true, false)
        for (item in reminders) {
            val mType = item.type
            var eventTime = item.dateTime
            if (!Reminder.isGpsType(mType)) {
                val repeatTime = item.repeatInterval
                val limit = item.repeatLimit.toLong()
                val count = item.eventCount
                val isLimited = limit > 0
                var color = 0
                if (map.containsKey(item.groupUuId)) {
                    color = map[item.groupUuId]!!
                }
                val calendar1 = Calendar.getInstance()
                calendar1.timeInMillis = eventTime
                var mDay = calendar1.get(Calendar.DAY_OF_MONTH)
                var mMonth = calendar1.get(Calendar.MONTH)
                var mYear = calendar1.get(Calendar.YEAR)
                if (eventTime > 0) {
                    data.add(EventModel(item.viewType, item, mDay, mMonth, mYear, color))
                } else {
                    continue
                }
                if (isFeature) {
                    calendar1.timeInMillis = item.startDateTime
                    if (Reminder.isBase(mType, Reminder.BY_WEEK)) {
                        var days: Long = 0
                        var max = Configs.MAX_DAYS_COUNT
                        if (isLimited) {
                            max = limit - count
                        }
                        val list = item.weekdays
                        val baseTime = item.dateTime
                        do {
                            calendar1.timeInMillis = calendar1.timeInMillis + AlarmManager.INTERVAL_DAY
                            eventTime = calendar1.timeInMillis
                            if (eventTime == baseTime) {
                                continue
                            }
                            val weekDay = calendar1.get(Calendar.DAY_OF_WEEK)
                            if (list[weekDay - 1] == 1 && eventTime > 0) {
                                mDay = calendar1.get(Calendar.DAY_OF_MONTH)
                                mMonth = calendar1.get(Calendar.MONTH)
                                mYear = calendar1.get(Calendar.YEAR)
                                days++
                                data.add(EventModel(item.viewType,
                                        Reminder(item, true).apply {
                                            this.eventTime = TimeUtil.getGmtFromDateTime(eventTime)
                                        },
                                        mDay, mMonth, mYear, color))
                            }
                        } while (days < max)
                    } else if (Reminder.isBase(mType, Reminder.BY_MONTH)) {
                        var days: Long = 0
                        var max = Configs.MAX_DAYS_COUNT
                        if (isLimited) {
                            max = limit - count
                        }
                        val baseTime = item.dateTime
                        do {
                            eventTime = timeCount.getNextMonthDayTime(item)
                            calendar1.timeInMillis = eventTime
                            if (eventTime == baseTime) {
                                continue
                            }
                            mDay = calendar1.get(Calendar.DAY_OF_MONTH)
                            mMonth = calendar1.get(Calendar.MONTH)
                            mYear = calendar1.get(Calendar.YEAR)
                            if (eventTime > 0) {
                                days++
                                data.add(EventModel(item.viewType,
                                        Reminder(item, true).apply {
                                            this.eventTime = TimeUtil.getGmtFromDateTime(eventTime)
                                        },
                                        mDay, mMonth, mYear, color))
                            }
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
                            mDay = calendar1.get(Calendar.DAY_OF_MONTH)
                            mMonth = calendar1.get(Calendar.MONTH)
                            mYear = calendar1.get(Calendar.YEAR)
                            if (eventTime > 0) {
                                days++
                                data.add(EventModel(item.viewType,
                                        Reminder(item, true).apply {
                                            this.eventTime = TimeUtil.getGmtFromDateTime(eventTime)
                                        },
                                        mDay, mMonth, mYear, color))
                            }
                        } while (days < max)
                    }
                }
            }
        }
    }

    private fun notifyEnd(callback: Callback?, list: List<EventModel>) {
        if (callback != null) {
            removeCallback(callback)
            callback.apply(list)
        }
    }

    interface InitCallback {
        fun onFinish()
    }

    interface Callback {
        fun apply(list: List<EventModel>)
    }

    private inner class CancelableRunnable internal constructor(private val day: Int, private val month: Int, private val year: Int, private val sort: Boolean, private var callback: Callback?) : Runnable {
        @Volatile
        private var isCanceled: Boolean = false

        fun setCallback(callback: Callback?) {
            this.callback = callback
        }

        fun setCanceled(canceled: Boolean) {
            isCanceled = canceled
        }

        override fun run() {
            if (isCanceled) return
            val res = ArrayList<EventModel>()
            Timber.d("run: d->%d, m->%d, y->%d, data-> %s", day, month, year, data)
            for (item in ArrayList(data)) {
                if (item == null) continue
                val mDay = item.day
                val mMonth = item.month
                val mYear = item.year
                val type = item.viewType
                if (type == EventModel.BIRTHDAY && mDay == day && mMonth == month) {
                    res.add(item)
                } else {
                    if (mDay == day && mMonth == month && mYear == year) {
                        res.add(item)
                    }
                }
                if (isCanceled) break
            }
            Timber.d("run: %d", res.size)
            if (isCanceled) return
            if (!sort) {
                mHandler.post { notifyEnd(callback, res) }
                return
            }
            if (isCanceled) return
            res.sortWith(Comparator { eventsItem, t1 ->
                var time1: Long = 0
                var time2: Long = 0
                if (eventsItem.model is Birthday) {
                    val item = eventsItem.model as Birthday
                    val dateItem = TimeUtil.getFutureBirthdayDate(prefs, item.date)
                    if (dateItem != null) {
                        val calendar = dateItem.calendar
                        time1 = calendar.timeInMillis
                    }
                } else if (eventsItem.model is Reminder) {
                    val reminder = eventsItem.model as Reminder
                    time1 = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
                }
                if (t1.model is Birthday) {
                    val item = t1.model as Birthday
                    val dateItem = TimeUtil.getFutureBirthdayDate(prefs, item.date)
                    if (dateItem != null) {
                        val calendar = dateItem.calendar
                        time2 = calendar.timeInMillis
                    }
                } else if (t1.model is Reminder) {
                    val reminder = t1.model as Reminder
                    time2 = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
                }
                (time1 - time2).toInt()
            })
            if (isCanceled) return
            mHandler.post { notifyEnd(callback, res) }
        }
    }
}
