package com.elementary.tasks.reminder.build.valuedialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.DayOfMonthBuilderItem
import com.elementary.tasks.reminder.build.DayOfYearBuilderItem
import com.elementary.tasks.reminder.build.DaysOfWeekBuilderItem
import com.elementary.tasks.reminder.build.GoogleTaskListBuilderItem
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.LedColorBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayDateBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayTimeBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.valuedialog.editor.DateValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DayOfMonthValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DayOfYearValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DaysOfWeekValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.GoogleTaskListValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.GroupValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.LedColorValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.TimeValueEditor
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet

/**
 * Whether [ValueEditorSheet] has a Compose editor for this item's type yet. `BuildReminderFragment`
 * falls back to the legacy `ValueDialog` (a `BottomSheetDialogFragment`) for anything not listed
 * here - this list grows as more `ValueController` subclasses are ported, and the fallback is
 * removed once it covers every `BuilderItem` type `ValueControllerFactory` handles.
 */
fun isSupportedByComposeEditor(builderItem: BuilderItem<*>): Boolean =
  when (builderItem) {
    is DateBuilderItem,
    is LocationDelayDateBuilderItem,
    is TimeBuilderItem,
    is LocationDelayTimeBuilderItem,
    is GroupBuilderItem,
    is GoogleTaskListBuilderItem,
    is LedColorBuilderItem,
    is DayOfMonthBuilderItem,
    is DayOfYearBuilderItem,
    is DaysOfWeekBuilderItem,
    -> true

    else -> false
  }

/**
 * The reminder builder's "edit value" bottom sheet: title + close button, optional description,
 * and a type-specific editor body. This is the Compose replacement for `ValueDialog` +
 * `ValueControllerFactory` - each editor below owns its own local UI state and calls
 * [onValueChange] with the mutated [BuilderItem] on every change (matching the legacy
 * `AbstractViewValueController.updateValue`'s live-as-you-edit propagation, not just on close).
 *
 * The legacy explicit Clear/Save button row (`buttons_holder`) is intentionally not reproduced -
 * it was never made visible in the source layout, so every edit already commits live and closing
 * the sheet (any way: close button, tap-outside, back) is enough.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValueEditorSheet(
  builderItem: BuilderItem<*>,
  onDismissRequest: () -> Unit,
  onValueChange: (BuilderItem<*>) -> Unit,
  modifier: Modifier = Modifier,
  is24HourFormat: Boolean = false,
) {
  AppModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 24.dp, end = 16.dp, top = 24.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = builderItem.title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      IconButton(onClick = onDismissRequest) {
        Icon(
          painter = painterResource(R.drawable.ic_builder_chevron_down),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    }

    val description = builderItem.description
    if (!description.isNullOrEmpty()) {
      Text(
        text = description,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 24.dp, end = 24.dp, top = 4.dp),
      )
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 24.dp, end = 24.dp, top = 8.dp),
    ) {
      ValueEditorContent(
        builderItem = builderItem,
        onValueChange = onValueChange,
        is24HourFormat = is24HourFormat,
      )
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun ValueEditorContent(
  builderItem: BuilderItem<*>,
  onValueChange: (BuilderItem<*>) -> Unit,
  is24HourFormat: Boolean,
) {
  when (builderItem) {
    is DateBuilderItem -> DateValueEditor(builderItem, onValueChange)
    is LocationDelayDateBuilderItem -> DateValueEditor(builderItem, onValueChange)
    is TimeBuilderItem -> TimeValueEditor(builderItem, is24HourFormat, onValueChange)
    is LocationDelayTimeBuilderItem -> TimeValueEditor(builderItem, is24HourFormat, onValueChange)
    is GroupBuilderItem -> GroupValueEditor(builderItem, onValueChange)
    is GoogleTaskListBuilderItem -> GoogleTaskListValueEditor(builderItem, onValueChange)
    is LedColorBuilderItem -> LedColorValueEditor(builderItem, onValueChange)
    is DayOfMonthBuilderItem -> DayOfMonthValueEditor(builderItem, onValueChange)
    is DayOfYearBuilderItem -> DayOfYearValueEditor(builderItem, onValueChange)
    is DaysOfWeekBuilderItem -> DaysOfWeekValueEditor(builderItem, onValueChange)
    else -> {}
  }
}
