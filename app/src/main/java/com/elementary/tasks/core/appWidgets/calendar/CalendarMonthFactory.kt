package com.elementary.tasks.core.appWidgets.calendar

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService

import com.elementary.tasks.R
import com.elementary.tasks.core.appWidgets.WidgetDataProvider
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeUtil

import java.util.ArrayList
import java.util.Calendar

import hirondelle.date4j.DateTime

/**
 * Copyright 2015 Nazar Suhovich
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
class CalendarMonthFactory internal constructor(private val mContext: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private val mDateTimeList = ArrayList<DateTime>()
    private val mPagerData = ArrayList<WidgetItem>()
    private val mWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    private var mDay: Int = 0
    private var mMonth: Int = 0
    private var mYear: Int = 0

    override fun onCreate() {
        mDateTimeList.clear()
        mPagerData.clear()
    }

    override fun onDataSetChanged() {
        mDateTimeList.clear()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        val sp = mContext.getSharedPreferences(CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE)
        val prefsMonth = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + mWidgetId, 0)

        mYear = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_YEAR + mWidgetId, calendar.get(Calendar.YEAR))
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
        mMonth = prefsMonth + 1

        val firstDateOfMonth = DateTime(mYear, prefsMonth + 1, 1, 0, 0, 0, 0)
        val lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.numDaysInMonth - 1)

        var weekdayOfFirstDate = firstDateOfMonth.weekDay!!
        val startDayOfWeek = Prefs.getInstance(mContext).startDay + 1

        if (weekdayOfFirstDate < startDayOfWeek) {
            weekdayOfFirstDate += 7
        }

        while (weekdayOfFirstDate > 0) {
            val dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate - startDayOfWeek)
            if (!dateTime.lt(firstDateOfMonth)) {
                break
            }
            mDateTimeList.add(dateTime)
            weekdayOfFirstDate--
        }
        for (i in 0 until lastDateOfMonth.day) {
            mDateTimeList.add(firstDateOfMonth.plusDays(i))
        }
        var endDayOfWeek = startDayOfWeek - 1
        if (endDayOfWeek == 0) {
            endDayOfWeek = 7
        }
        if (lastDateOfMonth.weekDay != endDayOfWeek) {
            var i = 1
            while (true) {
                val nextDay = lastDateOfMonth.plusDays(i)
                mDateTimeList.add(nextDay)
                i++
                if (nextDay.weekDay == endDayOfWeek) {
                    break
                }
            }
        }
        val size = mDateTimeList.size
        val numOfDays = 42 - size
        val lastDateTime = mDateTimeList[size - 1]
        for (i in 1..numOfDays) {
            val nextDateTime = lastDateTime.plusDays(i)
            mDateTimeList.add(nextDateTime)
        }
        showEvents()
    }

    private fun showEvents() {
        val calendar = Calendar.getInstance()
        val sPrefs = Prefs.getInstance(mContext)
        calendar.timeInMillis = TimeUtil.getBirthdayTime(sPrefs.birthdayTime)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val isFeature = sPrefs.isFutureEventEnabled
        val isRemindersEnabled = sPrefs.isRemindersInCalendarEnabled

        val provider = WidgetDataProvider(mContext)
        provider.setTime(hour, minute)
        if (isRemindersEnabled) {
            provider.setFeature(isFeature)
        }
        provider.fillArray()
        mPagerData.clear()

        calendar.timeInMillis = System.currentTimeMillis()
        var currentDay: Int
        var currentMonth: Int
        var currentYear: Int
        var position = 0
        do {
            currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            currentMonth = calendar.get(Calendar.MONTH)
            currentYear = calendar.get(Calendar.YEAR)
            val hasReminders = provider.hasReminder(currentDay, currentMonth, currentYear)
            val hasBirthdays = provider.hasBirthday(currentDay, currentMonth)
            mPagerData.add(WidgetItem(currentDay, currentMonth, currentYear,
                    hasReminders, hasBirthdays))
            position++
            calendar.timeInMillis = calendar.timeInMillis + AlarmManager.INTERVAL_DAY
        } while (position < Configs.MAX_DAYS_COUNT)
    }

    override fun onDestroy() {
        mDateTimeList.clear()
        mPagerData.clear()
    }

    override fun getCount(): Int {
        return mDateTimeList.size
    }

    override fun getViewAt(i: Int): RemoteViews {
        val sp = mContext.getSharedPreferences(
                CalendarWidgetConfig.CALENDAR_WIDGET_PREF, Context.MODE_PRIVATE)
        val theme = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_THEME + mWidgetId, 0)
        val calendarTheme = CalendarTheme.getThemes(mContext)[theme]
        val itemTextColor = calendarTheme.itemTextColor
        val rowColor = calendarTheme.rowColor
        val reminderM = calendarTheme.reminderMark
        val birthdayM = calendarTheme.birthdayMark
        val currentM = calendarTheme.currentMark
        val prefsMonth = sp.getInt(CalendarWidgetConfig.CALENDAR_WIDGET_MONTH + mWidgetId, 0)
        val rView = RemoteViews(mContext.packageName, R.layout.view_month_grid)

        val cs = ThemeUtil.getInstance(mContext)

        val selDay = mDateTimeList[i].day!!
        val selMonth = mDateTimeList[i].month!!
        val selYear = mDateTimeList[i].year!!

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val realMonth = calendar.get(Calendar.MONTH)
        val realYear = calendar.get(Calendar.YEAR)

        rView.setTextViewText(R.id.textView, selDay.toString())
        if (selMonth == prefsMonth + 1) {
            rView.setTextColor(R.id.textView, itemTextColor)
        } else {
            rView.setTextColor(R.id.textView, mContext.resources.getColor(R.color.material_grey))
        }
        rView.setInt(R.id.background, "setBackgroundResource", rowColor)

        rView.setInt(R.id.currentMark, "setBackgroundColor", Color.TRANSPARENT)
        rView.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT)
        rView.setInt(R.id.birthdayMark, "setBackgroundColor", Color.TRANSPARENT)

        if (mPagerData.size > 0) {
            for (item in mPagerData) {
                val day = item.day
                val month = item.month + 1
                val year = item.year
                if (day == selDay && month == selMonth) {
                    if (item.isHasReminders && year == selYear) {
                        if (reminderM != 0) {
                            rView.setInt(R.id.reminderMark, "setBackgroundResource", reminderM)
                        } else {
                            rView.setInt(R.id.reminderMark, "setBackgroundColor",
                                    mContext.resources.getColor(cs.colorReminderCalendar()))
                        }
                    } else {
                        rView.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT)
                    }
                    if (item.isHasBirthdays) {
                        if (birthdayM != 0) {
                            rView.setInt(R.id.birthdayMark, "setBackgroundResource", birthdayM)
                        } else {
                            rView.setInt(R.id.birthdayMark, "setBackgroundColor",
                                    mContext.resources.getColor(cs.colorBirthdayCalendar()))
                        }
                    } else {
                        rView.setInt(R.id.birthdayMark, "setBackgroundColor", Color.TRANSPARENT)
                    }
                    break
                }
            }
        }

        if (mDay == selDay && mMonth == selMonth && mYear == realYear && mMonth == realMonth + 1
                && mYear == selYear) {
            if (currentM != 0) {
                rView.setInt(R.id.currentMark, "setBackgroundResource", currentM)
            } else {
                rView.setInt(R.id.currentMark, "setBackgroundColor",
                        mContext.resources.getColor(cs.colorCurrentCalendar()))
            }
        } else {
            rView.setInt(R.id.currentMark, "setBackgroundColor", Color.TRANSPARENT)
        }

        calendar.timeInMillis = System.currentTimeMillis()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        calendar.set(Calendar.MONTH, selMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, selDay)
        calendar.set(Calendar.YEAR, selYear)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        val dateMills = calendar.timeInMillis

        val fillInIntent = Intent()
        fillInIntent.putExtra("date", dateMills)
        rView.setOnClickFillInIntent(R.id.textView, fillInIntent)
        return rView
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    private class WidgetItem internal constructor(var day: Int, var month: Int, var year: Int, internal val isHasReminders: Boolean, internal val isHasBirthdays: Boolean)
}