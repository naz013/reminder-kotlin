package com.github.naz013.feature.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureGateTappedEvent
import com.github.naz013.common.Permissions
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.feature.settings.backup.BackupSettingsScreen
import com.github.naz013.feature.settings.calendar.CalendarSettingsScreen
import com.github.naz013.feature.settings.calendar.CalendarSettingsViewModel
import com.github.naz013.feature.settings.calendar.country.HolidayCountryScreen
import com.github.naz013.feature.settings.calendar.country.rememberHolidayCountryPickerResultHolder
import com.github.naz013.feature.settings.debug.DeveloperEvent
import com.github.naz013.feature.settings.debug.DeveloperScreen
import com.github.naz013.feature.settings.debug.DeveloperViewModel
import com.github.naz013.feature.settings.debug.ObjectExportEvent
import com.github.naz013.feature.settings.debug.ObjectExportScreen
import com.github.naz013.feature.settings.debug.ObjectExportViewModel
import com.github.naz013.feature.settings.export.ExportNavKey
import com.github.naz013.feature.settings.general.GeneralSettingsEvent
import com.github.naz013.feature.settings.general.GeneralSettingsScreen
import com.github.naz013.feature.settings.general.GeneralSettingsViewModel
import com.github.naz013.feature.settings.general.rememberAppRestartController
import com.github.naz013.feature.settings.headeritems.HeaderItemsSettingsScreen
import com.github.naz013.feature.settings.headeritems.HeaderItemsSettingsViewModel
import com.github.naz013.feature.settings.location.LocationNavKey
import com.github.naz013.feature.settings.other.OtherNavKey
import com.github.naz013.feature.settings.proversion.ProVersionScreen
import com.github.naz013.feature.settings.proversion.ProVersionViewModel
import com.github.naz013.feature.settings.proversion.rememberGooglePlayMarketLauncher
import com.github.naz013.feature.settings.security.SecurityNavKey
import com.github.naz013.feature.settings.troubleshooting.TroubleshootingScreen
import com.github.naz013.feature.settings.troubleshooting.TroubleshootingScreenState
import com.github.naz013.feature.settings.troubleshooting.TroubleshootingViewModel
import com.github.naz013.feature.settings.troubleshooting.rememberOptimizationSettingsLauncher
import com.github.naz013.platform.SystemInfo
import com.github.naz013.reviews.rememberReviewsFormLauncher
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.login.rememberAuthProvider
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Suppress("LongParameterList") // one slot per cross-feature screen this module can't depend on directly
fun EntryProviderScope<NavKey>.settingsEntries(
  backStack: MutableList<NavKey>,
  applicationId: String,
  restartActivityClass: Class<out Activity>,
  remindersEntry: @Composable (SettingsNavKey.Reminders, MutableList<NavKey>) -> Unit,
  birthdayEntry: @Composable (SettingsNavKey.Birthday, MutableList<NavKey>) -> Unit,
  managePresetsEntry: @Composable (MutableList<NavKey>) -> Unit,
  notificationCustomizationHelpEntry: @Composable (onBackClick: () -> Unit) -> Unit,
  onOpenLocalBackupExport: (String) -> Unit,
  onOpenLocalBackupImport: (String) -> Unit,
  onOpenReminderActionTest: (String) -> Unit,
  onOpenBirthdayActionTest: (String) -> Unit,
) {
  entry<SettingsNavKey.Hub> { HubEntry(backStack) }
  entry<SettingsNavKey.General> { GeneralEntry(backStack, restartActivityClass) }
  entry<SettingsNavKey.HeaderItems> { HeaderItemsEntry(backStack) }
  entry<SettingsNavKey.Backup> { BackupEntry(backStack, onOpenLocalBackupExport, onOpenLocalBackupImport) }
  entry<SettingsNavKey.Reminders> { key -> remindersEntry(key, backStack) }
  entry<SettingsNavKey.Calendar> { key -> CalendarEntry(key, backStack) }
  entry<SettingsNavKey.SelectHolidayCountry> { SelectHolidayCountryEntry(backStack) }
  entry<SettingsNavKey.Birthday> { key -> birthdayEntry(key, backStack) }
  entry<SettingsNavKey.Note> { key -> NoteEntry(key, backStack) }
  entry<SettingsNavKey.ManagePresets> { managePresetsEntry(backStack) }
  entry<SettingsNavKey.Developer> { DeveloperEntry(backStack, onOpenReminderActionTest, onOpenBirthdayActionTest) }
  entry<SettingsNavKey.ObjectExportTest> { ObjectExportEntry(backStack) }
  entry<SettingsNavKey.ProVersion> { ProVersionEntry(backStack) }
  entry<SettingsNavKey.Troubleshooting> { TroubleshootingEntry(backStack, applicationId) }
  entry<SettingsNavKey.NotificationCustomizationHelp> {
    notificationCustomizationHelpEntry { if (backStack.size > 1) backStack.removeLastOrNull() }
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
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
private fun BackupEntry(
  backStack: MutableList<NavKey>,
  onOpenLocalBackupExport: (String) -> Unit,
  onOpenLocalBackupImport: (String) -> Unit,
) {
  val buildInfo = koinInject<BuildInfo>()
  val analyticsEventSender = koinInject<AnalyticsEventSender>()

  val exportBackupLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
      if (uri != null) onOpenLocalBackupExport(uri.toString())
    }
  val importBackupLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) onOpenLocalBackupImport(uri.toString())
    }

  SettingsScaffold(
    title = stringResource(R.string.backup),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
private fun GeneralEntry(
  backStack: MutableList<NavKey>,
  restartActivityClass: Class<out Activity>,
) {
  val viewModel = koinViewModel<GeneralSettingsViewModel>()

  val hapticFeedback = LocalHapticFeedback.current
  val appRestartController = rememberAppRestartController(restartActivityClass)

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
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    GeneralSettingsScreen(
      modifier = Modifier.padding(padding),
      state = state,
      onLanguageClick = viewModel::onLanguageClick,
      onThemeClick = viewModel::onThemeClick,
      onTimeFormatClick = viewModel::onTimeFormatClick,
      onHeaderItemsClick = { backStack.add(SettingsNavKey.HeaderItems) },
      onMetricToggle = { viewModel.onMetricToggle() },
      onAnalyticsToggle = { viewModel.onAnalyticsToggle() },
      onDialogOptionSelected = viewModel::onDialogOptionSelected,
      onDialogDismiss = viewModel::onDialogDismiss,
      onHapticToggle = { viewModel.onHapticToggle() }
    )
  }
}

