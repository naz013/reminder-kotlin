package com.elementary.tasks.birthdays.list.filters

import com.elementary.tasks.birthdays.list.BirthdayListItem
import com.elementary.tasks.core.filter.Modifier

class SearchModifier(
  modifier: Modifier<BirthdayListItem>? = null,
  callback: ((List<BirthdayListItem>) -> Unit)? = null
) : Modifier<BirthdayListItem>(modifier, callback) {

  private var searchValue: String = ""

  override fun apply(data: List<BirthdayListItem>): List<BirthdayListItem> {
    val list = mutableListOf<BirthdayListItem>()
    for (name in super.apply(data)) {
      if (filter(name)) list.add(name)
    }
    return list
  }

  private fun filter(v: BirthdayListItem): Boolean {
    return searchValue.isEmpty() || v.name.toLowerCase().contains(searchValue.toLowerCase())
  }

  fun setSearchValue(value: String?) {
    searchValue = value ?: ""
    onChanged()
  }
}