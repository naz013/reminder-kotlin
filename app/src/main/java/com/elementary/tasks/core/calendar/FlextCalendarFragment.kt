package com.elementary.tasks.core.calendar

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.EventsDataProvider
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.views.MonthView
import com.elementary.tasks.databinding.FragmentFlextCalBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment

import org.apache.commons.lang3.StringUtils

import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Formatter
import java.util.GregorianCalendar
import java.util.HashMap
import java.util.Locale
import java.util.TimeZone

import androidx.viewpager.widget.ViewPager
import hirondelle.date4j.DateTime
import timber.log.Timber

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
class FlextCalendarFragment : BaseNavigationFragment(), EventsDataProvider.Callback {

    /**
     * First day of month time
     */
    private val firstMonthTime = GregorianCalendar()

    /**
     * Reuse formatter to print "MMMM yyyy" format
     */
    private val monthYearStringBuilder = StringBuilder(50)
    private val monthYearFormatter = Formatter(
            monthYearStringBuilder, Locale.getDefault())

    protected var month = -1
    protected var year = -1

    protected var enableImage = true
    protected var isDark = true

    /**
     * Declare views
     */
    var fragments: List<DateGridFragment>? = null
        private set

    /**
     * caldroidData belongs to Caldroid
     */
    private val mLastMap = HashMap<DateTime, Events>()

    private val photosList = longArrayOf(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1)

    protected var startDayOfWeek = SUNDAY

    private var dateItemClickListener: MonthView.OnDateClick? = null
    private var dateItemLongClickListener: MonthView.OnDateLongClick? = null

    private var caldroidListener: FlextListener? = null
    private var binding: FragmentFlextCalBinding? = null
    private var pageChangeListener: DatePageChangeListener? = null

    val newWeekdayAdapter: WeekdayArrayAdapter
        get() = WeekdayArrayAdapter(activity, android.R.layout.simple_list_item_1,
                daysOfWeek, isDark)

    protected val daysOfWeek: ArrayList<String>
        get() {
            val list = ArrayList<String>()
            val fmt = SimpleDateFormat("EEE", Locale.getDefault())
            val sunday = DateTime(2013, 2, 17, 0, 0, 0, 0)
            var nextDay = sunday.plusDays(startDayOfWeek - SUNDAY)
            for (i in 0..6) {
                val date = FlextHelper.convertDateTimeToDate(nextDay)
                list.add(fmt.format(date).toUpperCase())
                nextDay = nextDay.plusDays(1)
            }
            return list
        }

    fun setListener(caldroidListener: FlextListener) {
        this.caldroidListener = caldroidListener
    }

    fun setCalendarDateTime(dateTime: DateTime?) {
        month = dateTime!!.month!!
        year = dateTime.year!!
        if (caldroidListener != null) {
            caldroidListener!!.onMonthChanged(month, year)
        }
        refreshView()
    }

    private fun getDateItemClickListener(): MonthView.OnDateClick {
        if (dateItemClickListener == null) {
            dateItemClickListener = { dateTime ->
                if (caldroidListener != null) {
                    val date = FlextHelper.convertDateTimeToDate(dateTime)
                    caldroidListener!!.onClickDate(date)
                }
            }
        }
        return dateItemClickListener
    }

    private fun getDateItemLongClickListener(): MonthView.OnDateLongClick {
        if (dateItemLongClickListener == null) {
            dateItemLongClickListener = { dateTime ->
                if (caldroidListener != null) {
                    val date = FlextHelper.convertDateTimeToDate(dateTime)
                    caldroidListener!!.onLongClickDate(date)
                }
            }
        }
        return dateItemLongClickListener
    }

    protected fun refreshMonthTitleTextView() {
        firstMonthTime.set(Calendar.YEAR, year)
        firstMonthTime.set(Calendar.MONTH, month - 1)
        firstMonthTime.set(Calendar.DAY_OF_MONTH, 1)
        val millis = firstMonthTime.timeInMillis
        monthYearStringBuilder.setLength(0)
        val monthTitle = DateUtils.formatDateRange(activity,
                monthYearFormatter, millis, millis, MONTH_YEAR_FLAG).toString()
        binding!!.monthYear.text = StringUtils.capitalize(monthTitle)
        if (caldroidListener != null) {
            caldroidListener!!.onMonthSelected(month)
        }
        if (binding!!.imageView != null && enableImage) {
            val check = ImageCheck.getInstance()
            if (check.isImage(month - 1, photosList[month - 1])) {
                Glide.with(binding!!.imageView)
                        .load(File(check.getImage(month - 1, photosList[month - 1])!!))
                        .into(binding!!.imageView)
            } else {
                LoadAsync(activity, month - 1, photosList[month - 1]).execute()
            }
        }
    }