@Composable
private fun HeaderItemsEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<HeaderItemsSettingsViewModel>()
  val state by viewModel.state.collectAsState()

  SettingsScaffold(
    title = stringResource(R.string.header_items),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
  ) { padding ->
    HeaderItemsSettingsScreen(
      modifier = Modifier.padding(padding),
      state = state,
      onToggle = viewModel::onToggle,
      onReorder = viewModel::onReorder,
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

  val buildInfo = koinInject<BuildInfo>()
  val featureFlags = koinInject<FeatureFlags>()
  val analyticsEventSender = koinInject<AnalyticsEventSender>()

  // SelectHolidayCountry is a separate Nav3 entry (its own ViewModelStoreOwner), so it can't call
  // back into CalendarSettingsViewModel directly - resolved the same way ApplicationPickerResultHolder
  // hands a picked value back once a picker entry pops on top of the one that pushed it.
  val holidayCountryPickerResultHolder = rememberHolidayCountryPickerResultHolder()
  LaunchedEffect(Unit) {
    val code = holidayCountryPickerResultHolder.pendingCountryCode
    if (code != null) {
      holidayCountryPickerResultHolder.pendingCountryCode = null
      viewModel.onHolidayCountryPicked(code)
    }
  }

  SettingsScaffold(
    title = key.screenTitle ?: stringResource(R.string.calendar),
    navigationIcon = settingsNavigationIcon(key.screenTitle),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
      isHolidaysSectionVisible = featureFlags.isEnabled(FeatureFlag.PUBLIC_HOLIDAYS),
      isHolidaysLocked = !buildInfo.isPro,
      onHolidaysToggle = viewModel::onHolidaysToggle,
      onHolidaysLockedClick = {
        analyticsEventSender.send(FeatureGateTappedEvent(Feature.PUBLIC_HOLIDAYS))
        backStack.add(SettingsNavKey.ProVersion)
      },
      onHolidayCountryClick = { backStack.add(SettingsNavKey.SelectHolidayCountry) },
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun SelectHolidayCountryEntry(backStack: MutableList<NavKey>) {
  val resultHolder = rememberHolidayCountryPickerResultHolder()
  HolidayCountryScreen(
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onCountrySelected = { code ->
      resultHolder.pendingCountryCode = code
      if (backStack.size > 1) backStack.removeLastOrNull()
    },
  )
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
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
private fun DeveloperEntry(
  backStack: MutableList<NavKey>,
  onOpenReminderActionTest: (String) -> Unit,
  onOpenBirthdayActionTest: (String) -> Unit,
) {
  val viewModel = koinViewModel<DeveloperViewModel>()

  val reviewsFormLauncher = rememberReviewsFormLauncher()
  val toastDispatcher = rememberToastDispatcher()

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
      is DeveloperEvent.OpenReminderAction -> onOpenReminderActionTest(event.reminderId)
      is DeveloperEvent.OpenBirthdayAction -> onOpenBirthdayActionTest(event.birthdayId)
      DeveloperEvent.OpenProVersion -> backStack.add(SettingsNavKey.ProVersion)
      DeveloperEvent.BannersReset -> toastDispatcher.showToast(message = "Home Screen banners have been reset")
      is DeveloperEvent.ShowMessage -> toastDispatcher.showToast(message = event.message)
    }
  }

  SettingsScaffold(
    title = "Developer",
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
      onInsertHugeFormattedNotesClick = viewModel::onInsertHugeFormattedNotesClick,
      onInsertInsightsDemoDataClick = viewModel::onInsertInsightsDemoDataClick,
      onPopulateCalendarNormalDataClick = viewModel::onPopulateCalendarNormalDataClick,
      onPopulateCalendarMassiveDataClick = viewModel::onPopulateCalendarMassiveDataClick,
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
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
private fun TroubleshootingEntry(
  backStack: MutableList<NavKey>,
  applicationId: String,
) {
  val viewModel = koinViewModel<TroubleshootingViewModel>()

  val optimizationSettingsLauncher = rememberOptimizationSettingsLauncher()
  val emailSender = rememberSendEmailResolver(applicationId)

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
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
