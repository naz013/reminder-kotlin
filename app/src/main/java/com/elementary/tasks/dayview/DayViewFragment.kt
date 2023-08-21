package com.elementary.tasks.dayview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.core.calendar.InfinitePagerAdapter
import com.elementary.tasks.core.calendar.InfiniteViewPager
import com.elementary.tasks.core.utils.ui.GlobalButtonObservable
import com.elementary.tasks.databinding.FragmentDayViewBinding
import com.elementary.tasks.dayview.day.DayCallback
import com.elementary.tasks.dayview.day.EventModel
import com.elementary.tasks.dayview.pager.DayPagerAdapter
import com.elementary.tasks.navigation.fragments.BaseCalendarFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate

class DayViewFragment : BaseCalendarFragment<FragmentDayViewBinding>(), DayCallback {

  private val buttonObservable by inject<GlobalButtonObservable>()
  lateinit var dayPagerAdapter: DayPagerAdapter
  private val datePageChangeListener = DatePageChangeListener()
  private val dayViewViewModel by viewModel<DayViewViewModel>()
  private var eventsPagerItem: EventsPagerItem? = null
  private var listener: ((EventsPagerItem, List<EventModel>) -> Unit)? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val bundle = arguments
    if (bundle != null) {
      date = dateTimeManager.fromMillis(DayViewFragmentArgs.fromBundle(bundle).date).toLocalDate()
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentDayViewBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    addMenu(R.menu.fragment_day_view, { menuItem ->
      when (menuItem.itemId) {
        R.id.action_voice -> {
          buttonObservable.fireAction(requireView(), GlobalButtonObservable.Action.VOICE)
          true
        }
        else -> false
      }
    })
    binding.fab.setOnClickListener { tryToShowActionDialog() }
    initPager()
    initViewModel()
    loadData()
  }

  private fun tryToShowActionDialog() {
    val item = eventsPagerItem
    val events = dayViewViewModel.events.value
    var list = emptyList<EventModel>()
    if (events != null && item != null) {
      val foundItem = events.first
      val foundList = events.second
      if (foundItem == item) {
        list = foundList
      }
    }
    showActionDialog(list)
  }

  private fun initViewModel() {
    dayViewViewModel.events.observe(viewLifecycleOwner) {
      val item = eventsPagerItem
      if (it != null && item != null) {
        val foundItem = it.first
        val foundList = it.second
        if (foundItem == item) {
          listener?.invoke(foundItem, foundList)
        }
      }
    }
  }

  private fun initPager() {
    dayPagerAdapter = DayPagerAdapter(childFragmentManager)
    binding.pager.adapter = InfinitePagerAdapter(dayPagerAdapter)
  }

  private fun updateMenuTitles(): String {
    val monthTitle = dateTimeManager.formatCalendarDate(date)
    callback?.onTitleChange(monthTitle)
    return monthTitle
  }

  override fun getTitle(): String = updateMenuTitles()

  private fun loadData() {
    showEvents(date)
  }

  private fun fromDate(date: LocalDate): EventsPagerItem {
    return EventsPagerItem(date.dayOfMonth, date.monthValue, date.year)
  }

  private fun showEvents(date: LocalDate) {
    this.date = date
    updateMenuTitles()

    datePageChangeListener.setCurrentDateTime(date)
    binding.pager.isEnabled = true
    binding.pager.addOnPageChangeListener(datePageChangeListener)
    binding.pager.currentItem = InfiniteViewPager.OFFSET + 1
  }

  override fun getViewModel(): DayViewViewModel {
    return dayViewViewModel
  }

  override fun find(
    eventsPagerItem: EventsPagerItem,
    listener: ((EventsPagerItem, List<EventModel>) -> Unit)?
  ) {
    this.eventsPagerItem = eventsPagerItem
    this.listener = listener
    dayViewViewModel.findEvents(eventsPagerItem)
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
      refreshAdapters(position)
      dayPagerAdapter.fragments[getCurrent(position)].requestData()
      val item = dayPagerAdapter.fragments[getCurrent(position)].getModel() ?: return
      date = LocalDate.of(item.year, item.month, item.day)
      updateMenuTitles()
    }
  }

  companion object {
    private const val NUMBER_OF_PAGES = 4
  }
}