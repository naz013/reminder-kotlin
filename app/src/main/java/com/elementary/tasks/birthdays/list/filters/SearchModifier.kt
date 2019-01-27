package com.elementary.tasks.birthdays.list.filters

import com.elementary.tasks.core.filter.Modifier
import com.elementary.tasks.core.data.models.Birthday

class SearchModifier(modifier: Modifier<Birthday>? = null,
                     callback: ((List<Birthday>) -> Unit)? = null) : Modifier<Birthday>(modifier, callback) {

    private var searchValue: String = ""

    override fun apply(data: List<Birthday>): List<Birthday> {
        val list = mutableListOf<Birthday>()
        for (note in super.apply(data)) {
            if (filter(note)) list.add(note)
        }
        return list
    }

    private fun filter(v: Birthday): Boolean {
        return searchValue.isEmpty() || v.name.toLowerCase().contains(searchValue.toLowerCase())
    }

    fun setSearchValue(value: String?) {
        searchValue = value ?: ""
        onChanged()
    }
}