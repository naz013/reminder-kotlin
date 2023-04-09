package com.elementary.tasks.monthview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Screen
import com.elementary.tasks.core.analytics.ScreenUsedEvent
import com.elementary.tasks.core.calendar.WeekdayArrayAdapter
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.GlobalButtonObservable
import com.elementary.tasks.databinding.FragmentFlextCalBinding
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.navigation.fragments.BaseCalendarFragment
import org.apache.commons.lang3.StringUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber

class CalendarFragment : BaseCalendarFragment<FragmentFlextCalBinding>(), MonthCallback {

  private val viewModel by viewModel<CalendarViewModel>()

  private val weekdayAdapter: WeekdayArrayAdapter by lazy {
    WeekdayArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, daysOfWeek, isDark)
  }
  private val infinitePagerAdapter = InfinitePagerAdapter2(prefs.todayColor, prefs.startDay, this)

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

  @SuppressLint("ClickableViewAccessibility")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.weekdayView.adapter = weekdayAdapter

    initViewModel()
    showCalendar()

    addMenu(R.menu.fragment_calendar, { menuItem ->
      when (menuItem.itemId) {
        R.id.action_settings -> {
          safeNavigation(CalendarFragmentDirections.actionActionCalendarToCalendarSettingsFragment())
          true
        }

        else -> false
      }
    })

    analyticsEventSender.send(ScreenUsedEvent(Screen.CALENDAR))
  }

  private fun isSunday(): Boolean {
    return prefs.startDay == 0
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.events.nonNullObserve(viewLifecycleOwner) { showSheet(it) }
    viewModel.map.nonNullObserve(viewLifecycleOwner) { infinitePagerAdapter.updateMapData(it) }
  }

  private fun updateMenuTitles(date: LocalDate): String {
    val monthTitle = StringUtils.capitalize(dateTimeManager.formatCalendarMonthYear(date))
    callback?.onTitleChange(monthTitle)
    return monthTitle
  }

  private fun showCalendar() {
    val date = LocalDate.now().withDayOfMonth(15)
    updateMenuTitles(date)

    binding.infiniteViewPager.adapter = infinitePagerAdapter
    binding.infiniteViewPager.registerOnPageChangeCallback(object :
      ViewPager2.OnPageChangeCallback() {
      private var currentDate = LocalDate.now()

      override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)
        if (state == ViewPager2.SCROLL_STATE_IDLE) {
          Timber.d("onPageScrollStateChanged: ${binding.infiniteViewPager.currentItem}")
          when (binding.infiniteViewPager.currentItem) {
            0 -> {
              // move to 4th position, current - 1
              currentDate = currentDate.minusMonths(1)
              infinitePagerAdapter.updateRightSide(createSide(currentDate))
              binding.infiniteViewPager.setCurrentItem(4, false)
            }

            2 -> {
              // move to 4th position, current + 1
              currentDate = currentDate.plusMonths(1)
              infinitePagerAdapter.updateRightSide(createSide(currentDate))
              binding.infiniteViewPager.setCurrentItem(4, false)
            }

            3 -> {
              // move to 1st position, current - 1
              currentDate = currentDate.minusMonths(1)
              infinitePagerAdapter.updateLeftSide(createSide(currentDate))
              binding.infiniteViewPager.setCurrentItem(1, false)
            }

            5 -> {
              // move to 1th position, current + 1
              currentDate = currentDate.plusMonths(1)
              infinitePagerAdapter.updateLeftSide(createSide(currentDate))
              binding.infiniteViewPager.setCurrentItem(1, false)
            }
          }
        }
      }

      override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        Timber.d("onPageSelected: $position")
        if (position == 1 || position == 4) {
          updateMenuTitles(currentDate)
          infinitePagerAdapter.selectPosition(position)
          viewModel.find(fromDate(currentDate))
        }
      }
    })
    infinitePagerAdapter.updateLeftSide(createSide(LocalDate.now()))
    infinitePagerAdapter.updateRightSide(createSide(LocalDate.now().plusMonths(3)))

    infinitePagerAdapter.selectPosition(1)
    binding.infiniteViewPager.setCurrentItem(1, false)
  }

  private fun createSide(date: LocalDate): List<MonthPagerItem> {
    return listOf(
      fromDate(date.minusMonths(1)),
      fromDate(date),
      fromDate(date.plusMonths(1))
    )
  }

  private fun fromDate(date: LocalDate): MonthPagerItem {
    return MonthPagerItem(date.monthValue, date.year)
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
    viewModel.onDateLongClicked(date)
  }

  private fun showSheet(list: List<EventModel> = listOf()) {
    Timber.d("showSheet: ${list.size}")
    val label = dateTimeManager.formatCalendarDate(date)
    withContext {
      DayBottomSheetDialog(
        context = it,
        label = label,
        list = list,
        addReminderCallback = { addReminder() },
        addBirthdayCallback = { addBirthday() },
        loadCallback = { listView, loadingView, emptyView, list ->
          loadEvents(listView, loadingView, emptyView, list)
        }
      ).show()
    }
  }
}
