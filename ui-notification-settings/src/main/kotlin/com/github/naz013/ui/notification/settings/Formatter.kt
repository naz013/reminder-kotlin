package com.github.naz013.ui.notification.settings

abstract class Formatter<T> {
  open fun format(t: T): String = t.toString()
}
