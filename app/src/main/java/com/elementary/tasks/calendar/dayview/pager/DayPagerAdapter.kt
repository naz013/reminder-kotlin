package com.elementary.tasks.calendar.dayview.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.elementary.tasks.calendar.dayview.day.DayEventsListFragment

class DayPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

  var fragments: MutableList<DayEventsListFragment> = mutableListOf()
    get() {
      if (field.isEmpty()) {
        for (i in 0 until count) {
          field.add(DayEventsListFragment())
        }
      }
      return field
    }

  override fun getItem(position: Int): Fragment {
    return fragments[position]
  }

  override fun getCount(): Int {
    return 4
  }
}
