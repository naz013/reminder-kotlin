package com.elementary.tasks.reminder.lists.filters

import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.data.ui.UiReminderListActiveGps
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.data.ui.UiReminderListRemoved
import com.elementary.tasks.core.data.ui.UiReminderListRemovedGps
import com.elementary.tasks.core.data.ui.UiReminderListRemovedShop
import com.elementary.tasks.core.filter.Modifier

class SearchModifier(
  modifier: Modifier<UiReminderList>? = null,
  callback: ((List<UiReminderList>) -> Unit)? = null
) : Modifier<UiReminderList>(modifier, callback) {

  private var searchValue: String = ""

  override fun apply(data: List<UiReminderList>) = if (searchValue.isEmpty()) {
    data
  } else {
    data.filter { filter(it) }
  }

  private fun filter(v: UiReminderList): Boolean {
    val searchableText = when (v) {
      is UiReminderListActiveShop -> v.summary
      is UiReminderListRemovedShop -> v.summary
      is UiReminderListActive -> v.summary
      is UiReminderListRemoved -> v.summary
      is UiReminderListActiveGps -> v.summary
      is UiReminderListRemovedGps -> v.summary
      else -> ""
    }
    return searchableText.lowercase().contains(searchValue.lowercase())
  }

  fun setSearchValue(value: String?) {
    searchValue = value ?: ""
    onChanged()
  }
}