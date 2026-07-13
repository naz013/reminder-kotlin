package com.elementary.tasks.reminder.build

import android.content.Context
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.os.compose.PermissionRequester
import com.elementary.tasks.core.os.compose.rememberPermissionRequesterRationale
import com.elementary.tasks.core.os.datapicker.compose.rememberApplicationPicker
import com.elementary.tasks.core.os.datapicker.compose.rememberContactPhonePicker
import com.elementary.tasks.core.os.datapicker.compose.rememberMultipleUriPicker
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.build.bi.BiGroup
import com.elementary.tasks.reminder.build.help.ReminderHelpScreen
import com.elementary.tasks.reminder.build.selectordialog.BuilderSelectorSheet
import com.elementary.tasks.reminder.build.selectordialog.SelectorDialogDataHolder
import com.elementary.tasks.reminder.build.valuedialog.ValueEditorSheet
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter
import com.elementary.tasks.reminder.build.valuedialog.editor.MapEditorScreen
import com.elementary.tasks.reminder.recur.RecurHelpScreen
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.Permissions
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Place
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.ReviewsApi
import com.github.naz013.ui.common.Dialogues
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Contributes the reminder builder island's screens (Nav3 entries) and the routing between them
 * into the app's single, shared [androidx.navigation3.ui.NavDisplay] (see
 * [com.elementary.tasks.navigation.nav3.AppNavGraph]). Main, Configure, Help and RecurHelp each own
 * their own Material 3 Scaffold/TopAppBar - no shared chrome, matching every other promoted island.
 */
fun EntryProviderScope<NavKey>.buildReminderEntries(backStack: MutableList<NavKey>) {
  entry<BuildReminderNavKey.Main> { key -> MainEntry(key, backStack) }
  entry<BuildReminderNavKey.Configure> { ConfigureEntry(backStack) }
  entry<BuildReminderNavKey.Help> {
    ReminderHelpScreen(onBackClick = { backStack.removeLastOrNull() })
  }
  entry<BuildReminderNavKey.RecurHelp> {
    RecurHelpScreen(onBackClick = { backStack.removeLastOrNull() })
  }
}

