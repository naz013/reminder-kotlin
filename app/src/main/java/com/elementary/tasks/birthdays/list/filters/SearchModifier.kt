package com.elementary.tasks.birthdays.list.filters

import com.elementary.tasks.core.data.ui.UiBirthdayList
import com.elementary.tasks.core.filter.Modifier

class SearchModifier(
  modifier: Modifier<UiBirthdayList>? = null,
  callback: ((List<UiBirthdayList>) -> Unit)? = null
) : Modifier<UiBirthdayList>(modifier, callback) {

  private var searchValue: String = ""

  override fun apply(data: List<UiBirthdayList>): List<UiBirthdayList> {
    val list = mutableListOf<UiBirthdayList>()
    for (name in super.apply(data)) {
      if (filter(name)) list.add(name)
    }
    return list
  }

  private fun filter(v: UiBirthdayList): Boolean {
    return searchValue.isEmpty() || v.name.toLowerCase().contains(searchValue.toLowerCase())
  }

  fun setSearchValue(value: String?) {
    searchValue = value ?: ""
    onChanged()
  }
}
