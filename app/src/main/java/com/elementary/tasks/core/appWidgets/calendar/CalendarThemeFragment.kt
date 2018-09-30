package com.elementary.tasks.core.appWidgets.calendar

import android.content.Context
import android.graphics.Color
import android.os.BadParcelableException
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.calendar.FlextHelper
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import hirondelle.date4j.DateTime
import kotlinx.android.synthetic.main.fragment_calendar_widget_preview.*
import kotlinx.android.synthetic.main.widget_calendar.view.*
import java.text.SimpleDateFormat
import java.util.*

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
class CalendarThemeFragment : BaseNavigationFragment() {
    private var pageNumber: Int = 0
    private var list: List<CalendarTheme> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = arguments
        try {
            pageNumber = intent?.getInt(ARGUMENT_PAGE_NUMBER) ?: 0
            val list = intent?.getParcelableArrayList<CalendarTheme>(ARGUMENT_DATA)
            if (list != null) this.list = list
        } catch (e: BadParcelableException) {
        }
    }

    override fun layoutRes(): Int = R.layout.fragment_calendar_widget_preview

    override fun getTitle(): String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val calendarTheme = list[pageNumber]
        val windowColor = calendarTheme.windowColor
        previewView.widgetBg.setBackgroundResource(windowColor)
        val windowTextColor = calendarTheme.windowTextColor
        themeTitle.setTextColor(windowTextColor)
        note.setTextColor(windowTextColor)

        val itemTextColor = calendarTheme.itemTextColor
        val widgetBgColor = calendarTheme.widgetBgColor
        val headerColor = calendarTheme.headerColor
        val borderColor = calendarTheme.borderColor
        val titleColor = calendarTheme.titleColor
        val rowColor = calendarTheme.rowColor

        val leftArrow = calendarTheme.leftArrow
        val rightArrow = calendarTheme.rightArrow
        val iconPlus = calendarTheme.iconPlus
        val iconVoice = calendarTheme.iconVoice
        val iconSettings = calendarTheme.iconSettings

        val currentMark = calendarTheme.currentMark
        val birthdayMark = calendarTheme.birthdayMark
        val reminderMark = calendarTheme.reminderMark

        previewView.weekdayGrid.setBackgroundResource(widgetBgColor)
        previewView.header.setBackgroundResource(headerColor)
        previewView.currentDate.setTextColor(titleColor)
        previewView.monthGrid.setBackgroundResource(borderColor)

        previewView.plusButton.setImageResource(iconPlus)
        previewView.nextMonth.setImageResource(rightArrow)
        previewView.prevMonth.setImageResource(leftArrow)
        previewView.voiceButton.setImageResource(iconVoice)
        previewView.settingsButton.setImageResource(iconSettings)

        val monthYearStringBuilder = StringBuilder(50)
        val monthYearFormatter = Formatter(monthYearStringBuilder, Locale.getDefault())
        val monthYearFlag = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR
        val cal = GregorianCalendar()
        val monthTitle = DateUtils.formatDateRange(activity,
                monthYearFormatter, cal.timeInMillis, cal.timeInMillis, monthYearFlag).toString()
        previewView.currentDate.text = monthTitle.toUpperCase()

        themeTitle.text = calendarTheme.title

        previewView.weekdayGrid.adapter = WeekdayAdapter(activity!!, itemTextColor)
        previewView.monthGrid.adapter = MonthGridAdapter(activity!!, intArrayOf(itemTextColor, rowColor, currentMark, birthdayMark, reminderMark))
    }

    private inner class WeekdayAdapter internal constructor(private val context: Context, private val textColor: Int) : BaseAdapter() {

        private val weekdays: MutableList<String>
        private val sunday = 1
        private val startDayOfWeek = sunday
        private var inflater: LayoutInflater? = null

        init {
            inflater = LayoutInflater.from(context)
            weekdays = ArrayList()
            weekdays.clear()
            val fmt = SimpleDateFormat("EEE", Locale.getDefault())

            // 17 Feb 2013 is Sunday
            val sunday = DateTime(2013, 2, 17, 0, 0, 0, 0)
            var nextDay = sunday.plusDays(startDayOfWeek - this.sunday)
            if (prefs!!.startDay == 1) {
                nextDay = nextDay.plusDays(1)
            }
            for (i in 0..6) {
                val date = FlextHelper.convertDateTimeToDate(nextDay)
                weekdays.add(fmt.format(date).toUpperCase())
                nextDay = nextDay.plusDays(1)
            }
        }

        override fun getCount(): Int {
            return weekdays.size
        }

        override fun getItem(position: Int): Any {
            return weekdays[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var cView = convertView
            if (cView == null) {
                inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                cView = inflater!!.inflate(R.layout.list_item_weekday_grid, null)
            }
            val textView = cView!!.findViewById<TextView>(R.id.textView1)
            textView.text = weekdays[position]
            textView.setTextColor(textColor)
            return cView
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun hasStableIds(): Boolean {
            return true
        }
    }

    private inner class MonthGridAdapter internal constructor(internal var context: Context, resources: IntArray) : BaseAdapter() {

        internal var datetimeList: MutableList<DateTime>
        internal var startDayOfWeek = 1
        internal var prefsMonth: Int = 0
        internal var inflater: LayoutInflater
        internal var textColor: Int = 0
        internal var widgetBgColor: Int = 0
        internal var cMark: Int = 0
        internal var bMark: Int = 0
        internal var rMark: Int = 0

        init {
            this.textColor = resources[0]
            this.widgetBgColor = resources[1]
            this.cMark = resources[2]
            this.bMark = resources[3]
            this.rMark = resources[4]
            inflater = LayoutInflater.from(context)
            datetimeList = ArrayList()
            datetimeList.clear()

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            val year = calendar.get(Calendar.YEAR)

            val firstDateOfMonth = DateTime(year, prefsMonth + 1, 1, 0, 0, 0, 0)
            val lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.numDaysInMonth - 1)

            var weekdayOfFirstDate = firstDateOfMonth.weekDay!!
            if (weekdayOfFirstDate < startDayOfWeek) {
                weekdayOfFirstDate += 7
            }
            while (weekdayOfFirstDate > 0) {
                var temp = startDayOfWeek
                if (prefs.startDay == 1) {
                    temp = startDayOfWeek + 1
                }
                val dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate - temp)
                if (!dateTime.lt(firstDateOfMonth)) {
                    break
                }
                datetimeList.add(dateTime)
                weekdayOfFirstDate--
            }
            for (i in 0 until lastDateOfMonth.day) {
                datetimeList.add(firstDateOfMonth.plusDays(i))
            }
            var endDayOfWeek = startDayOfWeek - 1
            if (endDayOfWeek == 0) {
                endDayOfWeek = 7
            }
            if (lastDateOfMonth.weekDay != endDayOfWeek) {
                var i = 1
                while (true) {
                    val nextDay = lastDateOfMonth.plusDays(i)
                    datetimeList.add(nextDay)
                    i++
                    if (nextDay.weekDay == endDayOfWeek) {
                        break
                    }
                }
            }
            val size = datetimeList.size
            val numOfDays = 42 - size
            val lastDateTime = datetimeList[size - 1]
            for (i in 1..numOfDays) {
                val nextDateTime = lastDateTime.plusDays(i)
                datetimeList.add(nextDateTime)
            }
        }

        override fun getCount(): Int {
            return datetimeList.size
        }

        override fun getItem(position: Int): Any {
            return datetimeList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var cView = convertView
            if (cView == null) {
                inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                cView = inflater.inflate(R.layout.view_month_grid, null)
            }
            val selDay = datetimeList[position].day!!
            val background = cView!!.findViewById<FrameLayout>(R.id.background)
            val textView = cView.findViewById<TextView>(R.id.textView)
            val currentMark = cView.findViewById<TextView>(R.id.currentMark)
            val reminderMark = cView.findViewById<TextView>(R.id.reminderMark)
            val birthdayMark = cView.findViewById<TextView>(R.id.birthdayMark)

            textView.text = selDay.toString()
            textView.setTextColor(textColor)
            background.setBackgroundResource(widgetBgColor)

            currentMark.setBackgroundColor(Color.TRANSPARENT)
            reminderMark.setBackgroundColor(Color.TRANSPARENT)
            birthdayMark.setBackgroundColor(Color.TRANSPARENT)
            if (selDay == 15) {
                if (rMark != 0) {
                    reminderMark.setBackgroundResource(rMark)
                } else {
                    reminderMark.setBackgroundColor(context.resources
                            .getColor(themeUtil!!.colorReminderCalendar()))
                }
            }
            if (selDay == 11) {
                if (bMark != 0) {
                    birthdayMark.setBackgroundResource(bMark)
                } else {
                    birthdayMark.setBackgroundColor(context.resources
                            .getColor(themeUtil!!.colorBirthdayCalendar()))
                }
            }
            if (11 == selDay) {
                if (cMark != 0) {
                    currentMark.setBackgroundResource(cMark)
                } else {
                    currentMark.setBackgroundColor(context.resources
                            .getColor(themeUtil!!.colorCurrentCalendar()))
                }
            }
            return cView
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun hasStableIds(): Boolean {
            return true
        }
    }

    companion object {

        internal const val ARGUMENT_PAGE_NUMBER = "arg_page_number"
        internal const val ARGUMENT_DATA = "arg_data"

        fun newInstance(page: Int, list: List<CalendarTheme>): CalendarThemeFragment {
            val pageFragment = CalendarThemeFragment()
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_PAGE_NUMBER, page)
            arguments.putParcelableArrayList(ARGUMENT_DATA, ArrayList(list))
            pageFragment.arguments = arguments
            return pageFragment
        }
    }
}
