package com.elementary.tasks.reminder.build.valuedialog.controller.google

import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.reminder.build.GoogleTaskListBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractSelectableRadioController
import com.elementary.tasks.reminder.build.valuedialog.controller.core.SelectableValue

class GoogleTaskListController(
  private val googleTaskListBuilderItem: GoogleTaskListBuilderItem
) : AbstractSelectableRadioController<GoogleTaskList,
  GoogleTaskListController.GoogleTaskListSelectableValue>(
  builderItem = googleTaskListBuilderItem
) {

  override fun clearValue() {
    builderItem.modifier.update(null)
  }

  override fun getAdapterData(): List<GoogleTaskListSelectableValue> {
    val value = builderItem.modifier.getValue()?.listId
    return googleTaskListBuilderItem.taskLists.map {
      GoogleTaskListSelectableValue(it, it.listId == value)
    }
  }

  override fun updateValue(selected: GoogleTaskListSelectableValue?) {
    updateValue(selected?.taskList)
  }

  data class GoogleTaskListSelectableValue(
    val taskList: GoogleTaskList,
    var selectionState: Boolean
  ) : SelectableValue {

    override fun getTitle(): String {
      return taskList.title
    }

    override fun isSelected(): Boolean {
      return selectionState
    }

    override fun setSelected(isSelected: Boolean) {
      this.selectionState = isSelected
    }
  }
}
