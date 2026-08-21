package com.elementary.tasks.settings

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.github.naz013.common.Permissions
import com.github.naz013.feature.birthday.settings.BirthdaySettingsEvent
import com.github.naz013.feature.birthday.settings.BirthdaySettingsScreen
import com.github.naz013.feature.birthday.settings.BirthdaySettingsViewModel
import com.github.naz013.feature.reminder.build.preset.ManagePresetsViewModel
import com.github.naz013.feature.reminder.settings.ManagePresetsScreen
import com.github.naz013.feature.reminder.settings.RemindersSettingsEvent
import com.github.naz013.feature.reminder.settings.RemindersSettingsScreen
import com.github.naz013.feature.reminder.settings.RemindersSettingsViewModel
import com.github.naz013.feature.settings.SettingsNavKey
import com.github.naz013.feature.settings.SettingsScaffold
import com.github.naz013.feature.settings.location.LocationNavKey
import com.github.naz013.feature.settings.settingsNavigationIcon
import com.github.naz013.feature.workflow.WorkflowNavKey
import com.github.naz013.insights.InsightsNavKey
import com.github.naz013.ui.common.datetime.rememberDateTimePicker
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import org.koin.compose.viewmodel.koinViewModel

/**
 * Renders the Reminders/Birthday/ManagePresets settings entries and the Notification
 * Customization help screen - these live in `feature-reminder`/`feature-birthday`, which
 * `feature-settings` can't depend on (feature-* modules never depend on each other), so
 * `feature-settings`'s `settingsEntries()` takes them as slots supplied from here, the
 * composition root that already depends on every feature module.
 */

