package com.elementary.tasks.reminder.lists.filters

import com.elementary.tasks.core.data.models.Reminder

import java.util.ArrayList
import java.util.Collections

/**
 * Copyright 2017 Nazar Suhovich
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
class ReminderFilterController(private val mCallback: FilterCallback<Reminder>?) {

    private val searchValue = FilterValue<String>()
    private val groupValue = FilterValue<List<String>>()
    private val typeValue = FilterValue<Int>()
    private val statusValue = FilterValue<Int>()
    private val rangeValue = FilterValue<DateFilter.DateRange>()

    private var original: MutableList<Reminder> = ArrayList()
    private var mFilter: ObjectFilter<Reminder>? = null

    init {
        initFilters()
    }

    private fun initFilters() {
        val searchFilter = SearchFilter(null)
        searchValue.subscribe(searchFilter)

        val dateFilter = DateFilter(searchFilter)
        rangeValue.subscribe(dateFilter)

        val groupFilter = GroupFilter(dateFilter)
        groupValue.subscribe(groupFilter)

        val typeFilter = TypeFilter(groupFilter)
        typeValue.subscribe(typeFilter)

        val statusFilter = StatusFilter(typeFilter)
        statusValue.subscribe(statusFilter)

        this.mFilter = statusFilter
    }

    fun setSearchValue(value: String?) {
        if (value == null) {
            searchValue.setValue("")
        } else {
            searchValue.setValue(value)
        }
        onChanged()
    }

    fun setGroupValue(value: String?) {
        if (value == null) {
            groupValue.setValue(listOf(""))
        } else {
            groupValue.setValue(listOf(value))
        }
        onChanged()
    }

    fun setGroupValues(value: List<String>) {
        groupValue.setValue(value)
        onChanged()
    }

    fun setTypeValue(value: Int) {
        typeValue.setValue(value)
        onChanged()
    }

    fun setStatusValue(value: Int) {
        statusValue.setValue(value)
        onChanged()
    }

    fun setRangeValue(value: Int) {
        when (value) {
            0 -> this.rangeValue.setValue(DateFilter.DateRange.ALL)
            1 -> this.rangeValue.setValue(DateFilter.DateRange.PERMANENT)
            2 -> this.rangeValue.setValue(DateFilter.DateRange.TODAY)
            3 -> this.rangeValue.setValue(DateFilter.DateRange.TOMORROW)
        }
        onChanged()
    }

    fun getOriginal(): List<Reminder> {
        return original
    }

    fun setOriginal(original: MutableList<Reminder>) {
        this.original = original
        onChanged()
    }

    private fun onChanged() {
        val list = ArrayList<Reminder>()
        for (reminder in original) {
            if (mFilter != null) {
                if (mFilter!!.filter(reminder)) list.add(reminder)
            } else
                list.add(reminder)
        }
        mCallback?.onChanged(list)
    }

    fun remove(reminder: Reminder) {
        if (original.contains(reminder)) {
            original.remove(reminder)
            onChanged()
        }
    }
}
