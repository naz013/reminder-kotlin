package com.elementary.tasks.day_view

import android.app.AlarmManager
import android.os.Bundle
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.core.calendar.InfinitePagerAdapter
import com.elementary.tasks.core.calendar.InfiniteViewPager
import com.elementary.tasks.core.utils.GlobalButtonObservable
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.view_models.day_view.DayViewViewModel
import com.elementary.tasks.databinding.FragmentDayViewBinding
import com.elementary.tasks.day_view.day.DayCallback
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.day_view.pager.DayPagerAdapter
import com.elementary.tasks.navigation.fragments.BaseCalendarFragment
import org.apache.commons.lang3.StringUtils
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
class DayViewFragment : BaseCalendarFragment<FragmentDayViewBinding>(), DayCallback {

    lateinit var dayPagerAdapter: DayPagerAdapter
    private val datePageChangeListener = DatePageChangeListener()
    private lateinit var viewModel: DayViewViewModel
    private var eventsPagerItem: EventsPagerItem? = null
    private var listener: ((EventsPagerItem, List<EventModel>) -> Unit)? = null

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.day_view_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_voice -> {
                buttonObservable.fireAction(view!!, GlobalButtonObservable.Action.VOICE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun layoutRes(): Int = R.layout.fragment_day_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.setOnClickListener { showActionDialog(false) }

        initPager()
        initViewModel()

        loadData()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this,
                DayViewViewModel.Factory(prefs.isFutureEventEnabled, TimeUtil.getBirthdayTime(prefs.birthdayTime)))
                .get(DayViewViewModel::class.java)
        viewModel.events.observe(this, Observer<Pair<EventsPagerItem, List<EventModel>>> {
            val item = eventsPagerItem
            if (it != null && item != null) {
                val foundItem = it.first
                val foundList = it.second
                if (foundItem == item) {
                    listener?.invoke(foundItem, foundList)
                }
            }
        })
    }

    private fun initPager() {
        dayPagerAdapter = DayPagerAdapter(childFragmentManager)
        binding.pager.adapter = InfinitePagerAdapter(dayPagerAdapter)
    }

    private fun updateMenuTitles(): String {
        val mills = if (dateMills != 0L) dateMills else System.currentTimeMillis()
        val monthTitle = StringUtils.capitalize(DateUtils.formatDateTime(activity, mills, MONTH_YEAR_FLAG).toString())
        callback?.onTitleChange(monthTitle)
        return monthTitle
    }

    override fun getTitle(): String = updateMenuTitles()

    private fun loadData() {
        if (dateMills != 0L) {
            showEvents(dateMills)
        } else {
            showEvents(System.currentTimeMillis())
        }
    }

    private fun fromMills(mills: Long): EventsPagerItem {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = mills
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        return EventsPagerItem(day, month, year)
    }

    private fun showEvents(date: Long) {
        dateMills = date
        updateMenuTitles()

        datePageChangeListener.setCurrentDateTime(dateMills)
        binding.pager.isEnabled = true
        binding.pager.addOnPageChangeListener(datePageChangeListener)
        binding.pager.currentItem = InfiniteViewPager.OFFSET + 1
    }

    override fun getViewModel(): DayViewViewModel {
        return viewModel
    }

    override fun find(eventsPagerItem: EventsPagerItem, listener: ((EventsPagerItem, List<EventModel>) -> Unit)?) {
        this.eventsPagerItem = eventsPagerItem
        this.listener = listener
        viewModel.findEvents(eventsPagerItem)
    }

    private inner class DatePageChangeListener : ViewPager.OnPageChangeListener {

        var currentPage = InfiniteViewPager.OFFSET + 1
            private set
        private var currentDateTime: Long = System.currentTimeMillis()

        internal fun setCurrentDateTime(dateTime: Long) {
            this.currentDateTime = dateTime
        }

        private fun getNext(position: Int): Int {
            return (position + 1) % NUMBER_OF_PAGES
        }

        private fun getPrevious(position: Int): Int {
            return (position + 3) % NUMBER_OF_PAGES
        }

        internal fun getCurrent(position: Int): Int {
            return position % NUMBER_OF_PAGES
        }

        override fun onPageScrollStateChanged(position: Int) {

        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {

        }

        private fun refreshAdapters(position: Int) {
            val currentFragment = dayPagerAdapter.fragments[getCurrent(position)]
            val prevFragment = dayPagerAdapter.fragments[getPrevious(position)]
            val nextFragment = dayPagerAdapter.fragments[getNext(position)]
            when {
                position == currentPage -> {
                    currentFragment.setModel(fromMills(currentDateTime))
                    prevFragment.setModel(fromMills(currentDateTime - AlarmManager.INTERVAL_DAY))
                    nextFragment.setModel(fromMills(currentDateTime + AlarmManager.INTERVAL_DAY))
                }
                position > currentPage -> {
                    currentDateTime += AlarmManager.INTERVAL_DAY
                    nextFragment.setModel(fromMills(currentDateTime + AlarmManager.INTERVAL_DAY))
                }
                else -> {
                    currentDateTime -= AlarmManager.INTERVAL_DAY
                    prevFragment.setModel(fromMills(currentDateTime - AlarmManager.INTERVAL_DAY))
                }
            }
            currentPage = position
        }

        override fun onPageSelected(position: Int) {
            refreshAdapters(position)
            dayPagerAdapter.fragments[getCurrent(position)].requestData()
            val item = dayPagerAdapter.fragments[getCurrent(position)].getModel() ?: return
            val calendar = Calendar.getInstance()
            calendar.set(item.year, item.month, item.day)
            dateMills = calendar.timeInMillis
            updateMenuTitles()
        }
    }

    companion object {

        private const val DATE_KEY = "date"
        private const val POS_KEY = "position"
        private const val NUMBER_OF_PAGES = 4
        const val MONTH_YEAR_FLAG = (DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR)

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
