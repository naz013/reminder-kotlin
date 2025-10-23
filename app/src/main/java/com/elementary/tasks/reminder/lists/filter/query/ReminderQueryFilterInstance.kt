package com.elementary.tasks.reminder.lists.filter.query

import com.elementary.tasks.core.filter.FilterInstance
import com.github.naz013.domain.Reminder

class ReminderQueryFilterInstance(private val query: String) : FilterInstance<Reminder> {
  override fun filter(t: Reminder): Boolean {
    if (query.isBlank()) return true
    return t.summary.contains(query, ignoreCase = true) || containsInDescription(t)
  }

  private fun containsInDescription(t: Reminder): Boolean {
    return t.description?.contains(query, ignoreCase = true) == true
  }
}
