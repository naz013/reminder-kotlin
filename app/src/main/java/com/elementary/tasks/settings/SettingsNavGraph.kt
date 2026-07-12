package com.elementary.tasks.settings

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.dialog.BirthdayActionActivity
import com.elementary.tasks.core.os.compose.PermissionRationaleDialog
import com.elementary.tasks.core.os.compose.rememberPermissionRequester
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.notes.ObserveNonNull
import com.elementary.tasks.reminder.build.preset.ManagePresetsViewModel
import com.elementary.tasks.reminder.dialog.ReminderActionActivity
import com.elementary.tasks.settings.reminders.ManagePresetsScreen
import com.elementary.tasks.settings.birthday.BirthdaySettingsEvent
import com.elementary.tasks.settings.birthday.BirthdaySettingsScreen
import com.elementary.tasks.settings.birthday.BirthdaySettingsViewModel
import com.elementary.tasks.settings.calendar.CalendarSettingsEvent
import com.elementary.tasks.settings.calendar.CalendarSettingsScreen
import com.elementary.tasks.settings.calendar.CalendarSettingsViewModel
import com.elementary.tasks.settings.export.ExportNavKey
import com.elementary.tasks.settings.general.GeneralSettingsEvent
import com.elementary.tasks.settings.general.GeneralSettingsScreen
import com.elementary.tasks.settings.general.GeneralSettingsViewModel
import com.elementary.tasks.settings.location.LocationNavKey
import com.elementary.tasks.settings.other.OtherNavKey
import com.elementary.tasks.settings.proversion.ProVersionScreen
import com.elementary.tasks.settings.proversion.ProVersionViewModel
import com.elementary.tasks.settings.reminders.DndTimeTarget
import com.elementary.tasks.settings.reminders.RemindersSettingsEvent
import com.elementary.tasks.settings.reminders.RemindersSettingsScreen
import com.elementary.tasks.settings.reminders.RemindersSettingsViewModel
import com.elementary.tasks.settings.security.SecurityNavKey
import com.elementary.tasks.settings.test.DeveloperEvent
import com.elementary.tasks.settings.test.DeveloperScreen
import com.elementary.tasks.settings.test.DeveloperViewModel
import com.elementary.tasks.settings.test.ObjectExportEvent
import com.elementary.tasks.settings.test.ObjectExportScreen
import com.elementary.tasks.settings.test.ObjectExportViewModel
import com.elementary.tasks.settings.troubleshooting.TroubleshootingScreen
import com.elementary.tasks.settings.troubleshooting.TroubleshootingViewModel
import com.elementary.tasks.splash.SplashScreenActivity
import com.github.naz013.common.Module
import com.github.naz013.common.Permissions
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.activity.finishWith
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.login.LoginApi
import com.github.naz013.ui.common.theme.ThemeProvider
import com.google.android.material.color.DynamicColors
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent

