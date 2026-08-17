package com.github.naz013.feature.reminder.lists.filter.query

import com.github.naz013.feature.reminder.lists.filter.FilterInstance
import com.github.naz013.domain.reminder.v2.ReminderV2

class ReminderV2QueryFilterInstance(
  private val query: String,
) : FilterInstance<ReminderV2> {
  override fun filter(t: ReminderV2): Boolean {
    if (query.isBlank()) return true
    return t.summary.contains(query, ignoreCase = true) || containsInDescription(t)
  }

  private fun containsInDescription(t: ReminderV2): Boolean = t.description?.contains(query, ignoreCase = true) == true
}
