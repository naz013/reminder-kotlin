package com.elementary.tasks.calendar.dayview

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.elementary.tasks.calendar.dayview.day.DayEventsListFragment
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

class InfiniteDayViewPagerAdapter(
  fragment: Fragment,
  private val baseDate: LocalDate = LocalDate.now(),
) : FragmentStateAdapter(fragment) {

  override fun createFragment(position: Int): Fragment {
    // Calculate the date offset from the center position
    val offset = position - CENTER_POSITION
    val date = baseDate.plusDays(offset.toLong())
    Logger.d(TAG, "Creating fragment for position $position, date: $date")
    return DayEventsListFragment.newInstance(DayPagerItem(date))
  }

  override fun getItemCount(): Int {
    return Int.MAX_VALUE
  }

  /**
   * Get the date for a given position
   */
  fun getDateForPosition(position: Int): LocalDate {
    val offset = position - CENTER_POSITION
    return baseDate.plusDays(offset.toLong())
  }

  /**
   * Get the position for a given date
   */
  fun getPositionForDate(targetDate: LocalDate): Int {
    val daysDiff = ChronoUnit.DAYS.between(
      LocalDate.of(baseDate.year, baseDate.monthValue, baseDate.dayOfMonth),
      LocalDate.of(targetDate.year, targetDate.monthValue, targetDate.dayOfMonth)
    ).toInt()
    return CENTER_POSITION + daysDiff
  }

  companion object {
    private const val TAG = "InfiniteDayViewPagerAdapter"
    // Center position for infinite scrolling
    const val CENTER_POSITION = Int.MAX_VALUE / 2
  }
}
