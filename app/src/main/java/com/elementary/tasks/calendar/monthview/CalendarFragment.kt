package com.elementary.tasks.calendar.monthview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.elementary.tasks.R
import com.elementary.tasks.calendar.BaseCalendarFragment
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.elementary.tasks.core.calendar.WeekdayArrayAdapter
import com.github.naz013.domain.calendar.StartDayOfWeekProtocol
import com.elementary.tasks.databinding.FragmentFlextCalBinding
import com.github.naz013.logging.Logger
import org.apache.commons.lang3.StringUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class CalendarFragment :
  BaseCalendarFragment<FragmentFlextCalBinding>(),
  MonthCallback,
  InfinitePagerAdapter2.DataAccessor {

  private val infinitePagerAdapter = InfinitePagerAdapter2(
    dataAccessor = this,
    monthCallback = this
  )

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

  override fun getStartDay(): StartDayOfWeekProtocol {
    return StartDayOfWeekProtocol(prefs.startDay)
  }

  override fun getTodayColor(): Int {
    return prefs.todayColor
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

    binding.weekdayView.adapter = WeekdayArrayAdapter(
      context = requireContext(),
      textViewResourceId = android.R.layout.simple_list_item_1,
      objects = daysOfWeek,
      isDark = isDark
    )

    showCalendar()

    addMenu(R.menu.fragment_calendar, { menuItem ->
      when (menuItem.itemId) {
        R.id.action_settings -> {
          safeNavigation {
            CalendarFragmentDirections.actionActionCalendarToCalendarSettingsFragment()
          }
          true
        }

        else -> false
      }
    })

    analyticsEventSender.send(ScreenUsedEvent(Screen.CALENDAR))
  }

  override fun onBackStackResume() {
    super.onBackStackResume()
    infinitePagerAdapter.selectPosition(1)
    binding.infiniteViewPager.setCurrentItem(1, false)
  }

  private fun isSunday(): Boolean {
    return prefs.startDay == 0
  }

  private fun updateMenuTitles(date: LocalDate): String {
    val monthTitle = StringUtils.capitalize(dateTimeManager.formatCalendarMonthYear(date))
    setTitle(monthTitle)
    return monthTitle
  }

  private fun showCalendar() {
    val date = LocalDate.now().withDayOfMonth(15)
    updateMenuTitles(date)

    binding.infiniteViewPager.adapter = infinitePagerAdapter
    binding.infiniteViewPager.registerOnPageChangeCallback(
      object : ViewPager2.OnPageChangeCallback() {
        private var currentDate = LocalDate.now()

        override fun onPageScrollStateChanged(state: Int) {
          super.onPageScrollStateChanged(state)
          if (state == ViewPager2.SCROLL_STATE_IDLE) {
            Logger.d("onPageScrollStateChanged: ${binding.infiniteViewPager.currentItem}")
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
          Logger.d("onPageSelected: $position")
          if (position == 1 || position == 4) {
            updateMenuTitles(currentDate)
            infinitePagerAdapter.selectPosition(position)
          }
        }
      }
    )
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
    return MonthPagerItem(date.monthValue, date.year, date)
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
    showSheet()
  }

  private fun showSheet() {
    safeContext {
      DayBottomSheetDialog(
        context = this,
        label = dateTimeManager.formatCalendarDate(date),
        addReminderCallback = { addReminder() },
        addBirthdayCallback = { addBirthday() }
      ).show()
    }
  }
}
