package com.elementary.tasks.birthdays

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
class CalendarPagerAdapter(fm: FragmentManager, datas: List<EventsPagerItem>) : FragmentPagerAdapter(fm) {

    private val datas = ArrayList<EventsPagerItem>()

    init {
        this.datas.addAll(datas)
    }

    override fun getItem(position: Int): Fragment {
        return EventsListFragment.newInstance(datas[position])
    }

    override fun getCount(): Int {
        return datas.size
    }
}
