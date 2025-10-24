package com.elementary.tasks.core.filter

interface FilterInstance<T> : (T) -> Boolean {
  fun filter(t: T): Boolean

  override fun invoke(p1: T): Boolean {
    return filter(p1)
  }
}
