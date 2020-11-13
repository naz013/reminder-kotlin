package com.elementary.tasks.reminder.lists.filters

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.filter.Modifier

class SearchModifier(modifier: Modifier<Reminder>? = null,
                     callback: ((List<Reminder>) -> Unit)? = null) : Modifier<Reminder>(modifier, callback) {

  private var searchValue: String = ""

  override fun apply(data: List<Reminder>): List<Reminder> {
    val list = mutableListOf<Reminder>()
    for (note in super.apply(data)) {
      if (filter(note)) list.add(note)
    }
    return list
  }

  private fun filter(v: Reminder): Boolean {
    return searchValue.isEmpty() || v.summary.toLowerCase().contains(searchValue.toLowerCase())
  }

  fun setSearchValue(value: String?) {
    searchValue = value ?: ""
    onChanged()
  }
}