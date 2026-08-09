package com.elementary.tasks.settings

import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.dialog.BirthdayActionActivity
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.github.naz013.ui.common.datetime.rememberDateTimePicker
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.preset.ManagePresetsViewModel
import com.elementary.tasks.reminder.dialog.ReminderActionActivity
import com.elementary.tasks.settings.backup.BackupSettingsScreen
import com.elementary.tasks.settings.birthday.BirthdaySettingsEvent
import com.elementary.tasks.settings.birthday.BirthdaySettingsScreen
import com.elementary.tasks.settings.birthday.BirthdaySettingsViewModel
import com.elementary.tasks.settings.calendar.CalendarSettingsScreen
import com.elementary.tasks.settings.calendar.CalendarSettingsViewModel
import com.elementary.tasks.settings.export.ExportNavKey
import com.elementary.tasks.settings.general.GeneralSettingsEvent
import com.elementary.tasks.settings.general.GeneralSettingsScreen
import com.elementary.tasks.settings.general.GeneralSettingsViewModel
import com.elementary.tasks.settings.general.rememberAppRestartController
import com.elementary.tasks.settings.location.LocationNavKey
import com.elementary.tasks.settings.other.OtherNavKey
import com.elementary.tasks.settings.proversion.ProVersionScreen
import com.elementary.tasks.settings.proversion.ProVersionViewModel
import com.elementary.tasks.settings.proversion.rememberGooglePlayMarketLauncher
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureGateTappedEvent
import com.elementary.tasks.settings.reminders.ManagePresetsScreen
import com.elementary.tasks.settings.reminders.RemindersSettingsEvent
import com.elementary.tasks.settings.reminders.RemindersSettingsScreen
import com.elementary.tasks.settings.reminders.RemindersSettingsViewModel
import com.elementary.tasks.settings.reminders.help.NotificationCustomizationHelpScreen
import com.elementary.tasks.settings.security.SecurityNavKey
import com.elementary.tasks.settings.test.DeveloperEvent
import com.elementary.tasks.settings.test.DeveloperScreen
import com.elementary.tasks.settings.test.DeveloperViewModel
import com.elementary.tasks.settings.test.ObjectExportEvent
import com.elementary.tasks.settings.test.ObjectExportScreen
import com.elementary.tasks.settings.test.ObjectExportViewModel
import com.elementary.tasks.settings.troubleshooting.TroubleshootingScreen
import com.elementary.tasks.settings.troubleshooting.TroubleshootingScreenState
import com.elementary.tasks.settings.troubleshooting.TroubleshootingViewModel
import com.elementary.tasks.settings.troubleshooting.rememberOptimizationSettingsLauncher
import com.elementary.tasks.workflow.WorkflowNavKey
import com.github.naz013.common.Permissions
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.platform.SystemInfo
import com.github.naz013.insights.InsightsNavKey
import com.github.naz013.localbackup.LocalBackupNavKey
import com.github.naz013.reviews.rememberReviewsFormLauncher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.github.naz013.ui.common.login.rememberAuthProvider
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.settingsEntries(backStack: MutableList<NavKey>) {
  entry<SettingsNavKey.Hub> { HubEntry(backStack) }
  entry<SettingsNavKey.General> { GeneralEntry(backStack) }
  entry<SettingsNavKey.Backup> { BackupEntry(backStack) }
  entry<SettingsNavKey.Reminders> { key -> RemindersEntry(key, backStack) }
  entry<SettingsNavKey.Calendar> { key -> CalendarEntry(key, backStack) }
  entry<SettingsNavKey.Birthday> { key -> BirthdayEntry(key, backStack) }
  entry<SettingsNavKey.Note> { key -> NoteEntry(key, backStack) }
  entry<SettingsNavKey.ManagePresets> { ManagePresetsEntry(backStack) }
  entry<SettingsNavKey.Developer> { DeveloperEntry(backStack) }
  entry<SettingsNavKey.ObjectExportTest> { ObjectExportEntry(backStack) }
  entry<SettingsNavKey.ProVersion> { ProVersionEntry(backStack) }
  entry<SettingsNavKey.Troubleshooting> { TroubleshootingEntry(backStack) }
  entry<SettingsNavKey.NotificationCustomizationHelp> {
    NotificationCustomizationHelpScreen(onBackClick = { backStack.removeLastOrNull() })
  }
}

