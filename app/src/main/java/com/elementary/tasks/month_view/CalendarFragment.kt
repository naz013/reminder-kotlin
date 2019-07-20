package com.elementary.tasks.month_view

import android.app.AlarmManager
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.core.calendar.InfinitePagerAdapter
import com.elementary.tasks.core.calendar.InfiniteViewPager
import com.elementary.tasks.core.calendar.WeekdayArrayAdapter
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.view_models.month_view.MonthViewViewModel
import com.elementary.tasks.databinding.FragmentFlextCalBinding
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.navigation.fragments.BaseCalendarFragment
import hirondelle.date4j.DateTime
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : BaseCalendarFragment<FragmentFlextCalBinding>(), MonthCallback {

    lateinit var dayPagerAdapter: MonthPagerAdapter
    private val datePageChangeListener = DatePageChangeListener()
    private lateinit var viewModel: MonthViewViewModel
    private var monthPagerItem: MonthPagerItem? = null
    private var listener: ((MonthPagerItem, List<EventModel>) -> Unit)? = null
    private val eventsList: MutableList<EventModel> = mutableListOf()

    private var startDayOfWeek = SUNDAY
    private val weekdayAdapter: WeekdayArrayAdapter?
        get() {
            val ctx = activity ?: return null
            return WeekdayArrayAdapter(ctx, android.R.layout.simple_list_item_1, daysOfWeek, isDark)
        }

    private val daysOfWeek: ArrayList<String>
        get() {
            val list = ArrayList<String>()
            val fmt = SimpleDateFormat("EEE", Locale.getDefault())
            val sunday = DateTime(2013, 2, 17, 0, 0, 0, 0)
            var nextDay = sunday.plusDays(startDayOfWeek - SUNDAY)
            for (i in 0..6) {
                val date = TimeUtil.convertDateTimeToDate(nextDay)
                list.add(fmt.format(date).toUpperCase())
                nextDay = nextDay.plusDays(1)
            }
            return list
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startDayOfWeek = if (prefs.startDay == 0) {
            SUNDAY
        } else {
            MONDAY
        }
    }

    override fun getTitle(): String = updateMenuTitles(System.currentTimeMillis())

    override fun layoutRes(): Int = R.layout.fragment_flext_cal

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.weekdayView.adapter = weekdayAdapter

        initPager()
        initViewModel()
        showCalendar()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this,
                MonthViewViewModel.Factory(prefs.isRemindersInCalendarEnabled,
                        prefs.isFutureEventEnabled)).get(MonthViewViewModel::class.java)
        viewModel.events.observe(this, Observer<Pair<MonthPagerItem, List<EventModel>>> {
            val item = monthPagerItem
            if (it != null && item != null) {
                val foundItem = it.first
                val foundList = it.second
                if (foundItem == item) {
                    eventsList.clear()
                    eventsList.addAll(foundList)
                    listener?.invoke(foundItem, foundList)
                }
            }
        })
    }

    private fun initPager() {
        dayPagerAdapter = MonthPagerAdapter(childFragmentManager)
        binding.pager.adapter = InfinitePagerAdapter(dayPagerAdapter)
    }

    private fun updateMenuTitles(mills: Long): String {
        val monthTitle = StringUtils.capitalize(DateUtils.formatDateTime(activity, mills, MONTH_YEAR_FLAG).toString())
        callback?.onTitleChange(monthTitle)
        return monthTitle
    }

    private fun showCalendar() {
        dayPagerAdapter = MonthPagerAdapter(childFragmentManager)
        binding.pager.adapter = InfinitePagerAdapter(dayPagerAdapter)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.DAY_OF_MONTH, 15)

        updateMenuTitles(calendar.timeInMillis)
        datePageChangeListener.setCurrentDateTime(calendar.timeInMillis)

        binding.pager.isEnabled = true
        binding.pager.addOnPageChangeListener(datePageChangeListener)
        binding.pager.currentItem = InfiniteViewPager.OFFSET + 1
    }

    private fun fromMills(mills: Long): MonthPagerItem {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = mills
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        return MonthPagerItem(month, year)
    }

    override fun find(monthPagerItem: MonthPagerItem, listener: ((MonthPagerItem, List<EventModel>) -> Unit)?) {
        Timber.d("find: $monthPagerItem")
        this.monthPagerItem = monthPagerItem
        this.listener = listener
        viewModel.findEvents(monthPagerItem)
    }

    override fun getViewModel(): MonthViewViewModel {
        return viewModel
    }

    override fun birthdayColor(): Int {
        return themeUtil.colorBirthdayCalendar()
    }

    override fun reminderColor(): Int {
        return themeUtil.colorReminderCalendar()
    }

    override fun onDateClick(date: Date) {
        findNavController().navigate(CalendarFragmentDirections.actionActionCalendarToDayViewFragment(date.time))
    }

    override fun onDateLongClick(date: Date) {
        dateMills = date.time
        showActionDialog(true, eventsList)
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
                    prevFragment.setModel(fromMills(currentDateTime - MONTH))
                    nextFragment.setModel(fromMills(currentDateTime + MONTH))
                }
                position > currentPage -> {
                    currentDateTime += MONTH
                    nextFragment.setModel(fromMills(currentDateTime + MONTH))
                }
                else -> {
                    currentDateTime -= MONTH
                    prevFragment.setModel(fromMills(currentDateTime - MONTH))
                }
            }
            currentPage = position
        }

        override fun onPageSelected(position: Int) {
            val current = dayPagerAdapter.fragments[getCurrent(position)]

            refreshAdapters(position)
            current.requestData()
            val item = current.getModel()

            Timber.d("onPageSelected: $item, $current")

            if (item == null) {
                return
            }

            val calendar = Calendar.getInstance()
            calendar.set(item.year, item.month, 15)
            updateMenuTitles(calendar.timeInMillis)
        }
    }

    companion object {
        private const val SUNDAY = 1
        private const val MONDAY = 2
        private const val NUMBER_OF_PAGES = 4
        private const val MONTH = AlarmManager.INTERVAL_DAY * 30
        private const val MONTH_YEAR_FLAG = (DateUtils.FORMAT_SHOW_DATE
                or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR)
    }
}
