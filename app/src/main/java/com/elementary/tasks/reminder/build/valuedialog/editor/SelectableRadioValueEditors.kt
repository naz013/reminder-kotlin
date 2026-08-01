package com.elementary.tasks.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.CategoryBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarBuilderItem
import com.elementary.tasks.reminder.build.GoogleTaskListBuilderItem
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.LockScreenVisibilityBuilderItem
import com.github.naz013.ui.common.compose.foundation.component.WheelPicker

/** Single-select group picker. Replaces `GroupController`, which resets to the item's default
 *  group on clear rather than to no selection - there's no explicit "clear" affordance here since
 *  the legacy Clear button was never shown, but the default-on-clear intent is preserved by
 *  seeding local state from the current value, which already falls back to the default group. */
@Composable
fun GroupValueEditor(
  builderItem: GroupBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val groups = builderItem.groups
  val initial = remember(builderItem) { builderItem.modifier.getValue() ?: builderItem.defaultGroup }
  var selectedIndex by remember(builderItem) {
    mutableIntStateOf(groups.indexOf(initial).coerceAtLeast(0))
  }
  WheelPicker(
    items = groups.map { it.title },
    selectedIndex = selectedIndex,
    onSelectedIndexChange = { index ->
      val group = groups.getOrNull(index)
      if (group != null) {
        selectedIndex = index
        builderItem.modifier.update(group)
        onValueChange(builderItem)
      }
    },
    modifier = Modifier.fillMaxWidth(),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
}

/** Single-select Google Task list picker. Replaces `GoogleTaskListController`. */
@Composable
fun GoogleTaskListValueEditor(
  builderItem: GoogleTaskListBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val taskLists = builderItem.taskLists
  val initial = remember(builderItem) { builderItem.modifier.getValue() }
  var selectedIndex by remember(builderItem) {
    mutableIntStateOf(taskLists.indexOf(initial).coerceAtLeast(0))
  }
  WheelPicker(
    items = taskLists.map { it.title },
    selectedIndex = selectedIndex,
    onSelectedIndexChange = { index ->
      val taskList = taskLists.getOrNull(index)
      if (taskList != null) {
        selectedIndex = index
        builderItem.modifier.update(taskList)
        onValueChange(builderItem)
      }
    },
    modifier = Modifier.fillMaxWidth(),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
}

/** Notification category wheel (Default/Alarm/Event/Call). */
@Composable
fun CategoryValueEditor(
  builderItem: CategoryBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val items = listOf(
    stringResource(R.string.notification_category_default),
    stringResource(R.string.notification_category_alarm),
    stringResource(R.string.notification_category_event),
    stringResource(R.string.notification_category_call),
  )
  var selectedIndex by remember(builderItem) {
    mutableIntStateOf(builderItem.modifier.getValue() ?: 0)
  }
  WheelPicker(
    items = items,
    selectedIndex = selectedIndex,
    onSelectedIndexChange = { index ->
      selectedIndex = index
      builderItem.modifier.update(index)
      onValueChange(builderItem)
    },
    modifier = Modifier.fillMaxWidth(),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
}

/** Lock-screen visibility wheel (Show full content/Hide sensitive content/Hide completely). */
@Composable
fun LockScreenVisibilityValueEditor(
  builderItem: LockScreenVisibilityBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val items = listOf(
    stringResource(R.string.lock_screen_visibility_public),
    stringResource(R.string.lock_screen_visibility_private),
    stringResource(R.string.lock_screen_visibility_secret),
  )
  var selectedIndex by remember(builderItem) {
    mutableIntStateOf(builderItem.modifier.getValue() ?: 1)
  }
  WheelPicker(
    items = items,
    selectedIndex = selectedIndex,
    onSelectedIndexChange = { index ->
      selectedIndex = index
      builderItem.modifier.update(index)
      onValueChange(builderItem)
    },
    modifier = Modifier.fillMaxWidth(),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
}

/** Single-select Google Calendar picker. Replaces `GoogleCalendarController`. Unlike the other
 *  selectable items, the option list isn't on the [BuilderItem] itself - it's fetched (a
 *  synchronous ContentResolver query) once per edit session via [GoogleCalendarUtils]. */
@Composable
fun GoogleCalendarValueEditor(
  builderItem: GoogleCalendarBuilderItem,
  googleCalendarUtils: GoogleCalendarUtils,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val calendars = remember(builderItem) { googleCalendarUtils.getCalendarsList() }
  val initial = remember(builderItem) { builderItem.modifier.getValue() }
  var selectedIndex by remember(builderItem) {
    mutableIntStateOf(calendars.indexOf(initial).coerceAtLeast(0))
  }
  WheelPicker(
    items = calendars.map { it.name },
    selectedIndex = selectedIndex,
    onSelectedIndexChange = { index ->
      val calendar = calendars.getOrNull(index)
      if (calendar != null) {
        selectedIndex = index
        builderItem.modifier.update(calendar)
        onValueChange(builderItem)
      }
    },
    modifier = Modifier.fillMaxWidth(),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
}
