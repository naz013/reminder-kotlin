package com.elementary.tasks.reminder.build.valuedialog.controller.datetime

import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractSelectableArrayController

class DayOfMonthController(
  builderItem: BuilderItem<Int>
) : AbstractSelectableArrayController<Int,
  AbstractSelectableArrayController.SimpleSelectableValue<Int>>(
  builderItem = builderItem,
  multiChoice = false,
  numOfColumns = 7
) {

  override fun getAdapterData(): List<SimpleSelectableValue<Int>> {
    val selectedDay = builderItem.modifier.getValue()
    return (1..28).map {
      SimpleSelectableValue(value = it, uiValue = "$it", selectionState = selectedDay == it)
    } + SimpleSelectableValue(
      value = 0,
      uiValue = getContext().getString(R.string.last_day),
      selectionState = selectedDay == 0
    )
  }

  override fun updateValue(selectedItems: List<SimpleSelectableValue<Int>>) {
    if (selectedItems.isEmpty()) {
      updateValue(null)
    } else {
      updateValue(selectedItems[0].value)
    }
  }
}
