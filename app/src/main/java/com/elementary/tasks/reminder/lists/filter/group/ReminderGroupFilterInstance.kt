package com.elementary.tasks.reminder.lists.filter.group

import com.elementary.tasks.core.filter.FilterInstance
import com.github.naz013.domain.Reminder

class ReminderGroupFilterInstance(private val ids: Set<String>) : FilterInstance<Reminder> {
  override fun filter(t: Reminder): Boolean {
    if (ids.isEmpty()) return true
    return t.groupUuId.isNotEmpty() && ids.contains(t.groupUuId)
  }
}
