package com.github.naz013.ui.common.format

interface ValueFormatter<T> {
  fun format(value: T): String
}
