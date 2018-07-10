package com.elementary.tasks.core.calendar

import android.os.Bundle
import androidx.annotation.IntRange
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.core.views.MonthView
import com.elementary.tasks.databinding.FragmentDateGridBinding

import java.util.HashMap
import androidx.fragment.app.Fragment
import hirondelle.date4j.DateTime

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
class DateGridFragment : Fragment() {

    private var monthView: MonthView? = null

    private var eventsMap: Map<DateTime, Events> = HashMap()
    @IntRange(from = 1, to = 12)
    private var month: Int = 0
    private var year: Int = 0

    private var onItemClickListener: MonthView.OnDateClick? = null
    private var onItemLongClickListener: MonthView.OnDateLongClick? = null

    fun setDateTime(dateTime: DateTime) {
        this.month = dateTime.month!!
        this.year = dateTime.year!!
        if (monthView != null) {
            monthView!!.setDate(year, month)
            monthView!!.invalidate()
        }
    }

    fun setDate(month: Int, year: Int) {
        this.month = month
        this.year = year
        if (monthView != null) {
            monthView!!.setDate(year, month)
            monthView!!.invalidate()
        }
    }

    fun setEventsMap(eventsMap: Map<DateTime, Events>) {
        this.eventsMap = eventsMap
    }

    fun setOnItemClickListener(onItemClickListener: MonthView.OnDateClick) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnItemLongClickListener(onItemLongClickListener: MonthView.OnDateLongClick) {
        this.onItemLongClickListener = onItemLongClickListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = FragmentDateGridBinding.inflate(inflater, container, false)
        monthView = v.monthView
        if (year != 0) {
            monthView!!.setDate(year, month)
        }
        monthView!!.setEventsMap(eventsMap)
        if (onItemClickListener != null) {
            monthView!!.setDateClick(onItemClickListener)
        }
        if (onItemLongClickListener != null) {
            monthView!!.setDateLongClick(onItemLongClickListener)
        }
        monthView!!.invalidate()
        return v.root
    }
}
