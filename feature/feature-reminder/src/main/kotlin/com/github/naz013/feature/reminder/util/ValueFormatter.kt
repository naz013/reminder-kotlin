package com.github.naz013.feature.reminder.util

interface ValueFormatter<T> {
  fun format(value: T): String
}
