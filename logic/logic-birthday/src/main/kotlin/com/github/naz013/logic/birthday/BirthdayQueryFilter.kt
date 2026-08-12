package com.github.naz013.logic.birthday

import com.github.naz013.domain.Birthday

class BirthdayQueryFilter(
  private val query: String,
) : (Birthday) -> Boolean {
  override fun invoke(t: Birthday): Boolean {
    if (query.isBlank()) return true
    return t.name.contains(query, ignoreCase = true)
  }
}