@Composable
private fun MainEntry(
  key: BuildReminderNavKey.Main,
  backStack: MutableList<NavKey>,
) {
  // Passed as the single Main key object, not 6 loose positional values: Koin's parameter
  // resolution matches by KClass, and two same-typed values (id: String, deepLinkText: String?)
  // in one parametersOf() list resolved ambiguously - id's value was leaking into deepLinkText.
  val viewModel = koinViewModel<BuildReminderViewModel> { parametersOf(key) }
  bindLifecycle(viewModel)
  Logger.i("BuildReminderNavGraph", "Opening the reminder edit screen for id: ${Logger.data(viewModel.id)}")

  val context = LocalContext.current
  val activity = LocalActivity.current as FragmentActivity
  val dialogues = koinInject<Dialogues>()
  val prefs = koinInject<Prefs>()
  val reviewsApi = koinInject<ReviewsApi>()
  val featureManager = koinInject<FeatureManager>()
  val selectorDialogDataHolder = koinInject<SelectorDialogDataHolder>()
  val paramToTextAdapter = koinInject<ParamToTextAdapter>()
  val googleCalendarUtils = koinInject<GoogleCalendarUtils>()
  val packageManagerWrapper = koinInject<PackageManagerWrapper>()
  val attachmentFileAdapter = koinInject<UriToAttachmentFileAdapter>()
  val dateTimeManager = koinInject<DateTimeManager>()

  val permissionRequester = rememberPermissionRequesterRationale()
  val pickApplication = rememberApplicationPicker()
  val pickContactPhone = rememberContactPhonePicker()
  val pickFiles = rememberMultipleUriPicker()

  val builderItems by viewModel.builderItems.observeAsState(emptyList())
  val prediction by viewModel.showPrediction.observeAsState()
  val canSaveAsPreset by viewModel.canSaveAsPreset.observeAsState(false)
  val canSave by viewModel.canSave.observeAsState(false)

  var saveAsPresetChecked by remember { mutableStateOf(false) }
  var presetNameState by remember { mutableStateOf("") }
  var showSelector by remember { mutableStateOf(false) }
  var editingItem by remember { mutableStateOf<Pair<Int, BuilderItem<*>>?>(null) }

  // BuilderConfigureScreen (a sibling backstack entry, not nested in this composition) writes
  // each toggle straight to prefs and has no reference to this screen's BuildReminderViewModel
  // instance - this flag (rememberSaveable so it survives Main being unmounted while Configure is
  // shown) is how Main learns "the config may have changed" once it remounts after Configure pops,
  // matching the old ActivityResult-callback timing without needing to share a ViewModel across
  // NavEntries.
  var pendingConfigRefresh by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    if (pendingConfigRefresh) {
      pendingConfigRefresh = false
      viewModel.onConfigurationChanged()
    }
  }

  viewModel.askPermissions.ObserveEvent { list ->
    permissionRequester.request(list, onGranted = { viewModel.onPermissionsGranted() })
  }
  viewModel.askEditPermissions.ObserveEvent { list ->
    permissionRequester.request(list, onGranted = { viewModel.onEditPermissionsGranted() })
  }
  viewModel.showEditDialog.ObserveEvent { pair -> editingItem = pair }
  viewModel.resultEvent.ObserveEvent { commands ->
    when (commands) {
      Commands.DELETED, Commands.SAVED -> backStack.removeLastOrNull()
      else -> {}
    }
  }
  viewModel.showReviewDialog.ObserveEvent {
    showReviewDialog(context, reviewsApi, featureManager, context.getString(R.string.share_your_experience))
  }

  BuildReminderScreen(
    builderItems = builderItems,
    prediction = prediction,
    canSave = canSave,
    canRemove = viewModel.canRemove,
    canSaveAsPreset = canSaveAsPreset,
    saveAsPresetChecked = saveAsPresetChecked,
    presetName = presetNameState,
    onBackClick = { backStack.removeLastOrNull() },
    onSaveClick = {
      askNotificationPermissionIfNeeded(permissionRequester) {
        askCopySaving(context, dialogues, viewModel)
      }
    },
    onDeleteClick = { deleteReminder(context, dialogues, viewModel) },
    onConfigureClick = {
      pendingConfigRefresh = true
      backStack.add(BuildReminderNavKey.Configure)
    },
    onHelpClick = { backStack.add(BuildReminderNavKey.Help) },
    onReportIssueClick = {
      showReviewDialog(context, reviewsApi, featureManager, context.getString(R.string.report_an_issue))
    },
    onSaveAsPresetChange = {
      saveAsPresetChecked = it
      viewModel.saveAsPreset = it
    },
    onPresetNameChange = {
      presetNameState = it
      viewModel.presetName = it
    },
    onItemClick = { position, item -> viewModel.onItemEditedClicked(position, item) },
    onItemRemove = { position, item -> viewModel.removeItem(position, item) },
    onAddClick = { showSelector = true },
  )

  if (showSelector) {
    BuilderSelectorSheet(
      tabs = selectorDialogDataHolder.getTabs(),
      builderItems = selectorDialogDataHolder.selectorBuilderItems,
      presets = selectorDialogDataHolder.presets,
      recurPresets = selectorDialogDataHolder.recurPresets,
      onDismissRequest = { showSelector = false },
      onBuilderItemSelected = { builderItem ->
        showSelector = false
        viewModel.addItem(builderItem)
      },
      onPresetSelected = { preset ->
        showSelector = false
        viewModel.onPresetSelected(preset)
      },
    )
  }

  editingItem?.let { (position, item) ->
    // Arriving/Leaving coordinates host a real Fragment (SimpleMapFragment, Google Maps SDK) via
    // FragmentContainerView. ValueEditorSheet's AppModalBottomSheet renders in a separate Compose
    // Popup/Dialog window, which isn't part of the fragment view tree childFragmentManager
    // searches when resolving that container by id, so it must be shown in-place instead (see
    // MapEditorScreen's kdoc).
    if (item is ArrivingCoordinatesBuilderItem || item is LeavingCoordinatesBuilderItem) {
      @Suppress("UNCHECKED_CAST")
      MapEditorScreen(
        builderItem = item as BuilderItem<Place>,
        fragmentManager = activity.supportFragmentManager,
        dateTimeManager = dateTimeManager,
        onDismissRequest = { editingItem = null },
        onValueChange = { updated -> viewModel.updateValue(position, updated) },
      )
    } else {
      ValueEditorSheet(
        builderItem = item,
        is24HourFormat = prefs.is24HourFormat,
        paramToTextAdapter = paramToTextAdapter,
        googleCalendarUtils = googleCalendarUtils,
        packageManagerWrapper = packageManagerWrapper,
        attachmentFileAdapter = attachmentFileAdapter,
        dateTimeManager = dateTimeManager,
        onPickApplication = { onResult -> pickApplication(onResult) },
        onPickContact = { onResult ->
          permissionRequester.request(Permissions.READ_CONTACTS, onGranted = { pickContactPhone(onResult) })
        },
        onPickFiles = { onResult -> pickFiles(onResult) },
        onDismissRequest = { editingItem = null },
        onValueChange = { updated -> viewModel.updateValue(position, updated) },
        onHelpClick = if (item.biGroup == BiGroup.ICAL) {
          { backStack.add(BuildReminderNavKey.RecurHelp) }
        } else {
          null
        },
      )
    }
  }
}

