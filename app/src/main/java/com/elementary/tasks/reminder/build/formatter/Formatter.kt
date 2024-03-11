package com.elementary.tasks.reminder.build.formatter

abstract class Formatter<T> {
  open fun format(t: T): String {
    return t.toString()
  }
}
