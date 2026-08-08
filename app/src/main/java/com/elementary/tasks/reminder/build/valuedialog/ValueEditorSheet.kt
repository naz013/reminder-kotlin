package com.elementary.tasks.reminder.build.valuedialog

import android.net.Uri
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.ApplicationBuilderItem
import com.elementary.tasks.reminder.build.AttachmentsBuilderItem
import com.elementary.tasks.reminder.build.BeforeTimeBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.BypassDndBuilderItem
import com.elementary.tasks.reminder.build.CategoryBuilderItem
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.DayOfMonthBuilderItem
import com.elementary.tasks.reminder.build.DayOfYearBuilderItem
import com.elementary.tasks.reminder.build.DaysOfWeekBuilderItem
import com.elementary.tasks.reminder.build.DelayMinutesBuilderItem
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
import com.elementary.tasks.reminder.build.LockScreenVisibilityBuilderItem
import com.elementary.tasks.reminder.build.NoteBuilderItem
import com.elementary.tasks.reminder.build.OtherParamsBuilderItem
import com.elementary.tasks.reminder.build.PhoneCallBuilderItem
import com.elementary.tasks.reminder.build.PriorityBuilderItem
import com.elementary.tasks.reminder.build.RepeatIntervalBuilderItem
import com.elementary.tasks.reminder.build.RepeatLimitBuilderItem
import com.elementary.tasks.reminder.build.RepeatTimeBuilderItem
import com.elementary.tasks.reminder.build.SmsBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.TimerBuilderItem
import com.elementary.tasks.reminder.build.TimerExclusionBuilderItem
import com.elementary.tasks.reminder.build.VibrationPatternBuilderItem
import com.elementary.tasks.reminder.build.WakeScreenBuilderItem
import com.elementary.tasks.reminder.build.WebAddressBuilderItem
import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.elementary.tasks.reminder.build.valuedialog.editor.ApplicationValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.AttachmentsValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.BeforeTimeValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.BypassDndValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.CategoryValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.CountdownExclusionValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.CountdownTimeValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DateValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DayOfMonthValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DayOfYearValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DaysOfWeekValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.DelayMinutesValueEditor
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
import com.elementary.tasks.reminder.build.valuedialog.editor.LockScreenVisibilityValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.NoteValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.OtherParamsValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.PhoneInputValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.PriorityValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.RepeatIntervalValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.RepeatLimitValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.RepeatTimeValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.SimpleTextValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.SubTasksValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.TextInputValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.TimeValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.VibrationPatternValueEditor
import com.elementary.tasks.reminder.build.valuedialog.editor.WakeScreenValueEditor
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet

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
  googleCalendarApi: GoogleCalendarApi,
  packageManagerWrapper: PackageManagerWrapper,
  attachmentFileAdapter: UriToAttachmentFileAdapter,
  dateTimeManager: DateTimeManager,
  onPickApplication: () -> Unit,
  onPickContact: (onResult: (phone: String) -> Unit) -> Unit,
  onPickFiles: (onResult: (List<Uri>) -> Unit) -> Unit,
  modifier: Modifier = Modifier,
  is24HourFormat: Boolean = false,
  hapticFeedbackEnabled: Boolean = true,
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
        hapticFeedbackEnabled = hapticFeedbackEnabled,
        paramToTextAdapter = paramToTextAdapter,
        googleCalendarApi = googleCalendarApi,
        packageManagerWrapper = packageManagerWrapper,
        attachmentFileAdapter = attachmentFileAdapter,
        dateTimeManager = dateTimeManager,
        onPickApplication = onPickApplication,
        onPickContact = onPickContact,
        onPickFiles = onPickFiles,
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
  hapticFeedbackEnabled: Boolean,
  paramToTextAdapter: ParamToTextAdapter,
  googleCalendarApi: GoogleCalendarApi,
  packageManagerWrapper: PackageManagerWrapper,
  attachmentFileAdapter: UriToAttachmentFileAdapter,
  dateTimeManager: DateTimeManager,
  onPickApplication: () -> Unit,
  onPickContact: (onResult: (phone: String) -> Unit) -> Unit,
  onPickFiles: (onResult: (List<Uri>) -> Unit) -> Unit,
) {
  when (builderItem) {
    is DateBuilderItem -> DateValueEditor(builderItem, onValueChange)
    is LocationDelayDateBuilderItem -> DateValueEditor(builderItem, onValueChange)
    is ICalStartDateBuilderItem -> DateValueEditor(builderItem, onValueChange)
    is ICalUntilDateBuilderItem -> DateValueEditor(builderItem, onValueChange)
    is ICalStartTimeBuilderItem -> TimeValueEditor(builderItem, is24HourFormat, onValueChange)
    is ICalUntilTimeBuilderItem -> TimeValueEditor(builderItem, is24HourFormat, onValueChange)
    is ICalFrequencyBuilderItem ->
      ICalFreqValueEditor(builderItem, paramToTextAdapter, onValueChange, hapticFeedbackEnabled)
    is ICalWeekStartBuilderItem ->
      ICalWeekStartValueEditor(builderItem, paramToTextAdapter, onValueChange, hapticFeedbackEnabled)
    is ICalIntBuilderItem -> ICalIntValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is ICalListIntBuilderItem -> ICalIntListValueEditor(builderItem, onValueChange)
    is ICalByDayBuilderItem -> ICalDayValueListValueEditor(builderItem, paramToTextAdapter, onValueChange)
    is TimeBuilderItem -> TimeValueEditor(builderItem, is24HourFormat, onValueChange)
    is LocationDelayTimeBuilderItem -> TimeValueEditor(builderItem, is24HourFormat, onValueChange)
    is GroupBuilderItem -> GroupValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is GoogleTaskListBuilderItem -> GoogleTaskListValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is LedColorBuilderItem -> LedColorValueEditor(builderItem, onValueChange)
    is DayOfMonthBuilderItem -> DayOfMonthValueEditor(builderItem, onValueChange)
    is DayOfYearBuilderItem -> DayOfYearValueEditor(builderItem, onValueChange)
    is DaysOfWeekBuilderItem -> DaysOfWeekValueEditor(builderItem, onValueChange)
    is PriorityBuilderItem -> PriorityValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is RepeatLimitBuilderItem -> RepeatLimitValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is BeforeTimeBuilderItem -> BeforeTimeValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is RepeatTimeBuilderItem -> RepeatTimeValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is RepeatIntervalBuilderItem -> RepeatIntervalValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is TimerBuilderItem -> CountdownTimeValueEditor(builderItem, onValueChange)
    is GoogleCalendarDurationBuilderItem ->
      GoogleCalendarDurationValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is OtherParamsBuilderItem -> OtherParamsValueEditor(builderItem, onValueChange)
    is CategoryBuilderItem -> CategoryValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is LockScreenVisibilityBuilderItem ->
      LockScreenVisibilityValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is BypassDndBuilderItem -> BypassDndValueEditor(builderItem, onValueChange)
    is WakeScreenBuilderItem -> WakeScreenValueEditor(builderItem, onValueChange)
    is VibrationPatternBuilderItem -> VibrationPatternValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is DelayMinutesBuilderItem -> DelayMinutesValueEditor(builderItem, onValueChange, hapticFeedbackEnabled)
    is EmailBuilderItem -> SimpleTextValueEditor(builderItem, onValueChange, KeyboardType.Email)
    is WebAddressBuilderItem -> SimpleTextValueEditor(builderItem, onValueChange, KeyboardType.Uri)
    is GoogleCalendarBuilderItem ->
      GoogleCalendarValueEditor(builderItem, googleCalendarApi, onValueChange, hapticFeedbackEnabled)
    is SummaryBuilderItem -> TextInputValueEditor(builderItem, onValueChange)
    is DescriptionBuilderItem -> TextInputValueEditor(builderItem, onValueChange)
    is EmailSubjectBuilderItem -> TextInputValueEditor(builderItem, onValueChange)
    is ApplicationBuilderItem -> ApplicationValueEditor(builderItem, packageManagerWrapper, onPickApplication)
    is PhoneCallBuilderItem -> PhoneInputValueEditor(builderItem, onPickContact, onValueChange)
    is SmsBuilderItem -> PhoneInputValueEditor(builderItem, onPickContact, onValueChange)
    is AttachmentsBuilderItem -> AttachmentsValueEditor(builderItem, attachmentFileAdapter, onPickFiles, onValueChange)
    is NoteBuilderItem -> NoteValueEditor(builderItem, onValueChange)
    is SubTasksBuilderItem -> SubTasksValueEditor(builderItem, dateTimeManager, onValueChange)
    is TimerExclusionBuilderItem ->
      CountdownExclusionValueEditor(builderItem, dateTimeManager, is24HourFormat, onValueChange)
    else -> {}
  }
}
