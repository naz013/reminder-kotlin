package com.elementary.tasks.reminder.build.valuedialog.controller.ical

import com.elementary.tasks.R
import com.github.naz013.ui.common.context.startActivity
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractSelectableArrayController
import com.elementary.tasks.reminder.recur.RecurHelpActivity

class ICalIntListController(
  builderItem: BuilderItem<List<Int>>,
  private val array: List<Int>
) : AbstractSelectableArrayController<List<Int>,
  AbstractSelectableArrayController.SimpleSelectableValue<Int>>(
  builderItem = builderItem,
  multiChoice = true,
  numOfColumns = 8
) {

  override fun getAdapterData(): List<SimpleSelectableValue<Int>> {
    val selected = builderItem.modifier.getValue()?.associate { it to it } ?: emptyMap()
    return array.map {
      SimpleSelectableValue(value = it, uiValue = "$it", selectionState = selected.containsKey(it))
    }
  }

  override fun updateValue(selectedItems: List<SimpleSelectableValue<Int>>) {
    if (selectedItems.isEmpty()) {
      updateValue(null)
    } else {
      updateValue(selectedItems.map { it.value })
    }
  }

  override fun onViewCreated() {
    super.onViewCreated()
    addOptionalButton(R.drawable.ic_builder_ical_help)
  }

  override fun onOptionalClicked() {
    getContext().startActivity(RecurHelpActivity::class.java)
  }
}
