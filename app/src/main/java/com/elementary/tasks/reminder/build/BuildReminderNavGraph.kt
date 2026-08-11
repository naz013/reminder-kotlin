package com.elementary.tasks.reminder.build

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.core.apps.SelectApplicationScreen
import com.elementary.tasks.core.compose.rememberDateTimeManager
import com.elementary.tasks.core.compose.rememberGoogleCalendarApi
import com.elementary.tasks.core.compose.rememberPackageManagerWrapper
import com.github.naz013.ui.common.permission.PermissionRequester
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.elementary.tasks.core.os.datapicker.compose.rememberContactPhonePicker
import com.elementary.tasks.core.os.datapicker.compose.rememberMultipleUriPicker
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.adapter.rememberParamToTextAdapter
import com.elementary.tasks.reminder.build.bi.BiGroup
import com.elementary.tasks.reminder.build.help.ReminderHelpScreen
import com.elementary.tasks.reminder.build.quickstart.QuickStartOption
import com.elementary.tasks.reminder.build.selectordialog.BuilderSelectorSheet
import com.elementary.tasks.reminder.build.selectordialog.rememberSelectorDialogDataHolder
import com.elementary.tasks.reminder.build.valuedialog.ValueEditorSheet
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.rememberUriToAttachmentFileAdapter
import com.elementary.tasks.reminder.build.valuedialog.editor.MapEditorScreen
import com.elementary.tasks.reminder.recur.RecurHelpScreen
import com.elementary.tasks.reminder.todo.TodoEditNavKey
import com.github.naz013.common.Permissions
import com.github.naz013.domain.Place
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.rememberPlayReviewLauncher
import com.github.naz013.reviews.rememberReviewsFormLauncher
import com.github.naz013.tags.TagsNavKey
import com.github.naz013.ui.common.compose.foundation.dialog.DialogDispatcher
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.buildReminderEntries(backStack: MutableList<NavKey>) {
  entry<BuildReminderNavKey.Main> { key -> MainEntry(key, backStack) }
  entry<BuildReminderNavKey.Help> {
    ReminderHelpScreen(onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() })
  }
  entry<BuildReminderNavKey.RecurHelp> {
    RecurHelpScreen(onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() })
  }
  entry<BuildReminderNavKey.SelectApplication> { SelectApplicationEntry(backStack) }
}

