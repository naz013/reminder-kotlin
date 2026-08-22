package com.github.naz013.feature.reminder.build.valuedialog

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.feature.reminder.build.ApplicationBuilderItem
import com.github.naz013.feature.reminder.build.AttachmentsBuilderItem
import com.github.naz013.feature.reminder.build.BeforeTimeBuilderItem
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.BypassDndBuilderItem
import com.github.naz013.feature.reminder.build.CategoryBuilderItem
import com.github.naz013.feature.reminder.build.DateBuilderItem
import com.github.naz013.feature.reminder.build.DayOfMonthBuilderItem
import com.github.naz013.feature.reminder.build.DayOfYearBuilderItem
import com.github.naz013.feature.reminder.build.DaysOfWeekBuilderItem
import com.github.naz013.feature.reminder.build.DelayMinutesBuilderItem
import com.github.naz013.feature.reminder.build.DescriptionBuilderItem
import com.github.naz013.feature.reminder.build.EmailBuilderItem
import com.github.naz013.feature.reminder.build.EmailSubjectBuilderItem
import com.github.naz013.feature.reminder.build.GoogleCalendarBuilderItem
import com.github.naz013.feature.reminder.build.GoogleCalendarDurationBuilderItem
import com.github.naz013.feature.reminder.build.GoogleTaskListBuilderItem
import com.github.naz013.feature.reminder.build.GroupBuilderItem
import com.github.naz013.feature.reminder.build.ICalByDayBuilderItem
import com.github.naz013.feature.reminder.build.ICalFrequencyBuilderItem
import com.github.naz013.feature.reminder.build.ICalIntBuilderItem
import com.github.naz013.feature.reminder.build.ICalListIntBuilderItem
import com.github.naz013.feature.reminder.build.ICalStartDateBuilderItem
import com.github.naz013.feature.reminder.build.ICalStartTimeBuilderItem
import com.github.naz013.feature.reminder.build.ICalUntilDateBuilderItem
import com.github.naz013.feature.reminder.build.ICalUntilTimeBuilderItem
import com.github.naz013.feature.reminder.build.ICalWeekStartBuilderItem
import com.github.naz013.feature.reminder.build.LedColorBuilderItem
import com.github.naz013.feature.reminder.build.LocationDelayDateBuilderItem
import com.github.naz013.feature.reminder.build.LocationDelayTimeBuilderItem
import com.github.naz013.feature.reminder.build.LockScreenVisibilityBuilderItem
import com.github.naz013.feature.reminder.build.NoteBuilderItem
import com.github.naz013.feature.reminder.build.OtherParamsBuilderItem
import com.github.naz013.feature.reminder.build.PhoneCallBuilderItem
import com.github.naz013.feature.reminder.build.PriorityBuilderItem
import com.github.naz013.feature.reminder.build.RepeatIntervalBuilderItem
import com.github.naz013.feature.reminder.build.RepeatLimitBuilderItem
import com.github.naz013.feature.reminder.build.RepeatTimeBuilderItem
import com.github.naz013.feature.reminder.build.SmsBuilderItem
import com.github.naz013.feature.reminder.build.SubTasksBuilderItem
import com.github.naz013.feature.reminder.build.SummaryBuilderItem
import com.github.naz013.feature.reminder.build.TimeBuilderItem
import com.github.naz013.feature.reminder.build.TimerBuilderItem
import com.github.naz013.feature.reminder.build.TimerExclusionBuilderItem
import com.github.naz013.feature.reminder.build.VibrationPatternBuilderItem
import com.github.naz013.feature.reminder.build.WakeScreenBuilderItem
import com.github.naz013.feature.reminder.build.WebAddressBuilderItem
import com.github.naz013.feature.reminder.build.adapter.ParamToTextAdapter
import com.github.naz013.feature.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.github.naz013.feature.reminder.build.valuedialog.editor.ApplicationValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.AttachmentsValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.BeforeTimeValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.BypassDndValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.CategoryValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.CountdownExclusionValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.CountdownTimeValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.DateValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.DayOfMonthValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.DayOfYearValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.DaysOfWeekValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.DelayMinutesValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.GoogleCalendarDurationValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.GoogleCalendarValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.GoogleTaskListValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.GroupValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.ICalDayValueListValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.ICalFreqValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.ICalIntListValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.ICalIntValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.ICalWeekStartValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.LedColorValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.LockScreenVisibilityValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.NoteValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.OtherParamsValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.PhoneInputValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.PriorityValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.RepeatIntervalValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.RepeatLimitValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.RepeatTimeValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.SimpleTextValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.SubTasksValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.TextInputValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.TimeValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.VibrationPatternValueEditor
import com.github.naz013.feature.reminder.build.valuedialog.editor.WakeScreenValueEditor
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet

/** Semantics test tag for [ValueEditorSheet]'s own close button (the chevron-down icon) - it has
 *  no text/`contentDescription` of its own to locate it by (`contentDescription = null` in
 *  source, since the icon's meaning is already obvious visually). Exposed so instrumented tests
 *  can close the sheet by tag (`composeRule.onNodeWithTag(valueEditorSheetCloseTestTag)`), and so
 *  Maestro flows can too via `tapOn: { id: "value_editor_sheet_close" }` - confirmed live via
 *  `adb shell uiautomator dump` showing `resource-id="value_editor_sheet_close"` on this node,
 *  and via a full passing Maestro run (`notification_permission_denied.yaml`, whose
 *  `create_countdown_reminder.yaml` subflow uses this tag to close the sheet 3 times). See
 *  docs/e2e-testing.md §1c for what it took to get here - this Compose UI version has no
 *  `LocalTestTagsAsResourceId` CompositionLocal, so `AppModalBottomSheet`
 *  (`BottomSheet.kt`) re-applies the older `Modifier.semantics { testTagsAsResourceId = true }`
 *  property inside its own Popup content for this to reach in here at all. */
const val valueEditorSheetCloseTestTag = "value_editor_sheet_close"

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
internal fun ValueEditorSheet(
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
        MenuIconButton(
          icon = painterResource(R.drawable.ic_builder_ical_help),
          contentDescription = stringResource(R.string.help),
          onClick = onHelpClick,
        )
      }
      MenuIconButton(
        modifier = Modifier.testTag(valueEditorSheetCloseTestTag),
        icon = painterResource(R.drawable.ic_builder_chevron_down),
        contentDescription = stringResource(R.string.cd_collapse),
        onClick = onDismissRequest,
      )
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
    is SubTasksBuilderItem -> SubTasksValueEditor(builderItem, dateTimeManager, onValueChange, hapticFeedbackEnabled)
    is TimerExclusionBuilderItem ->
      CountdownExclusionValueEditor(builderItem, dateTimeManager, is24HourFormat, onValueChange)
    else -> {}
  }
}
