package com.elementary.tasks.birthdays.list.filters

import com.elementary.tasks.core.filter.Modifier
import com.elementary.tasks.core.data.models.Birthday

class SortModifier(modifier: Modifier<Birthday>? = null,
                   callback: ((List<Birthday>) -> Unit)? = null) : Modifier<Birthday>(modifier, callback) {

    override fun apply(data: List<Birthday>): List<Birthday> {
        return sort(super.apply(data))
    }

    private fun sort(data: List<Birthday>): List<Birthday> {
        return data.sortedBy { it.day }.sortedBy { it.month }
    }
}