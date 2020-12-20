package com.elementary.tasks.month_view

import android.app.AlarmManager
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.core.calendar.InfinitePagerAdapter
import com.elementary.tasks.core.calendar.InfiniteViewPager
import com.elementary.tasks.core.calendar.WeekdayArrayAdapter
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.view_models.month_view.MonthViewViewModel
import com.elementary.tasks.databinding.FragmentFlextCalBinding
import com.elementary.tasks.day_view.DayViewFragment
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.navigation.fragments.BaseCalendarFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import hirondelle.date4j.DateTime
import org.apache.commons.lang3.StringUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : BaseCalendarFragment<FragmentFlextCalBinding>(), MonthCallback {

  private lateinit var dayPagerAdapter: MonthPagerAdapter
  private var behaviour: BottomSheetBehavior<LinearLayout>? = null
  private val datePageChangeListener = DatePageChangeListener()
  private val mViewModel by viewModel<MonthViewViewModel> {
    parametersOf(prefs.isRemindersInCalendarEnabled, prefs.isFutureEventEnabled)
  }
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

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentFlextCalBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    behaviour = BottomSheetBehavior.from(binding.eventsCard)
    behaviour?.state = BottomSheetBehavior.STATE_HIDDEN

    binding.weekdayView.adapter = weekdayAdapter

    initPager()
    initViewModel()
    showCalendar()
  }

  private fun initViewModel() {
    mViewModel.events.observe(viewLifecycleOwner, {
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
    mViewModel.findEvents(monthPagerItem)
  }

  override fun getViewModel(): MonthViewViewModel {
    return mViewModel
  }

  override fun birthdayColor(): Int {
    val ctx = context
    return if (ctx == null || !isAdded) Color.GREEN
    else ThemeProvider.colorBirthdayCalendar(ctx, prefs)
  }

  override fun reminderColor(): Int {
    val ctx = context
    return if (ctx == null || !isAdded) Color.BLUE
    else ThemeProvider.colorReminderCalendar(ctx, prefs)
  }

  override fun onDateClick(date: Date) {
    safeNavigation(CalendarFragmentDirections.actionActionCalendarToDayViewFragment(date.time))
  }

  override fun onDateLongClick(date: Date) {
    dateMills = date.time
    showSheet(eventsList)
  }

  private fun showSheet(list: List<EventModel> = listOf()) {
    withContext {
      binding.addBirth.setOnClickListener {
        addBirthday()
      }
      binding.addBirth.setOnLongClickListener {
        toast(getString(R.string.add_birthday))
        true
      }
      binding.addEvent.setOnClickListener {
        addReminder()
      }
      binding.addEvent.setOnLongClickListener {
        toast(getString(R.string.add_reminder_menu))
        true
      }
      if (list.isNotEmpty()) {
        binding.loadingView.visibility = View.VISIBLE
        binding.eventsList.layoutManager = LinearLayoutManager(it)
        loadEvents(binding.eventsList, binding.loadingView, list)
      } else {
        binding.loadingView.visibility = View.GONE
      }
      if (dateMills != 0L) {
        val monthTitle = DateUtils.formatDateTime(activity, dateMills, DayViewFragment.MONTH_YEAR_FLAG).toString()
        binding.dateLabel.text = monthTitle
      }
      behaviour?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }
  }

  private inner class DatePageChangeListener : ViewPager.OnPageChangeListener {

    var currentPage = InfiniteViewPager.OFFSET + 1
      private set
    private var currentDateTime: Long = System.currentTimeMillis()

    fun setCurrentDateTime(dateTime: Long) {
      this.currentDateTime = dateTime
    }

    private fun getNext(position: Int): Int {
      return (position + 1) % NUMBER_OF_PAGES
    }

    private fun getPrevious(position: Int): Int {
      return (position + 3) % NUMBER_OF_PAGES
    }

    fun getCurrent(position: Int): Int {
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
