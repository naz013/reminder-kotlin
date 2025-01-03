package com.elementary.tasks.calendar.dayview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.calendar.BaseCalendarFragment
import com.elementary.tasks.calendar.dayview.pager.DayPagerAdapter
import com.elementary.tasks.calendar.dayview.weekheader.WeekAdapter
import com.elementary.tasks.core.calendar.InfinitePagerAdapter
import com.elementary.tasks.core.calendar.InfiniteViewPager
import com.elementary.tasks.databinding.FragmentDayViewBinding
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate

class WeekViewFragment : BaseCalendarFragment<FragmentDayViewBinding>() {

  lateinit var dayPagerAdapter: DayPagerAdapter
  private val datePageChangeListener = DatePageChangeListener()
  private val weekViewModel by viewModel<WeekViewModel>()

  private val weekAdapter = WeekAdapter { scrollPositions(it.localDate) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val bundle = arguments
    if (bundle != null) {
      date = dateTimeManager.fromMillis(WeekViewFragmentArgs.fromBundle(bundle).date).toLocalDate()
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentDayViewBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.weekGridView.adapter = weekAdapter
    binding.fab.setOnClickListener { tryToShowActionDialog() }
    initPager()
    initViewModel()
    loadData()
  }

  private fun tryToShowActionDialog() {
    showActionDialog()
  }

  private fun initViewModel() {
    weekViewModel.week.nonNullObserve(viewLifecycleOwner) { weekAdapter.submitList(it) }
  }

  private fun initPager() {
    dayPagerAdapter = DayPagerAdapter(childFragmentManager)
    binding.pager.adapter = InfinitePagerAdapter(dayPagerAdapter)
  }

  private fun updateMenuTitles(): String {
    val monthTitle = dateTimeManager.formatCalendarDate(date)
    setTitle(monthTitle)
    return monthTitle
  }

  override fun getTitle(): String = updateMenuTitles()

  private fun loadData() {
    showEvents(date)
  }

  private fun fromDate(date: LocalDate): DayPagerItem {
    return DayPagerItem(date)
  }

  private fun scrollPositions(date: LocalDate) {
    Logger.d("scrollPositions: date=$date")
    datePageChangeListener.jumpToDate(date)
  }

  private fun showEvents(date: LocalDate) {
    this.date = date
    updateMenuTitles()

    datePageChangeListener.setCurrentDateTime(date)
    binding.pager.isEnabled = true
    binding.pager.addOnPageChangeListener(datePageChangeListener)
    binding.pager.currentItem = InfiniteViewPager.OFFSET + 1
  }

  private inner class DatePageChangeListener : ViewPager.OnPageChangeListener {

    var currentPage = InfiniteViewPager.OFFSET + 1
      private set
    private var currentDate: LocalDate = LocalDate.now()

    fun jumpToDate(newDate: LocalDate) {
      this.currentDate = newDate
      refreshAdapters(currentPage)
      date = newDate
      updateMenuTitles()
      weekViewModel.onDateSelected(date)
    }

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
          prevFragment.setModel(fromDate(currentDate.minusDays(1)))
          nextFragment.setModel(fromDate(currentDate.plusDays(1)))
        }
        position > currentPage -> {
          currentDate = currentDate.plusDays(1)
          nextFragment.setModel(fromDate(currentDate.plusDays(1)))
        }
        else -> {
          currentDate = currentDate.minusDays(1)
          prevFragment.setModel(fromDate(currentDate.minusDays(1)))
        }
      }
      currentPage = position
    }

    override fun onPageSelected(position: Int) {
      Logger.d("onPageSelected: position=$position")
      refreshAdapters(position)
      val item = dayPagerAdapter.fragments[getCurrent(position)].getModel() ?: return
      Logger.d("onPageSelected: item=$item")
      date = item.date
      updateMenuTitles()
      weekViewModel.onDateSelected(date)
    }
  }

  companion object {
    private const val NUMBER_OF_PAGES = 4
  }
}
