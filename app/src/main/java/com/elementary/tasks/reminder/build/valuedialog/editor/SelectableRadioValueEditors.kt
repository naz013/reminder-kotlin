package com.elementary.tasks.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.GoogleTaskListBuilderItem
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.ui.common.compose.foundation.component.SelectableRadioList

private val LIST_MAX_HEIGHT = 360.dp

/** Single-select group picker. Replaces `GroupController`, which resets to the item's default
 *  group on clear rather than to no selection - there's no explicit "clear" affordance here since
 *  the legacy Clear button was never shown, but the default-on-clear intent is preserved by
 *  seeding local state from the current value, which already falls back to the default group. */
@Composable
fun GroupValueEditor(
  builderItem: GroupBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  var selected by remember(builderItem) {
    mutableStateOf(builderItem.modifier.getValue() ?: builderItem.defaultGroup)
  }
  SelectableRadioList(
    items = builderItem.groups,
    selectedItem = selected,
    onItemSelected = { group ->
      selected = group
      builderItem.modifier.update(group)
      onValueChange(builderItem)
    },
    itemLabel = UiGroupList::title,
    itemKey = { it.id },
    modifier = Modifier.fillMaxWidth().heightIn(max = LIST_MAX_HEIGHT),
  )
}

/** Single-select Google Task list picker. Replaces `GoogleTaskListController`. */
@Composable
fun GoogleTaskListValueEditor(
  builderItem: GoogleTaskListBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  var selected by remember(builderItem) { mutableStateOf(builderItem.modifier.getValue()) }
  SelectableRadioList(
    items = builderItem.taskLists,
    selectedItem = selected,
    onItemSelected = { taskList ->
      selected = taskList
      builderItem.modifier.update(taskList)
      onValueChange(builderItem)
    },
    itemLabel = GoogleTaskList::title,
    itemKey = { it.listId },
    modifier = Modifier.fillMaxWidth().heightIn(max = LIST_MAX_HEIGHT),
  )
}