/**
 * Contributes the "hub" Settings island's screens (Nav3 entries) - everything reached directly
 * from the Settings hub except Security/PIN, Location/MapStyle, the Other/WebView/WhatsNew tree
 * and Cloud Backup/Services, each of which is its own sibling NavGraph (see [SecurityNavKey],
 * [LocationNavKey], [OtherNavKey], [com.elementary.tasks.settings.export.ExportNavKey]) - into the
 * app's single, shared [androidx.navigation3.ui.NavDisplay] (see
 * [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
fun EntryProviderScope<NavKey>.settingsEntries(backStack: MutableList<NavKey>) {
  entry<SettingsNavKey.Hub> { HubEntry(backStack) }
  entry<SettingsNavKey.General> { GeneralEntry(backStack) }
  entry<SettingsNavKey.Reminders> { key -> RemindersEntry(key, backStack) }
  entry<SettingsNavKey.Calendar> { key -> CalendarEntry(key, backStack) }
  entry<SettingsNavKey.Birthday> { key -> BirthdayEntry(key, backStack) }
  entry<SettingsNavKey.Note> { key -> NoteEntry(key, backStack) }
  entry<SettingsNavKey.ManagePresets> { ManagePresetsEntry(backStack) }
  entry<SettingsNavKey.Developer> { DeveloperEntry(backStack) }
  entry<SettingsNavKey.ObjectExportTest> { ObjectExportEntry(backStack) }
  entry<SettingsNavKey.ProVersion> { ProVersionEntry(backStack) }
  entry<SettingsNavKey.Troubleshooting> { TroubleshootingEntry(backStack) }
}

@Composable
private fun HubEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<SettingsHubViewModel>()
  bindLifecycle(viewModel)
  val activity = LocalActivity.current as FragmentActivity
  val prefs = koinInject<Prefs>()
  val state by viewModel.state.collectAsState()
  val isPlayServicesWarningVisible = remember { !SuperUtil.isGooglePlayServicesAvailable(activity) }
  val isBuyProBadgeVisible =
    remember {
      !BuildParams.isPro && !SuperUtil.isAppInstalled(activity, "com.cray.software.justreminderpro")
    }

  val pinLoginLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) backStack.add(SecurityNavKey.Security)
    }

  SettingsScaffold(
    title = stringResource(R.string.action_settings),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    SettingsHubScreen(
      state = state,
      isBuyProBadgeVisible = isBuyProBadgeVisible,
      isPlayServicesWarningVisible = isPlayServicesWarningVisible,
      onBuyProClick = { backStack.add(SettingsNavKey.ProVersion) },
      onUpdateClick = { SuperUtil.launchMarket(activity) },
      onGeneralClick = { backStack.add(SettingsNavKey.General) },
      onCloudBackupClick = { backStack.add(ExportNavKey.CloudBackup) },
      onCalendarClick = { backStack.add(SettingsNavKey.Calendar()) },
      onRemindersClick = { backStack.add(SettingsNavKey.Reminders()) },
      onBirthdaysClick = { backStack.add(SettingsNavKey.Birthday()) },
      onSecurityClick = {
        if (prefs.hasPinCode) {
          pinLoginLauncher.launch(LoginApi.authIntent(activity))
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
private fun GeneralEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<GeneralSettingsViewModel>()
  val activity = LocalActivity.current as FragmentActivity
  val prefs = koinInject<Prefs>()
  val state by viewModel.state.collectAsState()
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      GeneralSettingsEvent.RecreateActivity -> activity.recreate()
      GeneralSettingsEvent.ApplyDynamicColorsAndRecreate -> {
        if (prefs.useDynamicColors) DynamicColors.applyToActivityIfAvailable(activity)
        activity.recreate()
      }
      GeneralSettingsEvent.RestartApp -> activity.finishWith(SplashScreenActivity::class.java)
    }
  }

  SettingsScaffold(
    title = stringResource(R.string.general),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    GeneralSettingsScreen(
      state = state,
      onLanguageClick = viewModel::onLanguageClick,
      onThemeClick = viewModel::onThemeClick,
      onTimeFormatClick = viewModel::onTimeFormatClick,
      onDynamicColorsToggle = { viewModel.onDynamicColorsToggle() },
      onMetricToggle = { viewModel.onMetricToggle() },
      onAnalyticsToggle = { viewModel.onAnalyticsToggle() },
      onDialogOptionSelected = viewModel::onDialogOptionSelected,
      onDialogDismiss = viewModel::onDialogDismiss,
      modifier = Modifier.padding(padding),
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
  val activity = LocalActivity.current as FragmentActivity
  val dateTimePickerProvider = koinInject<DateTimePickerProvider>()
  val permissionRequester = rememberPermissionRequester()
  val state by viewModel.state.collectAsState()
  val hasLocation = remember { Module.hasLocation(context) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      RemindersSettingsEvent.OpenPresets -> backStack.add(SettingsNavKey.ManagePresets)
      RemindersSettingsEvent.OpenLocationSettings -> backStack.add(LocationNavKey.Location)
      is RemindersSettingsEvent.ShowTimePicker -> {
        val titleRes = if (event.target == DndTimeTarget.FROM) R.string.from else R.string.to
        dateTimePickerProvider.showTimePicker(
          fragmentManager = activity.supportFragmentManager,
          time = event.time,
          title = context.getString(titleRes),
        ) { viewModel.onTimeSelected(event.target, it) }
      }
      RemindersSettingsEvent.ShowPermanentNotification -> PermanentReminderReceiver.show(context)
      RemindersSettingsEvent.HidePermanentNotification -> PermanentReminderReceiver.hide(context)
    }
  }

  PermissionRationaleDialog(permissionRequester)
  SettingsScaffold(
    title = key.screenTitle ?: stringResource(R.string.reminders_),
    navigationIcon = settingsNavigationIcon(key.screenTitle),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    RemindersSettingsScreen(
      state = state,
      hasLocation = hasLocation,
      onPresetsClick = viewModel::onPresetsClick,
      onLocationClick = viewModel::onLocationClick,
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
  val context = LocalContext.current
  val activity = LocalActivity.current as FragmentActivity
  val dialogues = koinInject<Dialogues>()
  val permissionRequester = rememberPermissionRequester()
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is CalendarSettingsEvent.ShowColorPicker -> {
        dialogues.showColorDialog(
          activity,
          event.currentColorIndex,
          event.title,
          ThemeProvider.colorsForSliderThemed(activity),
        ) { color -> viewModel.onColorSelected(event.target, color) }
      }
    }
  }
  viewModel.showSelectGoogleCalendarDialog.ObserveEvent { data ->
    val names = data.calendars.map { it.name }.toTypedArray()
    val builder = dialogues.getMaterialDialog(context)
    builder.setTitle(R.string.choose_calendar)
    var selectedPosition = data.selectedPosition
    builder.setSingleChoiceItems(names, data.selectedPosition) { _, i -> selectedPosition = i }
    builder.setPositiveButton(R.string.save) { dialog, _ ->
      viewModel.onCalendarSelected(selectedPosition)
      dialog.dismiss()
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    builder.create().show()
  }

  PermissionRationaleDialog(permissionRequester)
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
      onSelectCalendarClick = {
        permissionRequester.request(
          listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR),
          onGranted = { viewModel.onSelectGoogleCalendarClicked() },
        )
      },
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
  val dateTimePickerProvider = koinInject<DateTimePickerProvider>()
  val activity = LocalActivity.current as FragmentActivity
  val permissionRequester = rememberPermissionRequester()
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is BirthdaySettingsEvent.ShowTimePicker -> {
        dateTimePickerProvider.showTimePicker(
          fragmentManager = activity.supportFragmentManager,
          time = event.time,
          title = context.getString(R.string.remind_at),
        ) { viewModel.onTimeSelected(it) }
      }
      is BirthdaySettingsEvent.UpdatePermanentNotificationVisibility -> {
        val action = if (event.visible) PermanentBirthdayReceiver.ACTION_SHOW else PermanentBirthdayReceiver.ACTION_HIDE
        context.sendBroadcast(Intent(context, PermanentBirthdayReceiver::class.java).setAction(action))
      }
    }
  }

  PermissionRationaleDialog(permissionRequester)
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
  val reviewsApi = koinInject<ReviewsApi>()
  val activity = LocalActivity.current as FragmentActivity
  val context = LocalContext.current
  val state by viewModel.state.collectAsState()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      DeveloperEvent.OpenObjectExport -> backStack.add(SettingsNavKey.ObjectExportTest)
      DeveloperEvent.OpenReviewDialog -> {
        reviewsApi.showFeedbackForm(
          context = context,
          title = "Write a review",
          appSource = if (BuildParams.isPro) AppSource.PRO else AppSource.FREE,
          allowLogsAttachment = false,
        )
      }
      is DeveloperEvent.OpenReminderAction -> ReminderActionActivity.mockTest(context, event.reminderId)
      is DeveloperEvent.OpenBirthdayAction -> BirthdayActionActivity.mockTest(context, event.birthdayId)
      DeveloperEvent.OpenProVersion -> backStack.add(SettingsNavKey.ProVersion)
    }
  }
  viewModel.bannersReset.ObserveEvent { activity.toast("Home Screen banners have been reset") }
  viewModel.actionMessage.ObserveEvent { message -> activity.toast(message) }

  SettingsScaffold(
    title = "Developer",
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    DeveloperScreen(
      state = state,
      onResetBannersClick = viewModel::onResetBannersClick,
      onBirthdayDialogClick = viewModel::onBirthdayDialogClick,
      onReminderDialogClick = viewModel::onReminderDialogClick,
      onObjectExportClick = viewModel::onObjectExportClick,
      onReviewDialogClick = viewModel::onReviewDialogClick,
      onProVersionClick = viewModel::onProVersionClick,
      onClearTableClick = viewModel::onClearTableClick,
      onClearAllTablesClick = viewModel::onClearAllTablesClick,
      onClearAllTablesConfirm = viewModel::onClearAllTablesConfirm,
      onClearAllTablesDismiss = viewModel::onClearAllTablesDismiss,
      onInsertDemoDataClick = viewModel::onInsertDemoDataClick,
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
  val activity = LocalActivity.current as FragmentActivity
  ProVersionScreen(
    advantages = viewModel.state.advantages,
    onBackClick = { backStack.removeLastOrNull() },
    onBuyClick = { SuperUtil.launchMarket(activity) },
  )
}

@Composable
private fun TroubleshootingEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<TroubleshootingViewModel>()
  bindLifecycle(viewModel)
  val activity = LocalActivity.current as FragmentActivity
  val analyticsEventSender = koinInject<AnalyticsEventSender>()
  val hideBatteryOptimizationCard by viewModel.hideBatteryOptimizationCard.observeAsState(false)
  val showEmptyView by viewModel.showEmptyView.observeAsState(false)
  val showSendLogs by viewModel.showSendLogs.observeAsState(false)
  viewModel.sendLogFile.ObserveNonNull { file ->
    TelephonyUtil.sendMail(
      context = activity,
      email = "feedback.cray@gmail.com",
      subject = "Issue Logs",
      message = "Hi,\n\nHere is logs for my issue.\n\nIssue description: \n\nBest regards\n",
      file = file,
    )
  }

  SettingsScaffold(
    title = stringResource(R.string.troubleshooting),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    TroubleshootingScreen(
      showSendLogs = showSendLogs,
      showBatteryOptimizationCard = !hideBatteryOptimizationCard,
      showEmptyView = showEmptyView,
      onSendLogsClick = viewModel::sendLogs,
      onDisableOptimizationClick = {
        analyticsEventSender.send(ScreenUsedEvent(Screen.TROUBLESHOOTING))
        openBatteryOptimizationSettings(activity, viewModel)
      },
      modifier = Modifier.padding(padding),
    )
  }
}

private fun openBatteryOptimizationSettings(
  activity: Activity,
  viewModel: TroubleshootingViewModel,
) {
  fun openAppSettings() {
    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
    with(intent) {
      data = Uri.fromParts("package", viewModel.packageName(), null)
      addCategory(CATEGORY_DEFAULT)
      addFlags(FLAG_ACTIVITY_NEW_TASK)
      addFlags(FLAG_ACTIVITY_NO_HISTORY)
      addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
    activity.startActivity(intent)
  }

  when (Build.MANUFACTURER) {
    "samsung" -> {
      val intent = Intent()
      intent.component =
        ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
      try {
        activity.startActivity(intent)
      } catch (ex: ActivityNotFoundException) {
        openAppSettings()
      }
    }

    "xiaomi" -> {
      var intent = Intent()
      intent.component =
        ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
      try {
        activity.startActivity(intent)
      } catch (ex: ActivityNotFoundException) {
        try {
          intent = Intent()
          intent.setComponent(
            ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"),
          )
          intent.putExtra("package_name", viewModel.packageName())
          intent.putExtra("package_label", activity.getText(R.string.app_name))
          activity.startActivity(intent)
        } catch (anfe: ActivityNotFoundException) {
          openAppSettings()
        }
      }
    }

    "huawei" -> {
      val intent = Intent()
      intent.component =
        ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
      try {
        activity.startActivity(intent)
      } catch (ex: ActivityNotFoundException) {
        openAppSettings()
      }
    }

    else -> openAppSettings()
  }
}

@Composable
private fun bindLifecycle(observer: DefaultLifecycleObserver) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(observer, lifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
