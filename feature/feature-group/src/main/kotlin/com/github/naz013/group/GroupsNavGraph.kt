package com.github.naz013.group

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
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
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.navigation.DetailPanePlaceholder
import com.github.naz013.ui.common.compose.hideKeyboard
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Registers the groups list and edit screens. Group details stays out of this module for now: it
 * reuses the app's reminder-list UI and navigates into the (not yet extracted) reminder-build and
 * reminder-preview features, so the app registers that entry itself against [GroupsNavKey.Details].
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.groupsEntries(
  backStack: MutableList<NavKey>,
  isRenderedAsDetailPane: (NavKey) -> Boolean,
  adsContent: @Composable () -> Unit = {},
  onNotificationHelpClick: () -> Unit = {},
  onNewReminderClick: (groupUuId: String) -> Unit = {},
  onReminderPreviewClick: (reminderUuId: String) -> Unit = {},
  onRulesForGroupClick: (groupUuId: String) -> Unit = {},
) {
  entry<GroupsNavKey.List>(
    metadata = ListDetailSceneStrategy.listPane(
      detailPlaceholder = {
        DetailPanePlaceholder(
          text = stringResource(R.string.select_group_to_see_details),
          icon = AppIcons.Fluent.Group,
        )
      },
    ),
  ) { GroupsListEntry(backStack) }
  entry<GroupsNavKey.Edit>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    GroupsEditEntry(key, backStack, renderAsDetailPane, adsContent, onNotificationHelpClick, onRulesForGroupClick)
  }
  entry<GroupsNavKey.Details>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    GroupsDetailsEntry(key, backStack, renderAsDetailPane, adsContent, onNewReminderClick, onReminderPreviewClick)
  }
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

  val selectedItemId =
    backStack.lastOrNull()?.let { key ->
      when (key) {
        is GroupsNavKey.Details -> key.id
        is GroupsNavKey.Edit -> key.id.takeIf { it.isNotBlank() }
        else -> null
      }
    }
  LaunchedEffect(selectedItemId) { viewModel.onSelectedItemIdChanged(selectedItemId) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      GroupsViewModel.NavigationEvent.AddGroup -> backStack.navigateToDetailPane(GroupsNavKey.Edit())
      is GroupsViewModel.NavigationEvent.OpenEdit -> {
        backStack.navigateToEditDetailPane(GroupsNavKey.Edit(event.id)) {
          it is GroupsNavKey.Details && it.id == event.id
        }
      }

      is GroupsViewModel.NavigationEvent.OpenDetails -> {
        backStack.navigateToDetailPane(GroupsNavKey.Details(event.id))
      }

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
  renderAsDetailPane: Boolean,
  adsContent: @Composable () -> Unit,
  onNotificationHelpClick: () -> Unit,
  onRulesForGroupClick: (groupUuId: String) -> Unit,
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
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSaveClick = viewModel::onSaveClick,
    onDeleteMenuClick = viewModel::onDeleteMenuClick,
    onNameChange = viewModel::onNameChanged,
    onColorSelected = viewModel::onColorSelected,
    onDefaultCheckChanged = viewModel::onDefaultCheckChanged,
    onWorkflowRulesClick = {
      state.id?.let { groupId -> onRulesForGroupClick(groupId) }
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
  renderAsDetailPane: Boolean,
  adsContent: @Composable () -> Unit,
  onNewReminderClick: (groupUuId: String) -> Unit = {},
  onReminderPreviewClick: (reminderUuId: String) -> Unit = {},
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
      is GroupDetailsViewModel.NavigationEvent.OpenReminderPreview -> onReminderPreviewClick(event.id)
      is GroupDetailsViewModel.NavigationEvent.ConfirmDelete -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.delete_group_permanently,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.onDeleteConfirmed() }
        )
      }

      is GroupDetailsViewModel.NavigationEvent.OpenAddReminder -> {
        onNewReminderClick(event.groupUuId)
      }

      GroupDetailsViewModel.NavigationEvent.Deleted -> if (backStack.size > 1) backStack.removeLastOrNull()
    }
  }

  val state by viewModel.state.collectAsState(GroupDetailsState())
  GroupDetailsScreen(
    state = state,
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onEditClick = viewModel::onEditClick,
    onDeleteClick = viewModel::onDeleteClick,
    onReminderClick = viewModel::onReminderClick,
    onAddClick = viewModel::onAddReminderClicked,
    adsContent = adsContent,
  )
}

/**
 * Navigation for the groups two-pane list's detail pane: if the current top entry is itself a
 * group details or edit form, replace it instead of stacking another one on top. Mirrors
 * `BirthdaysNavGraph.kt`'s identically-purposed private helper - kept local here since
 * List/Edit/Details are all registered by this same graph.
 */
private fun MutableList<NavKey>.navigateToDetailPane(key: NavKey) {
  val top = lastOrNull()
  if (top is GroupsNavKey.Details || top is GroupsNavKey.Edit) {
    removeLastOrNull()
  }
  add(key)
}

/**
 * Navigation into an Edit screen from the groups detail pane: if the detail pane is currently
 * showing the Details of that very same group ([isSameItemDetails] matches the top entry), push
 * Edit on top of it instead of replacing it - see the matching comment in `BirthdaysNavGraph.kt`.
 */
private fun MutableList<NavKey>.navigateToEditDetailPane(key: NavKey, isSameItemDetails: (NavKey) -> Boolean) {
  val top = lastOrNull()
  if (top != null && isSameItemDetails(top)) {
    add(key)
  } else {
    navigateToDetailPane(key)
  }
}
