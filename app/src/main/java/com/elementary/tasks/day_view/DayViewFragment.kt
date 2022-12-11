package com.elementary.tasks.day_view

import android.app.AlarmManager
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.core.calendar.InfinitePagerAdapter
import com.elementary.tasks.core.calendar.InfiniteViewPager
import com.elementary.tasks.core.utils.GlobalButtonObservable
import com.elementary.tasks.core.view_models.day_view.DayViewViewModel
import com.elementary.tasks.databinding.FragmentDayViewBinding
import com.elementary.tasks.day_view.day.DayCallback
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.day_view.pager.DayPagerAdapter
import com.elementary.tasks.navigation.fragments.BaseCalendarFragment
import org.apache.commons.lang3.StringUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.Calendar

class DayViewFragment : BaseCalendarFragment<FragmentDayViewBinding>(), DayCallback {

  private val buttonObservable by inject<GlobalButtonObservable>()
  lateinit var dayPagerAdapter: DayPagerAdapter
  private val datePageChangeListener = DatePageChangeListener()
  private val dayViewViewModel by viewModel<DayViewViewModel> {
    parametersOf(prefs.isFutureEventEnabled)
  }
  private var eventsPagerItem: EventsPagerItem? = null
  private var listener: ((EventsPagerItem, List<EventModel>) -> Unit)? = null

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val bundle = arguments
    if (bundle != null) {
      dateMills = DayViewFragmentArgs.fromBundle(bundle).date
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.day_view_menu, menu)
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_voice -> {
        buttonObservable.fireAction(requireView(), GlobalButtonObservable.Action.VOICE)
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentDayViewBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.fab.setOnClickListener { showActionDialog(false) }
    initPager()
    initViewModel()
    loadData()
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
    return dayViewViewModel
  }

  override fun find(eventsPagerItem: EventsPagerItem, listener: ((EventsPagerItem, List<EventModel>) -> Unit)?) {
    this.eventsPagerItem = eventsPagerItem
    this.listener = listener
    dayViewViewModel.findEvents(eventsPagerItem)
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
    private const val NUMBER_OF_PAGES = 4
    const val MONTH_YEAR_FLAG = (DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR)
  }
}
