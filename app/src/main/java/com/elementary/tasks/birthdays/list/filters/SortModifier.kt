package com.elementary.tasks.birthdays.list.filters

import com.elementary.tasks.core.data.ui.UiBirthdayList
import com.elementary.tasks.core.filter.Modifier

class SortModifier(
  modifier: Modifier<UiBirthdayList>? = null,
  callback: ((List<UiBirthdayList>) -> Unit)? = null
) : Modifier<UiBirthdayList>(modifier, callback) {

  override fun apply(data: List<UiBirthdayList>): List<UiBirthdayList> {
    return sort(super.apply(data))
  }

  private fun sort(data: List<UiBirthdayList>): List<UiBirthdayList> {
    return data.sortedBy { it.nextBirthdayDate }
  }
}
