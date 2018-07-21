package com.elementary.tasks.core.appWidgets

import android.app.AlarmManager
import android.content.Context

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

class WidgetDataProvider(private val mContext: Context) {

    private val data: MutableList<Item>
    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var hour: Int = 0
    private var minute: Int = 0
    private var isFeature: Boolean = false

    enum class WidgetType {
        BIRTHDAY,
        REMINDER
    }

    init {
        data = ArrayList()
    }

    fun setTime(hour: Int, minute: Int) {
        this.hour = hour
        this.minute = minute
    }

    fun setFeature(isFeature: Boolean) {
        this.isFeature = isFeature
    }

    fun getData(): List<Item> {
        return data
    }

    fun getItem(position: Int): Item {
        return data[position]
    }

    fun hasReminder(day: Int, month: Int, year: Int): Boolean {
        var res = false
        for (item in data) {
            if (res) {
                break
            }
            val mDay = item.day
            val mMonth = item.month
            val mYear = item.year
            val type = item.type
            res = mDay == day && mMonth == month && mYear == year && type == WidgetType.REMINDER
        }
        return res
    }

    fun hasBirthday(day: Int, month: Int): Boolean {
        var res = false
        for (item in data) {
            val mDay = item.day
            val mMonth = item.month
            val type = item.type
            if (mDay == day && mMonth == month && type == WidgetType.BIRTHDAY) {
                res = true
                break
            }
        }
        return res
    }

    fun fillArray() {
        data.clear()
        loadBirthdays()
        loadReminders()
    }

    private fun loadReminders() {
        val reminderItems = AppDb.getAppDatabase(mContext).reminderDao().getAll(true, false)
        for (item in reminderItems) {
            val mType = item.type
            var eventTime = item.dateTime
            if (!Reminder.isGpsType(item.type) && eventTime > 0) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = eventTime
                var mDay = calendar.get(Calendar.DAY_OF_MONTH)
                var mMonth = calendar.get(Calendar.MONTH)
                var mYear = calendar.get(Calendar.YEAR)
                data.add(Item(mDay, mMonth, mYear, WidgetType.REMINDER))
                val repeatTime = item.repeatInterval
                val limit = item.repeatLimit.toLong()
                val count = item.eventCount
                val isLimited = limit > 0
                if (isFeature) {
                    val calendar1 = Calendar.getInstance()
                    calendar1.timeInMillis = eventTime
                    if (Reminder.isBase(mType, Reminder.BY_WEEK)) {
                        var days: Long = 0
                        var max = Configs.MAX_DAYS_COUNT
                        if (isLimited) {
                            max = limit - count
                        }
                        val list = item.weekdays
                        do {
                            calendar1.timeInMillis = calendar1.timeInMillis + AlarmManager.INTERVAL_DAY
                            val weekDay = calendar1.get(Calendar.DAY_OF_WEEK)
                            if (list[weekDay - 1] == 1) {
                                val sDay = calendar1.get(Calendar.DAY_OF_MONTH)
                                val sMonth = calendar1.get(Calendar.MONTH)
                                val sYear = calendar1.get(Calendar.YEAR)
                                days++
                                data.add(Item(sDay, sMonth, sYear, WidgetType.REMINDER))
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
                            eventTime = TimeCount.getInstance(mContext).getNextMonthDayTime(item)
                            calendar1.timeInMillis = eventTime
                            days++
                            val sDay = calendar1.get(Calendar.DAY_OF_MONTH)
                            val sMonth = calendar1.get(Calendar.MONTH)
                            val sYear = calendar1.get(Calendar.YEAR)
                            data.add(Item(sDay, sMonth, sYear, WidgetType.REMINDER))
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
                            mDay = calendar1.get(Calendar.DAY_OF_MONTH)
                            mMonth = calendar1.get(Calendar.MONTH)
                            mYear = calendar1.get(Calendar.YEAR)
                            days++
                            data.add(Item(mDay, mMonth, mYear, WidgetType.REMINDER))
                        } while (days < max)
                    }
                }
            }
        }
    }

    private fun loadBirthdays() {
        val list = AppDb.getAppDatabase(mContext).birthdaysDao().all()
        for (item in list) {
            var date: Date? = null
            try {
                date = format.parse(item.date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (date != null) {
                val calendar1 = Calendar.getInstance()
                calendar1.time = date
                val bDay = calendar1.get(Calendar.DAY_OF_MONTH)
                val bMonth = calendar1.get(Calendar.MONTH)
                calendar1.timeInMillis = System.currentTimeMillis()
                calendar1.set(Calendar.MONTH, bMonth)
                calendar1.set(Calendar.DAY_OF_MONTH, bDay)
                calendar1.set(Calendar.HOUR_OF_DAY, hour)
                calendar1.set(Calendar.MINUTE, minute)
                data.add(Item(bDay, bMonth, 0, WidgetType.BIRTHDAY))
            }
        }
    }

    class Item(var day: Int, var month: Int, var year: Int, val type: WidgetType)
}
