package com.elementary.tasks.groups

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.ads.AdBanner
import com.elementary.tasks.ads.NormalAdBanner
import com.elementary.tasks.groups.create.EditGroupScreen
import com.elementary.tasks.groups.create.EditGroupState
import com.elementary.tasks.groups.create.EditGroupViewModel
import com.elementary.tasks.groups.details.GroupDetailsScreen
import com.elementary.tasks.groups.details.GroupDetailsState
import com.elementary.tasks.groups.details.GroupDetailsViewModel
import com.elementary.tasks.groups.list.GroupsScreen
import com.elementary.tasks.groups.list.GroupsScreenState
import com.elementary.tasks.groups.list.GroupsViewModel
import com.elementary.tasks.navigation.nav3.hideKeyboard
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.reminder.preview.ReminderPreviewNavKey
import com.elementary.tasks.settings.SettingsNavKey
import com.elementary.tasks.workflow.WorkflowNavKey
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.groupsEntries(backStack: MutableList<NavKey>) {
  entry<GroupsNavKey.List> { GroupsListEntry(backStack) }
  entry<GroupsNavKey.Details> { key -> GroupsDetailsEntry(key, backStack) }
  entry<GroupsNavKey.Edit> { key -> GroupsEditEntry(key, backStack) }
}

@Composable
private fun GroupsListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<GroupsViewModel>()

  val dialogDispatcher = rememberDialogDispatcher()

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(viewModel, lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          viewModel.refreshState()
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      GroupsViewModel.NavigationEvent.AddGroup -> backStack.add(GroupsNavKey.Edit())
      is GroupsViewModel.NavigationEvent.OpenEdit -> backStack.add(GroupsNavKey.Edit(event.id))
      is GroupsViewModel.NavigationEvent.OpenDetails -> backStack.add(GroupsNavKey.Details(event.id))
      is GroupsViewModel.NavigationEvent.ConfirmDelete -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.delete_group_permanently,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.deleteGroup(event.id) }
        )
      }
    }
  }

  val state by viewModel.state.collectAsState(GroupsScreenState())
  GroupsScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onAddClick = viewModel::onAddClick,
    onGroupClick = viewModel::onGroupClick,
    onGroupMenuAction = viewModel::onGroupMenuAction,
  )
}

@Composable
private fun GroupsDetailsEntry(
  key: GroupsNavKey.Details,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<GroupDetailsViewModel> { parametersOf(key.id) }
  val dialogDispatcher = rememberDialogDispatcher()

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(viewModel, lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          viewModel.refreshState()
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is GroupDetailsViewModel.NavigationEvent.OpenEdit -> backStack.add(GroupsNavKey.Edit(event.id))
      is GroupDetailsViewModel.NavigationEvent.OpenReminderPreview ->
        backStack.add(ReminderPreviewNavKey.Preview(event.id))
      is GroupDetailsViewModel.NavigationEvent.ConfirmDelete -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.delete_group_permanently,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.onDeleteConfirmed() }
        )
      }
      is GroupDetailsViewModel.NavigationEvent.OpenAddReminder -> {
        backStack.add(BuildReminderNavKey.Main(groupUuId = event.groupUuId))
      }
      GroupDetailsViewModel.NavigationEvent.Deleted -> backStack.removeLastOrNull()
    }
  }

  val state by viewModel.state.collectAsState(GroupDetailsState())
  GroupDetailsScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onEditClick = viewModel::onEditClick,
    onDeleteClick = viewModel::onDeleteClick,
    onReminderClick = viewModel::onReminderClick,
    onAddClick = viewModel::onAddReminderClicked,
    adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), adBanner = AdBanner.Group) },
  )
}

@Composable
private fun GroupsEditEntry(
  key: GroupsNavKey.Edit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<EditGroupViewModel> { parametersOf(key.id, key.fromIntentData) }
  val context = LocalContext.current
  DisposableEffect(viewModel) {
    onDispose { context.hideKeyboard() }
  }
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is EditGroupViewModel.NavigationEvent.Back -> backStack.removeLastOrNull()
    }
  }

  val state by viewModel.state.collectAsState(EditGroupState())
  EditGroupScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onSaveClick = viewModel::onSaveClick,
    onDeleteMenuClick = viewModel::onDeleteMenuClick,
    onNameChange = viewModel::onNameChanged,
    onColorSelected = viewModel::onColorSelected,
    onDefaultCheckChanged = viewModel::onDefaultCheckChanged,
    onWorkflowRulesClick = {
      state.id?.let { groupId -> backStack.add(WorkflowNavKey.RulesForGroup(groupId)) }
    },
    onVibrateClick = viewModel::onVibrateClick,
    onRepeatNotificationClick = viewModel::onRepeatNotificationClick,
    onBypassDndClick = viewModel::onBypassDndClick,
    onWakeScreenClick = viewModel::onWakeScreenClick,
    onPriorityClick = viewModel::onPriorityClick,
    onCategoryClick = viewModel::onCategoryClick,
    onLockScreenVisibilityClick = viewModel::onLockScreenVisibilityClick,
    onVibrationPatternClick = viewModel::onVibrationPatternClick,
    onNotificationHelpClick = { backStack.add(SettingsNavKey.NotificationCustomizationHelp) },
    onNotificationChoiceSelected = viewModel::onNotificationChoiceSelected,
    onDelayMinutesClick = viewModel::onDelayMinutesClick,
    onDelayMinutesOverrideToggle = viewModel::onDelayMinutesOverrideToggle,
    onDelayMinutesPreviewChange = viewModel::onDelayMinutesPreviewChange,
    onDelayMinutesConfirm = viewModel::onDelayMinutesConfirm,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onCopyKeepClick = viewModel::onCopyKeepClick,
    onCopyReplaceClick = viewModel::onCopyReplaceClick,
    onDialogDismiss = viewModel::onDialogDismiss,
    adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), adBanner = AdBanner.Group) },
  )
}
