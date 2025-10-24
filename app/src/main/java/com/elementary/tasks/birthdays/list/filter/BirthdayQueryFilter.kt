package com.elementary.tasks.birthdays.list.filter

import com.elementary.tasks.core.filter.FilterInstance
import com.github.naz013.domain.Birthday

class BirthdayQueryFilter(private val query: String) : FilterInstance<Birthday> {
  override fun filter(t: Birthday): Boolean {
    if (query.isBlank()) return true
    return t.name.contains(query, ignoreCase = true)
  }
}
