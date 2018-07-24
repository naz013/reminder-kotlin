package com.elementary.tasks.navigation.fragments

import android.app.AlarmManager
import android.os.Bundle
import android.view.*
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.CalendarPagerAdapter
import com.elementary.tasks.birthdays.EventsPagerItem
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.TimeUtil
import kotlinx.android.synthetic.main.fragment_day_view.*
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
class DayViewFragment : BaseCalendarFragment() {

    private val pagerData = ArrayList<EventsPagerItem>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = arguments
        if (intent != null) {
            dateMills = intent.getLong(DATE_KEY, 0)
        }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_day_view, container, false)
    }

    private fun updateMenuTitles() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        if (dateMills != 0L) {
            calendar.timeInMillis = dateMills
        }
        val dayString = TimeUtil.getDate(calendar.timeInMillis)
        if (callback != null) callback?.onTitleChange(dayString)
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getString(R.string.calendar))
            callback?.onFragmentSelect(this)
            callback?.onMenuSelect(R.id.nav_day_view)
        }
        loadData()
    }

    private fun loadData() {
        initProvider()
        val calendar = Calendar.getInstance()
        if (dateMills != 0L) {
            calendar.timeInMillis = dateMills
            showEvents(calendar.time)
        } else {
            calendar.timeInMillis = System.currentTimeMillis()
            showEvents(calendar.time)
        }
    }

    private fun showEvents(date: Date) {
        pagerData.clear()
        val calendar = Calendar.getInstance()
        calendar.time = date
        val targetDay = calendar.get(Calendar.DAY_OF_MONTH)
        val targetMonth = calendar.get(Calendar.MONTH)
        val targetYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = System.currentTimeMillis()

        var position = 0
        var targetPosition = -1
        calendar.add(Calendar.DAY_OF_MONTH, -100)
        while (position < Configs.MAX_DAYS_COUNT + 100) {
            val mDay = calendar.get(Calendar.DAY_OF_MONTH)
            val mMonth = calendar.get(Calendar.MONTH)
            val mYear = calendar.get(Calendar.YEAR)
            if (mDay == targetDay && mMonth == targetMonth && mYear == targetYear) {
                targetPosition = position
                pagerData.add(EventsPagerItem(position, 1, mDay, mMonth, mYear))
            } else {
                pagerData.add(EventsPagerItem(position, 0, mDay, mMonth, mYear))
            }
            position++
            calendar.timeInMillis = calendar.timeInMillis + AlarmManager.INTERVAL_DAY
        }
        val pagerAdapter = CalendarPagerAdapter(if (Module.isJellyMR2) childFragmentManager else fragmentManager!!, pagerData)
        try {
            pager.adapter = pagerAdapter
        } catch (ignored: IllegalStateException) {
        }

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i2: Int) {

            }

            override fun onPageSelected(i: Int) {
                val item = pagerData[i]
                val calendar1 = Calendar.getInstance()
                calendar1.set(item.year, item.month, item.day)
                dateMills = calendar1.timeInMillis
                updateMenuTitles()
            }

            override fun onPageScrollStateChanged(i: Int) {

            }
        })
        pager.currentItem = targetPosition
        updateMenuTitles()
    }

    companion object {

        private const val DATE_KEY = "date"
        private const val POS_KEY = "position"

        fun newInstance(date: Long, position: Int): DayViewFragment {
            val pageFragment = DayViewFragment()
            val arguments = Bundle()
            arguments.putLong(DATE_KEY, date)
            arguments.putInt(POS_KEY, position)
            pageFragment.arguments = arguments
            return pageFragment
        }
    }
}
