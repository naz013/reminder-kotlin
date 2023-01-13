package com.elementary.tasks.month_view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Screen
import com.elementary.tasks.core.analytics.ScreenUsedEvent
import com.elementary.tasks.core.calendar.InfinitePagerAdapter
import com.elementary.tasks.core.calendar.InfiniteViewPager
import com.elementary.tasks.core.calendar.WeekdayArrayAdapter
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.databinding.FragmentFlextCalBinding
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.navigation.fragments.BaseCalendarFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.apache.commons.lang3.StringUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber

class CalendarFragment : BaseCalendarFragment<FragmentFlextCalBinding>(), MonthCallback {

  private lateinit var dayPagerAdapter: MonthPagerAdapter
  private var behaviour: BottomSheetBehavior<LinearLayout>? = null
  private val datePageChangeListener = DatePageChangeListener()

  private val mViewModel by viewModel<MonthViewViewModel>()
  private var monthPagerItem: MonthPagerItem? = null
  private var listener: ((MonthPagerItem, List<EventModel>) -> Unit)? = null
  private val eventsList: MutableList<EventModel> = mutableListOf()

  private val weekdayAdapter: WeekdayArrayAdapter by lazy {
    WeekdayArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, daysOfWeek, isDark)
  }

  private val daysOfWeek: ArrayList<String>
    get() {
      val list = ArrayList<String>()
      var date = if (isSunday()) {
        LocalDate.of(2022, 12, 25)
      } else {
        LocalDate.of(2022, 12, 26)
      }
      for (i in 0 until 7) {
        list.add(dateTimeManager.formatCalendarWeekday(date).uppercase())
        date = date.plusDays(1)
      }
      return list
    }

  override fun getTitle(): String = updateMenuTitles(LocalDate.now())

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

    analyticsEventSender.send(ScreenUsedEvent(Screen.CALENDAR))
  }

  private fun isSunday(): Boolean {
    return prefs.startDay == 0
  }

  private fun initViewModel() {
    mViewModel.events.observe(viewLifecycleOwner) {
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
    }
  }

  private fun initPager() {
    dayPagerAdapter = MonthPagerAdapter(childFragmentManager)
    binding.pager.adapter = InfinitePagerAdapter(dayPagerAdapter)
  }

  private fun updateMenuTitles(date: LocalDate): String {
    val monthTitle = StringUtils.capitalize(dateTimeManager.formatCalendarMonthYear(date))
    callback?.onTitleChange(monthTitle)
    return monthTitle
  }

  private fun showCalendar() {
    dayPagerAdapter = MonthPagerAdapter(childFragmentManager)
    binding.pager.adapter = InfinitePagerAdapter(dayPagerAdapter)

    val date = LocalDate.now().withDayOfMonth(15)

    updateMenuTitles(date)
    datePageChangeListener.setCurrentDateTime(date)

    binding.pager.isEnabled = true
    binding.pager.addOnPageChangeListener(datePageChangeListener)
    binding.pager.currentItem = InfiniteViewPager.OFFSET + 1
  }

  private fun fromDate(date: LocalDate): MonthPagerItem {
    return MonthPagerItem(date.monthValue, date.year)
  }

  override fun find(
    monthPagerItem: MonthPagerItem,
    listener: ((MonthPagerItem, List<EventModel>) -> Unit)?
  ) {
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

  override fun onDateClick(date: LocalDate) {
    safeNavigation(
      CalendarFragmentDirections.actionActionCalendarToDayViewFragment(
        dateTimeManager.toMillis(LocalDateTime.of(date, LocalTime.now()))
      )
    )
  }

  override fun onDateLongClick(date: LocalDate) {
    this.date = date
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
      binding.dateLabel.text = dateTimeManager.formatCalendarDate(date)
      behaviour?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }
  }

  private inner class DatePageChangeListener : ViewPager.OnPageChangeListener {

    var currentPage = InfiniteViewPager.OFFSET + 1
      private set
    private var currentDate: LocalDate = LocalDate.now()

    fun setCurrentDateTime(date: LocalDate) {
      this.currentDate = date
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
          currentFragment.setModel(fromDate(currentDate))
          prevFragment.setModel(fromDate(currentDate.minusMonths(1)))
          nextFragment.setModel(fromDate(currentDate.plusMonths(1)))
        }

        position > currentPage -> {
          currentDate = currentDate.plusMonths(1)
          nextFragment.setModel(fromDate(currentDate.plusMonths(1)))
        }

        else -> {
          currentDate = currentDate.minusMonths(1)
          prevFragment.setModel(fromDate(currentDate.minusMonths(1)))
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

      val date = LocalDate.of(item.year, item.monthValue, 15)
      updateMenuTitles(date)
    }
  }

  companion object {
    private const val NUMBER_OF_PAGES = 4
  }
}
