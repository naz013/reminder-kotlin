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
import androidx.compose.ui.text.input.KeyboardType
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.reminder.build.BeforeTimeBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.DayOfMonthBuilderItem
import com.elementary.tasks.reminder.build.DayOfYearBuilderItem
import com.elementary.tasks.reminder.build.DaysOfWeekBuilderItem
import com.elementary.tasks.reminder.build.DescriptionBuilderItem
import com.elementary.tasks.reminder.build.EmailBuilderItem
import com.elementary.tasks.reminder.build.EmailSubjectBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarDurationBuilderItem
import com.elementary.tasks.reminder.build.GoogleTaskListBuilderItem
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.ICalByDayBuilderItem
import com.elementary.tasks.reminder.build.ICalFrequencyBuilderItem
import com.elementary.tasks.reminder.build.ICalIntBuilderItem
import com.elementary.tasks.reminder.build.ICalListIntBuilderItem
import com.elementary.tasks.reminder.build.ICalStartDateBuilderItem
import com.elementary.tasks.reminder.build.ICalStartTimeBuilderItem
import com.elementary.tasks.reminder.build.ICalUntilDateBuilderItem
import com.elementary.tasks.reminder.build.ICalUntilTimeBuilderItem
import com.elementary.tasks.reminder.build.ICalWeekStartBuilderItem
import com.elementary.tasks.reminder.build.LedColorBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayDateBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayTimeBuilderItem
import com.elementary.tasks.reminder.build.OtherParamsBuilderItem
import com.elementary.tasks.reminder.build.PriorityBuilderItem
import com.elementary.tasks.reminder.build.RepeatIntervalBuilderItem
import com.elementary.tasks.reminder.build.RepeatLimitBuilderItem
import com.elementary.tasks.reminder.build.RepeatTimeBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.TimerBuilderItem
import com.elementary.tasks.reminder.build.WebAddressBuilderItem
import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.build.valuedialog.editor.BeforeTimeValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.CountdownTimeValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DateValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DayOfMonthValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DayOfYearValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DaysOfWeekValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.GoogleCalendarDurationValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.GoogleCalendarValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.GoogleTaskListValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.GroupValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.ICalDayValueListValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.ICalFreqValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.ICalIntListValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.ICalIntValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.ICalWeekStartValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.LedColorValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.OtherParamsValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.PriorityValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.RepeatIntervalValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.RepeatLimitValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.RepeatTimeValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.SimpleTextValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.TextInputValueEditor
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
    is PriorityBuilderItem,
    is RepeatLimitBuilderItem,
    is BeforeTimeBuilderItem,
    is RepeatTimeBuilderItem,
    is RepeatIntervalBuilderItem,
    is TimerBuilderItem,
    is GoogleCalendarDurationBuilderItem,
    is OtherParamsBuilderItem,
    is EmailBuilderItem,
    is WebAddressBuilderItem,
    is ICalStartDateBuilderItem,
    is ICalUntilDateBuilderItem,
    is ICalStartTimeBuilderItem,
    is ICalUntilTimeBuilderItem,
    is ICalFrequencyBuilderItem,
    is ICalWeekStartBuilderItem,
    is ICalIntBuilderItem,
    is ICalListIntBuilderItem,
    is ICalByDayBuilderItem,
    is GoogleCalendarBuilderItem,
    is SummaryBuilderItem,
    is DescriptionBuilderItem,
    is EmailSubjectBuilderItem,
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
  paramToTextAdapter: ParamToTextAdapter,
  googleCalendarUtils: GoogleCalendarUtils,
  modifier: Modifier = Modifier,
  is24HourFormat: Boolean = false,
  onHelpClick: (() -> Unit)? = null,
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
      if (onHelpClick != null) {
        IconButton(onClick = onHelpClick) {
          Icon(
            painter = painterResource(R.drawable.ic_builder_ical_help),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
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
        paramToTextAdapter = paramToTextAdapter,
        googleCalendarUtils = googleCalendarUtils,
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
  paramToTextAdapter: ParamToTextAdapter,
  googleCalendarUtils: GoogleCalendarUtils,
) {
  when (builderItem) {
    is DateBuilderItem -> DateValueEditor(builderItem, onValueChange)
    is LocationDelayDateBuilderItem -> DateValueEditor(builderItem, onValueChange)
    is ICalStartDateBuilderItem -> DateValueEditor(builderItem, onValueChange)
    is ICalUntilDateBuilderItem -> DateValueEditor(builderItem, onValueChange)
    is ICalStartTimeBuilderItem -> TimeValueEditor(builderItem, is24HourFormat, onValueChange)
    is ICalUntilTimeBuilderItem -> TimeValueEditor(builderItem, is24HourFormat, onValueChange)
    is ICalFrequencyBuilderItem -> ICalFreqValueEditor(builderItem, paramToTextAdapter, onValueChange)
    is ICalWeekStartBuilderItem -> ICalWeekStartValueEditor(builderItem, paramToTextAdapter, onValueChange)
    is ICalIntBuilderItem -> ICalIntValueEditor(builderItem, onValueChange)
    is ICalListIntBuilderItem -> ICalIntListValueEditor(builderItem, onValueChange)
    is ICalByDayBuilderItem -> ICalDayValueListValueEditor(builderItem, paramToTextAdapter, onValueChange)
    is TimeBuilderItem -> TimeValueEditor(builderItem, is24HourFormat, onValueChange)
    is LocationDelayTimeBuilderItem -> TimeValueEditor(builderItem, is24HourFormat, onValueChange)
    is GroupBuilderItem -> GroupValueEditor(builderItem, onValueChange)
    is GoogleTaskListBuilderItem -> GoogleTaskListValueEditor(builderItem, onValueChange)
    is LedColorBuilderItem -> LedColorValueEditor(builderItem, onValueChange)
    is DayOfMonthBuilderItem -> DayOfMonthValueEditor(builderItem, onValueChange)
    is DayOfYearBuilderItem -> DayOfYearValueEditor(builderItem, onValueChange)
    is DaysOfWeekBuilderItem -> DaysOfWeekValueEditor(builderItem, onValueChange)
    is PriorityBuilderItem -> PriorityValueEditor(builderItem, onValueChange)
    is RepeatLimitBuilderItem -> RepeatLimitValueEditor(builderItem, onValueChange)
    is BeforeTimeBuilderItem -> BeforeTimeValueEditor(builderItem, onValueChange)
    is RepeatTimeBuilderItem -> RepeatTimeValueEditor(builderItem, onValueChange)
    is RepeatIntervalBuilderItem -> RepeatIntervalValueEditor(builderItem, onValueChange)
    is TimerBuilderItem -> CountdownTimeValueEditor(builderItem, onValueChange)
    is GoogleCalendarDurationBuilderItem -> GoogleCalendarDurationValueEditor(builderItem, onValueChange)
    is OtherParamsBuilderItem -> OtherParamsValueEditor(builderItem, onValueChange)
    is EmailBuilderItem -> SimpleTextValueEditor(builderItem, onValueChange, KeyboardType.Email)
    is WebAddressBuilderItem -> SimpleTextValueEditor(builderItem, onValueChange, KeyboardType.Uri)
    is GoogleCalendarBuilderItem -> GoogleCalendarValueEditor(builderItem, googleCalendarUtils, onValueChange)
    is SummaryBuilderItem -> TextInputValueEditor(builderItem, onValueChange)
    is DescriptionBuilderItem -> TextInputValueEditor(builderItem, onValueChange)
    is EmailSubjectBuilderItem -> TextInputValueEditor(builderItem, onValueChange)
    else -> {}
  }
}
