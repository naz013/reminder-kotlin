package com.elementary.tasks.reminder.build.valuedialog.controller.extra

import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractSelectableRadioController
import com.elementary.tasks.reminder.build.valuedialog.controller.core.SelectableValue

class GroupController(
  private val groupBuilderItem: GroupBuilderItem
) : AbstractSelectableRadioController<UiGroupList, GroupController.GroupSelectableValue>(
  builderItem = groupBuilderItem
) {

  override fun clearValue() {
    builderItem.modifier.update(groupBuilderItem.defaultGroup)
  }

  override fun getAdapterData(): List<GroupSelectableValue> {
    val value = builderItem.modifier.getValue()?.id
    return groupBuilderItem.groups.map {
      GroupSelectableValue(it, it.id == value)
    }
  }

  override fun updateValue(selected: GroupSelectableValue?) {
    updateValue(selected?.group)
  }

  data class GroupSelectableValue(
    val group: UiGroupList,
    var selectionState: Boolean
  ) : SelectableValue {

    override fun getTitle(): String {
      return group.title
    }

    override fun isSelected(): Boolean {
      return selectionState
    }

    override fun setSelected(isSelected: Boolean) {
      this.selectionState = isSelected
    }
  }
}
