package com.elementary.tasks.navigation.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.EventsDataProvider
import com.elementary.tasks.core.calendar.CalendarSingleton
import com.elementary.tasks.core.calendar.FlextCalendarFragment
import com.elementary.tasks.core.calendar.FlextListener
import com.elementary.tasks.core.utils.LogUtil
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
class CalendarFragment : BaseCalendarFragment() {

    private val listener = object : FlextListener {
        override fun onClickDate(date: Date) {
            LogUtil.d(TAG, "onClick: $date")
            saveTime(date)
            replaceFragment(DayViewFragment.newInstance(dateMills, 0), "")
        }

        override fun onLongClickDate(date: Date) {
            LogUtil.d(TAG, "onLongClickDate: $date")
            saveTime(date)
            showActionDialog(true)
        }

        override fun onMonthChanged(month: Int, year: Int) {}

        override fun onViewCreated() {}

        override fun onMonthSelected(month: Int) {}
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.day_view_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_voice -> {

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getString(R.string.calendar))
            callback?.onFragmentSelect(this)
            CalendarSingleton.getInstance().fabClick = View.OnClickListener {
                dateMills = System.currentTimeMillis()
                showActionDialog(false)
            }
        }
        showCalendar()
        LogUtil.d(TAG, "onResume: ")
    }

    private fun showCalendar() {
        val calendarView = FlextCalendarFragment()
        val args = Bundle()
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        args.putInt(FlextCalendarFragment.MONTH, cal.get(Calendar.MONTH) + 1)
        args.putInt(FlextCalendarFragment.YEAR, cal.get(Calendar.YEAR))
        if (prefs.startDay == 0) {
            args.putInt(FlextCalendarFragment.START_DAY_OF_WEEK, FlextCalendarFragment.SUNDAY)
        } else {
            args.putInt(FlextCalendarFragment.START_DAY_OF_WEEK, FlextCalendarFragment.MONDAY)
        }
        args.putBoolean(FlextCalendarFragment.DARK_THEME, themeUtil.isDark)
        args.putBoolean(FlextCalendarFragment.ENABLE_IMAGES, prefs.isCalendarImagesEnabled)
        val monthImage = prefs.calendarImages
        args.putLongArray(FlextCalendarFragment.MONTH_IMAGES, monthImage.photos)
        calendarView.arguments = args
        calendarView.setListener(listener)
        calendarView.refreshView()
        replaceFragment(calendarView, getString(R.string.calendar))
        val isReminder = prefs.isRemindersInCalendarEnabled
        val isFeature = prefs.isFutureEventEnabled
        CalendarSingleton.getInstance().provider = EventsDataProvider(context!!, isReminder, isFeature)
        activity!!.invalidateOptionsMenu()
    }

    private fun saveTime(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        dateMills = calendar.timeInMillis
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BaseCalendarFragment.REMINDER_CODE || requestCode == BaseCalendarFragment.BD_CODE && resultCode == Activity.RESULT_OK) {
            showCalendar()
        }
    }

    companion object {

        private const val TAG = "CalendarFragment"
    }
}