@Composable
private fun SelectApplicationEntry(backStack: MutableList<NavKey>) {
  val resultHolder = rememberApplicationPickerResultHolder()
  SelectApplicationScreen(
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onAppSelected = { packageName ->
      resultHolder.pendingPackageName = packageName
      if (backStack.size > 1) backStack.removeLastOrNull()
    },
  )
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
  Logger.i("BuildReminderNavGraph", "Opening the reminder edit screen for id: ${Logger.data(viewModel.id)}")

  val dialogDispatcher = rememberDialogDispatcher()
  val reviewsFormLauncher = rememberReviewsFormLauncher()
  val playReviewLauncher = rememberPlayReviewLauncher()
  val toastDispatcher = rememberToastDispatcher()

  val selectorDialogDataHolder = rememberSelectorDialogDataHolder()
  val paramToTextAdapter = rememberParamToTextAdapter()
  val googleCalendarApi = rememberGoogleCalendarApi()
  val packageManagerWrapper = rememberPackageManagerWrapper()
  val attachmentFileAdapter = rememberUriToAttachmentFileAdapter()
  val dateTimeManager = rememberDateTimeManager()

  val permissionRequester = rememberPermissionRequesterRationale()
  val pickContactPhone = rememberContactPhonePicker()
  val pickFiles = rememberMultipleUriPicker()
  val applicationPickerResultHolder = rememberApplicationPickerResultHolder()

  val state by viewModel.state.collectAsState()

  var showSelector by remember { mutableStateOf(false) }

  // SelectApplication is a separate Nav3 entry (its own ViewModelStoreOwner), so it can't hand
  // the picked package name back to viewModel directly - this position (rememberSaveable for the
  // same reason as pendingConfigRefresh above) plus the shared applicationPickerResultHolder is
  // how Main recovers "which item was being edited" once it remounts after the picker pops.
  var pendingApplicationPickPosition by rememberSaveable { mutableStateOf<Int?>(null) }
  LaunchedEffect(Unit) {
    val position = pendingApplicationPickPosition
    val packageName = applicationPickerResultHolder.pendingPackageName
    if (position != null && packageName != null) {
      pendingApplicationPickPosition = null
      applicationPickerResultHolder.pendingPackageName = null
      viewModel.onApplicationPicked(position, packageName)
    }
  }

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is BuildReminderViewModel.ViewModelEvent.AskPermissions -> {
        permissionRequester.request(event.permissions, onGranted = { viewModel.onPermissionsGranted() })
      }

      is BuildReminderViewModel.ViewModelEvent.AskEditPermissions -> {
        permissionRequester.request(event.permissions, onGranted = { viewModel.onEditPermissionsGranted() })
      }

      is BuildReminderViewModel.ViewModelEvent.ShowReviewDialog -> {
        reviewsFormLauncher.showFeedbackForm(
          title = event.title,
          appSource = event.appSource,
          allowLogsAttachment = event.canAttachLogs,
        )
      }

      BuildReminderViewModel.ViewModelEvent.ShowPlayReviewFlow -> {
        playReviewLauncher.launchReviewFlow()
      }

      BuildReminderViewModel.ViewModelEvent.MoveBack -> if (backStack.size > 1) backStack.removeLastOrNull()

      is BuildReminderViewModel.ViewModelEvent.ShowMessage -> {
        toastDispatcher.showToast(messageRes = event.messageRes)
      }

      BuildReminderViewModel.ViewModelEvent.OpenManageTags -> backStack.add(TagsNavKey.Manage)

      is BuildReminderViewModel.ViewModelEvent.RedirectToTodoEdit -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
        backStack.add(TodoEditNavKey.Main(id = event.id))
      }
    }
  }

  BuildReminderScreen(
    isLoadingForEdit = state.isLoadingForEdit,
    builderItems = state.builderItems,
    prediction = state.prediction,
    canSave = state.canSave,
    canRemove = state.canRemove,
    canSaveAsPreset = state.canSaveAsPreset,
    saveAsPresetChecked = state.saveAsPresetChecked,
    presetName = state.presetName,
    quickStartOptions = QuickStartOption.entries,
    allTags = state.allTags,
    selectedTagIds = state.selectedTagIds,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSaveClick = {
      askNotificationPermissionIfNeeded(permissionRequester) {
        askCopySaving(dialogDispatcher, state, viewModel)
      }
    },
    onDeleteClick = { deleteReminder(dialogDispatcher, state, viewModel) },
    onHelpClick = { backStack.add(BuildReminderNavKey.Help) },
    onReportIssueClick = viewModel::onReportAnIssueClicked,
    onSaveAsPresetChange = viewModel::onSaveAsPresetChange,
    onPresetNameChange = viewModel::onPresetNameChange,
    onItemClick = { position, item -> viewModel.onItemEditedClicked(position, item) },
    onItemRemove = { position, item -> viewModel.removeItem(position, item) },
    onAddClick = { showSelector = true },
    onQuickStartClick = viewModel::onQuickStartSelected,
    onTagToggle = viewModel::onTagToggle,
    onManageTagsClick = viewModel::onManageTagsClick,
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

  state.editingItem?.let { (position, item) ->
    // Arriving/Leaving coordinates use MapEditorScreen's own swipeable sheet rather than
    // ValueEditorSheet's AppModalBottomSheet (see MapEditorScreen's kdoc).
    if (item is ArrivingCoordinatesBuilderItem || item is LeavingCoordinatesBuilderItem) {
      @Suppress("UNCHECKED_CAST")
      MapEditorScreen(
        builderItem = item as BuilderItem<Place>,
        dateTimeManager = dateTimeManager,
        onDismissRequest = { viewModel.onEditDialogDismissed() },
        onValueChange = { updated -> viewModel.updateValue(position, updated) },
      )
    } else {
      ValueEditorSheet(
        builderItem = item,
        is24HourFormat = state.is24HourFormat,
        hapticFeedbackEnabled = state.hapticFeedbackEnabled,
        paramToTextAdapter = paramToTextAdapter,
        googleCalendarApi = googleCalendarApi,
        packageManagerWrapper = packageManagerWrapper,
        attachmentFileAdapter = attachmentFileAdapter,
        dateTimeManager = dateTimeManager,
        onPickApplication = {
          pendingApplicationPickPosition = position
          viewModel.onEditDialogDismissed()
          backStack.add(BuildReminderNavKey.SelectApplication)
        },
        onPickContact = { onResult ->
          permissionRequester.request(Permissions.READ_CONTACTS, onGranted = { pickContactPhone(onResult) })
        },
        onPickFiles = { onResult -> pickFiles(onResult) },
        onDismissRequest = { viewModel.onEditDialogDismissed() },
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
  dialogDispatcher: DialogDispatcher,
  state: BuildReminderState,
  viewModel: BuildReminderViewModel,
) {
  if (state.isFromFile && state.hasSameInDb) {
    dialogDispatcher.showDialog(
      textRes = R.string.same_reminder_message,
      positiveButtonRes = R.string.keep,
      negativeButtonRes = R.string.replace,
      neutralButtonRes = R.string.cancel,
      onPositive = { viewModel.saveReminder(true) },
      onNegative = { viewModel.saveReminder(false) },
    )
  } else {
    viewModel.saveReminder(false)
  }
}

private fun deleteReminder(
  dialogDispatcher: DialogDispatcher,
  state: BuildReminderState,
  viewModel: BuildReminderViewModel,
) {
  if (state.isRemoved) {
    dialogDispatcher.showDialog(
      titleRes = R.string.delete,
      textRes = R.string.are_you_sure,
      positiveButtonRes = R.string.yes,
      negativeButtonRes = R.string.no,
      onPositive = { viewModel.deleteReminder(true) },
    )
  } else {
    dialogDispatcher.showDialog(
      titleRes = R.string.move_to_the_archive,
      textRes = R.string.are_you_sure,
      positiveButtonRes = R.string.yes,
      negativeButtonRes = R.string.no,
      onPositive = { viewModel.moveToTrash() },
    )
  }
}
