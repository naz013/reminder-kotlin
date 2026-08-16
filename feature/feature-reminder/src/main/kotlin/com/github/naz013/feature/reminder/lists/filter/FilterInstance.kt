package com.github.naz013.feature.reminder.lists.filter

interface FilterInstance<T> : (T) -> Boolean {
  fun filter(t: T): Boolean

  override fun invoke(p1: T): Boolean = filter(p1)
}
