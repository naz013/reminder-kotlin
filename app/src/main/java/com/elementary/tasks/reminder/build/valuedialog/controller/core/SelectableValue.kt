package com.elementary.tasks.reminder.build.valuedialog.controller.core

interface SelectableValue {
  fun getTitle(): String
  fun isSelected(): Boolean
  fun setSelected(isSelected: Boolean)
}