@Composable
fun RemindersCrossFeatureEntry(
  key: SettingsNavKey.Reminders,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<RemindersSettingsViewModel>()
  val context = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current
  val dateTimePicker = rememberDateTimePicker()
  val permissionRequester = rememberPermissionRequesterRationale()
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      RemindersSettingsEvent.OpenPresets -> backStack.add(SettingsNavKey.ManagePresets)
      RemindersSettingsEvent.OpenLocationSettings -> backStack.add(LocationNavKey.Location)
      RemindersSettingsEvent.OpenWorkflowRules -> backStack.add(WorkflowNavKey.Gallery)
      is RemindersSettingsEvent.ShowTimePicker -> {
        dateTimePicker.showTimePicker(
          time = event.time,
          title = event.title,
          is24Hour = event.is24Hour,
          onTimeSelected = { viewModel.onTimeSelected(event.target, it) },
        )
      }
      RemindersSettingsEvent.ShowPermanentNotification -> PermanentReminderReceiver.show(context)
      RemindersSettingsEvent.HidePermanentNotification -> PermanentReminderReceiver.hide(context)
      RemindersSettingsEvent.HapticFeedback -> {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      }
    }
  }

  SettingsScaffold(
    title = key.screenTitle ?: stringResource(R.string.reminders_),
    navigationIcon = settingsNavigationIcon(key.screenTitle),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    RemindersSettingsScreen(
      state = state,
      onInsightsClick = {
        if (state.isInsightsLocked) {
          viewModel.onInsightsLockedClick()
          backStack.add(SettingsNavKey.ProVersion)
        } else {
          backStack.add(InsightsNavKey.Dashboard)
        }
      },
      onPresetsClick = viewModel::onPresetsClick,
      onLocationClick = viewModel::onLocationClick,
      onWorkflowRulesClick = viewModel::onWorkflowRulesClick,
      onPriorityClick = viewModel::onPriorityClick,
      onCompletedToggle = viewModel::onCompletedToggle,
      onWearToggle = viewModel::onWearToggle,
      onSnoozeClick = viewModel::onSnoozeClick,
      onRepeatToggle = viewModel::onRepeatToggle,
      onRepeatIntervalClick = viewModel::onRepeatIntervalClick,
      onLedToggle = viewModel::onLedToggle,
      onLedColorClick = viewModel::onLedColorClick,
      onPermanentNotificationClick = {
        val turningOn = !viewModel.state.value.isPermanentNotificationChecked
        if (turningOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          permissionRequester.request(
            Permissions.POST_NOTIFICATION,
            onGranted = { viewModel.onPermanentNotificationToggle() },
          )
        } else {
          viewModel.onPermanentNotificationToggle()
        }
      },
      onStatusIconToggle = viewModel::onStatusIconToggle,
      onDoNotDisturbToggle = viewModel::onDoNotDisturbToggle,
      onDndFromClick = viewModel::onDndFromClick,
      onDndToClick = viewModel::onDndToClick,
      onDndActionClick = viewModel::onDndActionClick,
      onDndIgnoreClick = viewModel::onDndIgnoreClick,
      onDefaultVibrateToggle = viewModel::onDefaultVibrateToggle,
      onDefaultBypassDoNotDisturbToggle = viewModel::onDefaultBypassDoNotDisturbToggle,
      onDefaultWakeScreenToggle = viewModel::onDefaultWakeScreenToggle,
      onDefaultSwipeToDismissToggle = viewModel::onDefaultSwipeToDismissToggle,
      onInAppAlertBannerToggle = viewModel::onInAppAlertBannerToggle,
      onDefaultCategoryClick = viewModel::onDefaultCategoryClick,
      onDefaultLockScreenVisibilityClick = viewModel::onDefaultLockScreenVisibilityClick,
      onDefaultVibrationPatternClick = viewModel::onDefaultVibrationPatternClick,
      onNotificationHelpClick = { backStack.add(SettingsNavKey.NotificationCustomizationHelp) },
      onChoiceOptionSelected = viewModel::onChoiceOptionSelected,
      onSeekValueChange = viewModel::onSeekValueChange,
      onSeekConfirm = viewModel::onSeekConfirm,
      onDialogDismiss = viewModel::onDialogDismiss,
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
fun BirthdayCrossFeatureEntry(
  key: SettingsNavKey.Birthday,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<BirthdaySettingsViewModel>()
  val context = LocalContext.current
  val dateTimePicker = rememberDateTimePicker()
  val permissionRequester = rememberPermissionRequesterRationale()
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is BirthdaySettingsEvent.ShowTimePicker -> {
        dateTimePicker.showTimePicker(
          time = event.time,
          title = event.title,
          is24Hour = event.is24Hour,
          onTimeSelected = { viewModel.onTimeSelected(it) },
        )
      }
      is BirthdaySettingsEvent.UpdatePermanentNotificationVisibility -> {
        val action = if (event.visible) PermanentBirthdayReceiver.ACTION_SHOW else PermanentBirthdayReceiver.ACTION_HIDE
        context.sendBroadcast(Intent(context, PermanentBirthdayReceiver::class.java).setAction(action))
      }
    }
  }

  SettingsScaffold(
    title = key.screenTitle ?: stringResource(R.string.birthdays),
    navigationIcon = settingsNavigationIcon(key.screenTitle),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    BirthdaySettingsScreen(
      state = state,
      onReminderToggle = viewModel::onReminderToggle,
      onDaysToBirthdayClick = viewModel::onDaysToBirthdayClick,
      onDaysToBirthdayPreviewChange = viewModel::onDaysToBirthdayPreviewChange,
      onDaysToBirthdayConfirm = viewModel::onDaysToBirthdayConfirm,
      onPriorityClick = viewModel::onPriorityClick,
      onPriorityOptionSelected = viewModel::onPriorityOptionSelected,
      onReminderTimeClick = viewModel::onReminderTimeClick,
      onWidgetToggle = viewModel::onWidgetToggle,
      onHomeDaysClick = viewModel::onHomeDaysClick,
      onHomeDaysPreviewChange = viewModel::onHomeDaysPreviewChange,
      onHomeDaysConfirm = viewModel::onHomeDaysConfirm,
      onPermanentToggle = viewModel::onPermanentToggle,
      onGlobalToggle = viewModel::onGlobalToggle,
      onLedToggle = viewModel::onLedToggle,
      onLedColorClick = viewModel::onLedColorClick,
      onLedColorOptionSelected = viewModel::onLedColorOptionSelected,
      onUseContactsToggle = {
        permissionRequester.request(Permissions.READ_CONTACTS, onGranted = { viewModel.onUseContactsToggle() })
      },
      onAutoScanToggle = viewModel::onAutoScanToggle,
      onDialogDismiss = viewModel::onDialogDismiss,
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
fun ManagePresetsCrossFeatureEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<ManagePresetsViewModel>()
  val state by viewModel.state.collectAsState()

  SettingsScaffold(
    title = stringResource(R.string.recur_presets),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    ManagePresetsScreen(
      presets = state.presets,
      onDeleteClick = { viewModel.deletePreset(it.id) },
      modifier = Modifier.padding(padding),
    )
  }
}
