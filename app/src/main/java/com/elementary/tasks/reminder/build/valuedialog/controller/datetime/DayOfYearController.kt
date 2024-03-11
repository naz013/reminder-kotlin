package com.elementary.tasks.reminder.build.valuedialog.controller.datetime

import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractSelectableArrayController

class DayOfYearController(
  builderItem: BuilderItem<Int>
) : AbstractSelectableArrayController<Int,
  AbstractSelectableArrayController.SimpleSelectableValue<Int>>(
  builderItem = builderItem,
  multiChoice = false,
  numOfColumns = 8
) {

  override fun getAdapterData(): List<SimpleSelectableValue<Int>> {
    val selectedDay = builderItem.modifier.getValue()
    return (1..365).map {
      SimpleSelectableValue(value = it, uiValue = "$it", selectionState = selectedDay == it)
    }
  }

  override fun updateValue(selectedItems: List<SimpleSelectableValue<Int>>) {
    if (selectedItems.isEmpty()) {
      updateValue(null)
    } else {
      updateValue(selectedItems[0].value)
    }
  }
}