@Composable
private fun ConfigureEntry(backStack: MutableList<NavKey>) {
  val configureViewModel = koinViewModel<BuilderConfigureViewModel>()
  val state by configureViewModel.state.collectAsState()
  BuilderConfigureScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onSummaryToggle = configureViewModel::onSummaryToggle,
    onBeforeToggle = configureViewModel::onBeforeToggle,
    onRepeatToggle = configureViewModel::onRepeatToggle,
    onRepeatLimitToggle = configureViewModel::onRepeatLimitToggle,
    onPriorityToggle = configureViewModel::onPriorityToggle,
    onAttachmentToggle = configureViewModel::onAttachmentToggle,
    onCalendarToggle = configureViewModel::onCalendarToggle,
    onTasksToggle = configureViewModel::onTasksToggle,
    onExtraToggle = configureViewModel::onExtraToggle,
    onLedToggle = configureViewModel::onLedToggle,
    onICalendarToggle = configureViewModel::onICalendarToggle,
    onMakeCallToggle = configureViewModel::onMakeCallToggle,
    onSendSmsToggle = configureViewModel::onSendSmsToggle,
    onOpenAppToggle = configureViewModel::onOpenAppToggle,
    onOpenLinkToggle = configureViewModel::onOpenLinkToggle,
    onSendEmailToggle = configureViewModel::onSendEmailToggle,
  )
}

private fun askNotificationPermissionIfNeeded(
  permissionRequester: PermissionRequester,
  onGranted: () -> Unit,
) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    permissionRequester.request(Permissions.POST_NOTIFICATION, onGranted = onGranted)
  } else {
    onGranted()
  }
}

private fun askCopySaving(
  context: Context,
  dialogues: Dialogues,
  viewModel: BuildReminderViewModel,
) {
  if (viewModel.isFromFile && viewModel.hasSameInDb) {
    dialogues
      .getMaterialDialog(context)
      .setMessage(R.string.same_reminder_message)
      .setPositiveButton(R.string.keep) { dialogInterface, _ ->
        dialogInterface.dismiss()
        viewModel.saveReminder(true)
      }.setNegativeButton(R.string.replace) { dialogInterface, _ ->
        dialogInterface.dismiss()
        viewModel.saveReminder(false)
      }.setNeutralButton(R.string.cancel) { dialogInterface, _ ->
        dialogInterface.dismiss()
      }.create()
      .show()
  } else {
    viewModel.saveReminder(false)
  }
}

private fun deleteReminder(
  context: Context,
  dialogues: Dialogues,
  viewModel: BuildReminderViewModel,
) {
  if (viewModel.isRemoved) {
    dialogues.askConfirmation(context, context.getString(R.string.delete)) { confirmed ->
      if (confirmed) viewModel.deleteReminder(true)
    }
  } else {
    dialogues.askConfirmation(context, context.getString(R.string.move_to_the_archive)) { confirmed ->
      if (confirmed) viewModel.moveToTrash()
    }
  }
}

private fun showReviewDialog(
  context: Context,
  reviewsApi: ReviewsApi,
  featureManager: FeatureManager,
  title: String,
) {
  val appSource = if (BuildParams.isPro) AppSource.PRO else AppSource.FREE
  reviewsApi.showFeedbackForm(
    context = context,
    title = title,
    appSource = appSource,
    allowLogsAttachment = featureManager.isFeatureEnabled(FeatureManager.Feature.LOGS_IN_REVIEWS),
  )
}

@Composable
private fun bindLifecycle(observer: DefaultLifecycleObserver) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(observer, lifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