    fun refreshView() {
        if (month == -1 || year == -1) {
            return
        }
        refreshMonthTitleTextView()
    }

    protected fun retrieveInitialArgs() {
        val args = arguments
        if (args != null) {
            month = args.getInt(MONTH, -1)
            year = args.getInt(YEAR, -1)
            startDayOfWeek = args.getInt(START_DAY_OF_WEEK, 1)
            if (startDayOfWeek > 7) {
                startDayOfWeek = startDayOfWeek % 7
            }
            enableImage = args.getBoolean(ENABLE_IMAGES, true)
            isDark = args.getBoolean(DARK_THEME, true)
            val photos = args.getLongArray(MONTH_IMAGES)
            if (photos != null) {
                for (i in photos.indices) {
                    val id = photos[i]
                    if (id != -1) photosList[i] = id
                }
            }
        }
        if (month == -1 || year == -1) {
            val dateTime = DateTime.today(TimeZone.getDefault())
            month = dateTime.month!!
            year = dateTime.year!!
        }
    }

    private fun setupDateGridPages() {
        val currentDateTime = DateTime(year, month, 1, 0, 0, 0, 0)
        pageChangeListener = initMonthPager(currentDateTime)
        val pagerAdapter = MonthPagerAdapter(fragmentManager)
        fragments = pagerAdapter.fragments
        fragments!![0].setDate(currentDateTime.month!!, currentDateTime.year!!)
        val nextDateTime = currentDateTime.plus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay)
        fragments!![1].setDate(nextDateTime.month!!, nextDateTime.year!!)
        val next2DateTime = nextDateTime.plus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay)
        fragments!![2].setDate(next2DateTime.month!!, next2DateTime.year!!)
        val prevDateTime = currentDateTime.minus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay)
        fragments!![3].setDate(prevDateTime.month!!, prevDateTime.year!!)
        for (i in 0 until NUMBER_OF_PAGES) {
            val dateGridFragment = fragments!![i]
            dateGridFragment.setEventsMap(mLastMap)
            dateGridFragment.setOnItemClickListener(getDateItemClickListener())
            dateGridFragment.setOnItemLongClickListener(getDateItemLongClickListener())
        }
        pageChangeListener!!.setFlextGridAdapters(fragments)
        val infinitePagerAdapter = InfinitePagerAdapter(pagerAdapter)
        binding!!.monthsInfinitePager.adapter = infinitePagerAdapter
    }

    private fun initMonthPager(currentDateTime: DateTime): DatePageChangeListener {
        val pageChangeListener = DatePageChangeListener()
        pageChangeListener.setCurrentDateTime(currentDateTime)
        binding!!.monthsInfinitePager.isEnabled = true
        if (Module.isLollipop) {
            binding!!.monthsInfinitePager.addOnPageChangeListener(pageChangeListener)
        } else {
            binding!!.monthsInfinitePager.setOnPageChangeListener(pageChangeListener)
        }
        return pageChangeListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retrieveInitialArgs()
        binding = FragmentFlextCalBinding.inflate(inflater, container, false)
        binding!!.loaderView.setVisibility(View.VISIBLE)
        val weekdaysAdapter = newWeekdayAdapter
        binding!!.weekdayGridview.adapter = weekdaysAdapter
        setupDateGridPages()
        refreshView()
        if (caldroidListener != null) {
            caldroidListener!!.onViewCreated()
        }
        return binding!!.root
    }

    private fun loadData() {
        val provider = CalendarSingleton.getInstance().provider
        if (provider != null) {
            if (provider.isReady) {
                Timber.d("loadData: isReady")
                onReady()
            } else {
                Timber.d("loadData: wait")
                provider.addObserver(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.calendar))
            callback!!.setClick(CalendarSingleton.getInstance().fabClick)
        }
        loadData()
    }

    override fun onPause() {
        super.onPause()
        val provider = CalendarSingleton.getInstance().provider
        provider?.removeObserver(this)
    }

    override fun onReady() {
        Timber.d("onReady: ")
        binding!!.loaderView.setVisibility(View.GONE)
        val provider = CalendarSingleton.getInstance().provider
        if (provider != null) {
            this.mLastMap.clear()
            this.mLastMap.putAll(provider.events)
        }
        refreshFragments()
    }

    private fun refreshFragments() {
        if (binding!!.monthsInfinitePager.adapter != null) {
            val adapter = binding!!.monthsInfinitePager.adapter as InfinitePagerAdapter?
            adapter!!.notifyDataSetChanged()
        }
        if (pageChangeListener != null) {
            pageChangeListener!!.refreshAdapters(pageChangeListener!!.currentPage)
        }
    }

    private inner class DatePageChangeListener : ViewPager.OnPageChangeListener {

        var currentPage = InfiniteViewPager.OFFSET
            private set
        private var currentDateTime: DateTime? = null
        private var flextGridAdapters: List<DateGridFragment>? = null

        internal fun setFlextGridAdapters(flextGridAdapters: List<DateGridFragment>) {
            this.flextGridAdapters = flextGridAdapters
        }

        internal fun setCurrentDateTime(dateTime: DateTime) {
            this.currentDateTime = dateTime
            setCalendarDateTime(currentDateTime)
        }

        /**
         * Return virtual next position
         *
         * @param position position
         * @return position
         */
        private fun getNext(position: Int): Int {
            return (position + 1) % NUMBER_OF_PAGES
        }

        /**
         * Return virtual previous position
         *
         * @param position position
         * @return position
         */
        private fun getPrevious(position: Int): Int {
            return (position + 3) % NUMBER_OF_PAGES
        }

        /**
         * Return virtual current position
         *
         * @param position position
         * @return position
         */
        internal fun getCurrent(position: Int): Int {
            return position % NUMBER_OF_PAGES
        }

        override fun onPageScrollStateChanged(position: Int) {

        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {

        }

        internal fun refreshAdapters(position: Int) {
            val currentAdapter = flextGridAdapters!![getCurrent(position)]
            val prevAdapter = flextGridAdapters!![getPrevious(position)]
            val nextAdapter = flextGridAdapters!![getNext(position)]
            if (position == currentPage) {
                currentAdapter.setEventsMap(mLastMap)
                currentAdapter.setDateTime(currentDateTime)
                prevAdapter.setEventsMap(mLastMap)
                prevAdapter.setDateTime(currentDateTime!!.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay))
                nextAdapter.setEventsMap(mLastMap)
                nextAdapter.setDateTime(currentDateTime!!.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay))
            } else if (position > currentPage) {
                currentDateTime = currentDateTime!!.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay)
                nextAdapter.setEventsMap(mLastMap)
                nextAdapter.setDateTime(currentDateTime!!.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay))
            } else {
                currentDateTime = currentDateTime!!.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay)
                prevAdapter.setEventsMap(mLastMap)
                prevAdapter.setDateTime(currentDateTime!!.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay))
            }// Swipe left
            // Detect if swipe right or swipe left
            // Swipe right
            currentPage = position
        }

        /**
         * Refresh the fragments
         */
        override fun onPageSelected(position: Int) {
            LogUtil.d(TAG, "onPageSelected: $position")
            refreshAdapters(position)
            setCalendarDateTime(currentDateTime)
        }
    }

    companion object {

        private val TAG = "FlextCalendarFragment"

        /**
         * Weekday conventions
         */
        var SUNDAY = 1
        var MONDAY = 2

        /**
         * Flags to display month
         */
        private val MONTH_YEAR_FLAG = (DateUtils.FORMAT_SHOW_DATE
                or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR)

        val NUMBER_OF_PAGES = 4

        /**
         * Initial params key
         */
        val MONTH = "month"
        val YEAR = "year"
        val START_DAY_OF_WEEK = "startDayOfWeek"
        val ENABLE_IMAGES = "enableImages"
        val DARK_THEME = "dark_theme"
        val MONTH_IMAGES = "month_images"

        fun newInstance(): FlextCalendarFragment {
            val fragment = FlextCalendarFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
