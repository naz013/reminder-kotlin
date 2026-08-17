package com.github.naz013.group

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.feature.reminder.preview.ReminderPreviewNavKey
import com.github.naz013.feature.workflow.WorkflowNavKey
import com.github.naz013.group.create.EditGroupScreen
import com.github.naz013.group.create.EditGroupState
import com.github.naz013.group.create.EditGroupViewModel
import com.github.naz013.group.details.GroupDetailsScreen
import com.github.naz013.group.details.GroupDetailsState
import com.github.naz013.group.details.GroupDetailsViewModel
import com.github.naz013.group.list.GroupsScreen
import com.github.naz013.group.list.GroupsScreenState
import com.github.naz013.group.list.GroupsViewModel
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.hideKeyboard
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Registers the groups list and edit screens. Group details stays out of this module for now: it
 * reuses the app's reminder-list UI and navigates into the (not yet extracted) reminder-build and
 * reminder-preview features, so the app registers that entry itself against [GroupsNavKey.Details].
 */
fun EntryProviderScope<NavKey>.groupsEntries(
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit = {},
  onNotificationHelpClick: () -> Unit = {},
) {
  entry<GroupsNavKey.List> { GroupsListEntry(backStack) }
  entry<GroupsNavKey.Edit> { key -> GroupsEditEntry(key, backStack, adsContent, onNotificationHelpClick) }
  entry<GroupsNavKey.Details> { key -> GroupsDetailsEntry(key, backStack, adsContent) }
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
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onAddClick = viewModel::onAddClick,
    onGroupClick = viewModel::onGroupClick,
    onGroupMenuAction = viewModel::onGroupMenuAction,
  )
}

@Composable
private fun GroupsEditEntry(
  key: GroupsNavKey.Edit,
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit,
  onNotificationHelpClick: () -> Unit,
) {
  val viewModel = koinViewModel<EditGroupViewModel> { parametersOf(key.id, key.fromIntentData) }
  val context = LocalContext.current
  DisposableEffect(viewModel) {
    onDispose { context.hideKeyboard() }
  }
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is EditGroupViewModel.NavigationEvent.Back -> if (backStack.size > 1) backStack.removeLastOrNull()
    }
  }

  val state by viewModel.state.collectAsState(EditGroupState())
  EditGroupScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
    onNotificationHelpClick = onNotificationHelpClick,
    onNotificationChoiceSelected = viewModel::onNotificationChoiceSelected,
    onDelayMinutesClick = viewModel::onDelayMinutesClick,
    onDelayMinutesOverrideToggle = viewModel::onDelayMinutesOverrideToggle,
    onDelayMinutesPreviewChange = viewModel::onDelayMinutesPreviewChange,
    onDelayMinutesConfirm = viewModel::onDelayMinutesConfirm,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onCopyKeepClick = viewModel::onCopyKeepClick,
    onCopyReplaceClick = viewModel::onCopyReplaceClick,
    onDialogDismiss = viewModel::onDialogDismiss,
    adsContent = adsContent,
  )
}

@Composable
private fun GroupsDetailsEntry(
  key: GroupsNavKey.Details,
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit,
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
      GroupDetailsViewModel.NavigationEvent.Deleted -> if (backStack.size > 1) backStack.removeLastOrNull()
    }
  }

  val state by viewModel.state.collectAsState(GroupDetailsState())
  GroupDetailsScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onEditClick = viewModel::onEditClick,
    onDeleteClick = viewModel::onDeleteClick,
    onReminderClick = viewModel::onReminderClick,
    onAddClick = viewModel::onAddReminderClicked,
    adsContent = adsContent,
  )
}