@Composable
private fun HubEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<SettingsHubViewModel>()

  val googlePlayMarketLauncher = rememberGooglePlayMarketLauncher()
  val authProvider = rememberAuthProvider()

  val state by viewModel.state.collectAsState(SettingsHubState())

  SettingsScaffold(
    title = stringResource(R.string.action_settings),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    SettingsHubScreen(
      state = state,
      onBuyProClick = { backStack.add(SettingsNavKey.ProVersion) },
      onUpdateClick = { googlePlayMarketLauncher.launchSelf() },
      onGeneralClick = { backStack.add(SettingsNavKey.General) },
      onBackupClick = { backStack.add(SettingsNavKey.Backup) },
      onCalendarClick = { backStack.add(SettingsNavKey.Calendar()) },
      onRemindersClick = { backStack.add(SettingsNavKey.Reminders()) },
      onBirthdaysClick = { backStack.add(SettingsNavKey.Birthday()) },
      onSecurityClick = {
        if (state.hasPinCode) {
          authProvider.requestAuth(onAuthSuccess = { backStack.add(SecurityNavKey.Security) })
        } else {
          backStack.add(SecurityNavKey.Security)
        }
      },
      onNotesClick = { backStack.add(SettingsNavKey.Note()) },
      onOtherClick = { backStack.add(OtherNavKey.Other) },
      onDeveloperClick = { backStack.add(SettingsNavKey.Developer) },
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun BackupEntry(backStack: MutableList<NavKey>) {
  val buildInfo = koinInject<BuildInfo>()
  val analyticsEventSender = koinInject<AnalyticsEventSender>()

  val exportBackupLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
      if (uri != null) backStack.add(LocalBackupNavKey.Export(uri.toString()))
    }
  val importBackupLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) backStack.add(LocalBackupNavKey.Import(uri.toString()))
    }

  SettingsScaffold(
    title = stringResource(R.string.backup),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    BackupSettingsScreen(
      isLocalBackupLocked = !buildInfo.isPro,
      onCloudBackupClick = { backStack.add(ExportNavKey.CloudBackup) },
      onExportBackupClick = { exportBackupLauncher.launch(BACKUP_FILE_NAME) },
      onImportBackupClick = { importBackupLauncher.launch(arrayOf("*/*")) },
      onLocalBackupLockedClick = {
        analyticsEventSender.send(FeatureGateTappedEvent(Feature.LOCAL_BACKUP))
        backStack.add(SettingsNavKey.ProVersion)
      },
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun GeneralEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<GeneralSettingsViewModel>()

  val hapticFeedback = LocalHapticFeedback.current
  val appRestartController = rememberAppRestartController()

  val state by viewModel.state.collectAsState()
  viewModel.event.ObserveEvent { event ->
    when (event) {
      GeneralSettingsEvent.RecreateActivity -> appRestartController.recreate()
      GeneralSettingsEvent.RestartApp -> appRestartController.restartApp()

      is GeneralSettingsEvent.HapticFeedback -> {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
      }
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.general),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    GeneralSettingsScreen(
      modifier = Modifier.padding(padding),
      state = state,
      onLanguageClick = viewModel::onLanguageClick,
      onThemeClick = viewModel::onThemeClick,
      onTimeFormatClick = viewModel::onTimeFormatClick,
      onMetricToggle = { viewModel.onMetricToggle() },
      onAnalyticsToggle = { viewModel.onAnalyticsToggle() },
      onDialogOptionSelected = viewModel::onDialogOptionSelected,
      onDialogDismiss = viewModel::onDialogDismiss,
      onHapticToggle = { viewModel.onHapticToggle() }
    )
  }
}

@Composable
private fun RemindersEntry(
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
    onBackClick = { backStack.removeLastOrNull() },
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
private fun CalendarEntry(
  key: SettingsNavKey.Calendar,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<CalendarSettingsViewModel>()
  val permissionRequester = rememberPermissionRequesterRationale()
  val state by viewModel.state.collectAsState()

  SettingsScaffold(
    title = key.screenTitle ?: stringResource(R.string.calendar),
    navigationIcon = settingsNavigationIcon(key.screenTitle),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    CalendarSettingsScreen(
      state = state,
      onFirstDayClick = viewModel::onFirstDayClick,
      onFirstDayOptionSelected = viewModel::onFirstDayOptionSelected,
      onTodayColorClick = viewModel::onTodayColorClick,
      onReminderColorClick = viewModel::onReminderColorClick,
      onBirthdayColorClick = viewModel::onBirthdayColorClick,
      onColorOptionSelected = viewModel::onColorOptionSelected,
      onSelectCalendarClick = {
        permissionRequester.request(
          listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR),
          onGranted = { viewModel.onSelectGoogleCalendarClicked() },
        )
      },
      onGoogleCalendarOptionSelected = viewModel::onGoogleCalendarOptionSelected,
      onCalendarResetClick = viewModel::onCalendarReset,
      onExportToggle = viewModel::onExportToggle,
      onScanToggle = viewModel::onScanToggle,
      onDialogDismiss = viewModel::onDialogDismiss,
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun BirthdayEntry(
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
    onBackClick = { backStack.removeLastOrNull() },
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
private fun NoteEntry(
  key: SettingsNavKey.Note,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<NoteSettingsViewModel>()
  val state by viewModel.state.collectAsState()

  SettingsScaffold(
    title = key.screenTitle ?: stringResource(R.string.notes),
    navigationIcon = settingsNavigationIcon(key.screenTitle),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    NoteSettingsScreen(
      state = state,
      onColorRememberToggle = viewModel::onColorRememberToggle,
      onFontSizeRememberToggle = viewModel::onFontSizeRememberToggle,
      onFontStyleRememberToggle = viewModel::onFontStyleRememberToggle,
      onOpacityClick = viewModel::onOpacityClick,
      onOpacityPreviewChange = viewModel::onOpacityPreviewChange,
      onOpacityConfirm = viewModel::onOpacityConfirm,
      onOpacityDialogDismiss = viewModel::onOpacityDialogDismiss,
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun ManagePresetsEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<ManagePresetsViewModel>()
  val state by viewModel.state.collectAsState()

  SettingsScaffold(
    title = stringResource(R.string.recur_presets),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    ManagePresetsScreen(
      presets = state.presets,
      onDeleteClick = { viewModel.deletePreset(it.id) },
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun DeveloperEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<DeveloperViewModel>()

  val reviewsFormLauncher = rememberReviewsFormLauncher()
  val toastDispatcher = rememberToastDispatcher()

  val context = LocalContext.current
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      DeveloperEvent.OpenObjectExport -> backStack.add(SettingsNavKey.ObjectExportTest)
      is DeveloperEvent.OpenReviewDialog -> {
        reviewsFormLauncher.showFeedbackForm(
          title = "Write a review",
          appSource = event.appSource,
          allowLogsAttachment = false,
        )
      }
      is DeveloperEvent.OpenReminderAction -> ReminderActionActivity.mockTest(context, event.reminderId)
      is DeveloperEvent.OpenBirthdayAction -> BirthdayActionActivity.mockTest(context, event.birthdayId)
      DeveloperEvent.OpenProVersion -> backStack.add(SettingsNavKey.ProVersion)
      DeveloperEvent.BannersReset -> toastDispatcher.showToast(message = "Home Screen banners have been reset")
      is DeveloperEvent.ShowMessage -> toastDispatcher.showToast(message = event.message)
    }
  }

  SettingsScaffold(
    title = "Developer",
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    DeveloperScreen(
      state = state,
      modifier = Modifier.padding(padding),
      onResetBannersClick = viewModel::onResetBannersClick,
      onBirthdayDialogClick = viewModel::onBirthdayDialogClick,
      onReminderDialogClick = viewModel::onReminderDialogClick,
      onRecurrenceTestClick = viewModel::onRecurrenceTestClick,
      onObjectExportClick = viewModel::onObjectExportClick,
      onReviewDialogClick = viewModel::onReviewDialogClick,
      onProVersionClick = viewModel::onProVersionClick,
      onClearTableClick = viewModel::onClearTableClick,
      onClearAllTablesClick = viewModel::onClearAllTablesClick,
      onClearAllTablesConfirm = viewModel::onClearAllTablesConfirm,
      onClearAllTablesDismiss = viewModel::onClearAllTablesDismiss,
      onInsertDemoDataClick = viewModel::onInsertDemoDataClick,
      onInsertInsightsDemoDataClick = viewModel::onInsertInsightsDemoDataClick,
      onDialogOptionSelected = viewModel::onDialogOptionSelected,
      onDialogConfirm = viewModel::onDialogConfirm,
      onDialogDismiss = viewModel::onDialogDismiss,
    )
  }
}

@Composable
private fun ObjectExportEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<ObjectExportViewModel>()
  val state by viewModel.state.collectAsState()
  var pendingItemId by remember { mutableStateOf<String?>(null) }

  val saveLocationLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
      val itemId = pendingItemId
      if (uri != null && itemId != null) viewModel.onSaveLocationPicked(itemId, uri)
      pendingItemId = null
    }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is ObjectExportEvent.RequestSaveLocation -> {
        pendingItemId = event.itemId
        saveLocationLauncher.launch(event.fileName)
      }
      ObjectExportEvent.ObjectSaved -> Unit
    }
  }

  SettingsScaffold(
    title = "Save object to File",
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    ObjectExportScreen(
      state = state,
      onObjectTypeSelected = viewModel::onObjectTypeSelected,
      onItemClick = viewModel::onItemClick,
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun ProVersionEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<ProVersionViewModel>()
  val googlePlayMarketLauncher = rememberGooglePlayMarketLauncher()
  ProVersionScreen(
    advantages = viewModel.state.advantages,
    onBackClick = { backStack.removeLastOrNull() },
    onBuyClick = {
      viewModel.onBuyClicked()
      googlePlayMarketLauncher.launch(
        packageName = SystemInfo.PRO_PACKAGE_NAME,
        referrer = "utm_source=free_app&utm_medium=in_app_cta",
      )
    },
  )
}

@Composable
private fun TroubleshootingEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<TroubleshootingViewModel>()

  val optimizationSettingsLauncher = rememberOptimizationSettingsLauncher()
  val emailSender = rememberSendEmailResolver()

  val state by viewModel.state.collectAsState(TroubleshootingScreenState())
  viewModel.event.ObserveEvent { event ->
    when (event) {
      is TroubleshootingViewModel.ViewModelEvent.SendLogs -> {
        emailSender.send(
          email = "feedback.cray@gmail.com",
          subject = "Issue Logs",
          message = "Hi,\n\nHere is logs for my issue.\n\nIssue description: \n\nBest regards\n",
          file = event.file,
        )
      }

      is TroubleshootingViewModel.ViewModelEvent.OpenOptimizationSettings -> {
        optimizationSettingsLauncher.launch()
      }
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.troubleshooting),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    TroubleshootingScreen(
      state = state,
      onSendLogsClick = viewModel::sendLogs,
      onDisableOptimizationClick = { viewModel.onOpenOptimizationSettingsClicked() },
      modifier = Modifier.padding(padding),
    )
  }
}

private const val BACKUP_FILE_NAME = "reminder_backup.rbkp"
