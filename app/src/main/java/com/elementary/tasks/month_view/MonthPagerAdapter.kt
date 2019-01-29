package com.elementary.tasks.month_view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MonthPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    var fragments: MutableList<MonthFragment> = mutableListOf()
        get() {
            if (field.isEmpty()) {
                for (i in 0 until count) {
                    field.add(MonthFragment())
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