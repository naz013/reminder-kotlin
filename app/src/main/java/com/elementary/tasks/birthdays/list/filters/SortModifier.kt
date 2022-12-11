package com.elementary.tasks.birthdays.list.filters

import com.elementary.tasks.birthdays.list.BirthdayListItem
import com.elementary.tasks.core.filter.Modifier

class SortModifier(
  modifier: Modifier<BirthdayListItem>? = null,
  callback: ((List<BirthdayListItem>) -> Unit)? = null
) : Modifier<BirthdayListItem>(modifier, callback) {

  override fun apply(data: List<BirthdayListItem>): List<BirthdayListItem> {
    return sort(super.apply(data))
  }

  private fun sort(data: List<BirthdayListItem>): List<BirthdayListItem> {
    return data.sortedBy { it.nextBirthdayDate }
  }
}
