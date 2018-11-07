package com.elementary.tasks.dayView.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.elementary.tasks.dayView.day.EventsListFragment

class DayPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    var fragments: MutableList<EventsListFragment> = mutableListOf()
        get() {
            if (field.isEmpty()) {
                for (i in 0 until count) {
                    field.add(EventsListFragment())
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