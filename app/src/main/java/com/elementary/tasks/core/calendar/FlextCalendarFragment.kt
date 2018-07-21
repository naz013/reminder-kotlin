package com.elementary.tasks.core.calendar

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.EventsDataProvider
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.views.MonthView
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import hirondelle.date4j.DateTime
import kotlinx.android.synthetic.main.fragment_flext_cal.*
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
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
open class FlextCalendarFragment : BaseNavigationFragment(), EventsDataProvider.Callback {

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

    private var enableImage = true
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

    private var startDayOfWeek = SUNDAY

    private var dateItemClickListener: MonthView.OnDateClick? = null
    private var dateItemLongClickListener: MonthView.OnDateLongClick? = null

    private var caldroidListener: FlextListener? = null
    private var pageChangeListener: DatePageChangeListener? = null

    private val newWeekdayAdapter: WeekdayArrayAdapter
        get() = WeekdayArrayAdapter(activity!!, android.R.layout.simple_list_item_1,
                daysOfWeek, isDark)

    private val daysOfWeek: ArrayList<String>
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
        var dateClick = dateItemClickListener
        if (dateClick == null) {
            dateClick = object : MonthView.OnDateClick {
                override fun onClick(dateTime: DateTime) {
                    if (caldroidListener != null) {
                        val date = FlextHelper.convertDateTimeToDate(dateTime)
                        caldroidListener!!.onClickDate(date)
                    }
                }
            }
        }
        return dateClick
    }

    private fun getDateItemLongClickListener(): MonthView.OnDateLongClick {
        var dateLongClick = dateItemLongClickListener
        if (dateLongClick == null) {
            dateLongClick = object : MonthView.OnDateLongClick {
                override fun onLongClick(dateTime: DateTime) {
                    if (caldroidListener != null) {
                        val date = FlextHelper.convertDateTimeToDate(dateTime)
                        caldroidListener!!.onLongClickDate(date)
                    }
                }
            }
        }
        return dateLongClick
    }

    private fun refreshMonthTitleTextView() {
        firstMonthTime.set(Calendar.YEAR, year)
        firstMonthTime.set(Calendar.MONTH, month - 1)
        firstMonthTime.set(Calendar.DAY_OF_MONTH, 1)
        val millis = firstMonthTime.timeInMillis
        monthYearStringBuilder.setLength(0)
        val monthTitle = DateUtils.formatDateRange(activity,
                monthYearFormatter, millis, millis, MONTH_YEAR_FLAG).toString()
        monthYear.text = StringUtils.capitalize(monthTitle)
        if (caldroidListener != null) {
            caldroidListener!!.onMonthSelected(month)
        }
        if (imageView != null && enableImage) {
            val check = ImageCheck.getInstance()
            if (check.isImage(month - 1, photosList[month - 1])) {
                Glide.with(imageView)
                        .load(File(check.getImage(month - 1, photosList[month - 1])!!))
                        .into(imageView)
            } else {
                LoadAsync(activity!!, month - 1, photosList[month - 1]).execute()
            }
        }
    }

    fun refreshView() {
        if (month == -1 || year == -1) {
            return
        }
        refreshMonthTitleTextView()
    }

    private fun retrieveInitialArgs() {
        val args = arguments
        if (args != null) {
            month = args.getInt(MONTH, -1)
            year = args.getInt(YEAR, -1)
            startDayOfWeek = args.getInt(START_DAY_OF_WEEK, 1)
            if (startDayOfWeek > 7) {
                startDayOfWeek %= 7
            }
            enableImage = args.getBoolean(ENABLE_IMAGES, true)
            isDark = args.getBoolean(DARK_THEME, true)
            val photos = args.getLongArray(MONTH_IMAGES)
            if (photos != null) {
                for (i in photos.indices) {
                    val id = photos[i]
                    if (id != -1L) photosList[i] = id
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
        val pagerAdapter = MonthPagerAdapter(fragmentManager!!)
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
        pageChangeListener!!.setFlextGridAdapters(fragments!!)
        val infinitePagerAdapter = InfinitePagerAdapter(pagerAdapter)
        months_infinite_pager.adapter = infinitePagerAdapter
    }

    private fun initMonthPager(currentDateTime: DateTime): DatePageChangeListener {
        val pageChangeListener = DatePageChangeListener()
        pageChangeListener.setCurrentDateTime(currentDateTime)
        months_infinite_pager.isEnabled = true
        months_infinite_pager.addOnPageChangeListener(pageChangeListener)
        return pageChangeListener
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retrieveInitialArgs()
        return inflater.inflate(R.layout.fragment_flext_cal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loaderView?.visibility = View.VISIBLE
        val weekdaysAdapter = newWeekdayAdapter
        weekday_gridview.adapter = weekdaysAdapter
        setupDateGridPages()
        refreshView()
        if (caldroidListener != null) {
            caldroidListener!!.onViewCreated()
        }
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
        loaderView?.visibility = View.GONE
        val provider = CalendarSingleton.getInstance().provider
        if (provider != null) {
            this.mLastMap.clear()
            this.mLastMap.putAll(provider.events)
        }
        refreshFragments()
    }

    private fun refreshFragments() {
        if (months_infinite_pager.adapter != null) {
            val adapter = months_infinite_pager.adapter as InfinitePagerAdapter?
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
            when {
                position == currentPage -> {
                    currentAdapter.setEventsMap(mLastMap)
                    currentAdapter.setDateTime(currentDateTime!!)
                    prevAdapter.setEventsMap(mLastMap)
                    prevAdapter.setDateTime(currentDateTime!!.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay))
                    nextAdapter.setEventsMap(mLastMap)
                    nextAdapter.setDateTime(currentDateTime!!.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay))
                }
                position > currentPage -> {
                    currentDateTime = currentDateTime!!.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay)
                    nextAdapter.setEventsMap(mLastMap)
                    nextAdapter.setDateTime(currentDateTime!!.plus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay))
                }
                else -> {
                    currentDateTime = currentDateTime!!.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay)
                    prevAdapter.setEventsMap(mLastMap)
                    prevAdapter.setDateTime(currentDateTime!!.minus(0, 1, 0, 0, 0, 0, 0, DateTime.DayOverflow.LastDay))
                }
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

        private const val TAG = "FlextCalendarFragment"

        /**
         * Weekday conventions
         */
        var SUNDAY = 1
        var MONDAY = 2

        /**
         * Flags to display month
         */
        private const val MONTH_YEAR_FLAG = (DateUtils.FORMAT_SHOW_DATE
                or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR)

        const val NUMBER_OF_PAGES = 4

        /**
         * Initial params key
         */
        const val MONTH = "month"
        const val YEAR = "year"
        const val START_DAY_OF_WEEK = "startDayOfWeek"
        const val ENABLE_IMAGES = "enableImages"
        const val DARK_THEME = "dark_theme"
        const val MONTH_IMAGES = "month_images"

        fun newInstance(): FlextCalendarFragment {
            val fragment = FlextCalendarFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
