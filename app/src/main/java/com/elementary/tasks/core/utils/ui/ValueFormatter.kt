package com.elementary.tasks.core.utils.ui

interface ValueFormatter<T> {
  fun format(value: T): String
}
