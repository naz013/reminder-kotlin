package com.elementary.tasks.core.calendar

import java.util.ArrayList

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class MonthPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    var fragments: MutableList<DateGridFragment> = mutableListOf()
        get() {
            if (field.isEmpty()) {
                this.fragments = ArrayList()
                for (i in 0 until count) {
                    field.add(DateGridFragment())
                }
            }
            return field
        }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return FlextCalendarFragment.NUMBER_OF_PAGES
    }
}
