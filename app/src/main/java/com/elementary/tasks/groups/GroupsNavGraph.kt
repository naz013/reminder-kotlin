package com.elementary.tasks.groups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.groups.create.EditGroupScreen
import com.elementary.tasks.groups.create.EditGroupState
import com.elementary.tasks.groups.create.EditGroupViewModel
import com.elementary.tasks.groups.list.GroupsScreen
import com.elementary.tasks.groups.list.GroupsScreenState
import com.elementary.tasks.groups.list.GroupsViewModel
import com.elementary.tasks.navigation.nav3.hideKeyboard
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.ui.common.Dialogues
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Contributes the Groups island's screens (Nav3 entries) and the routing between them into the
 * app's single, shared [androidx.navigation3.ui.NavDisplay] (see
 * [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
fun EntryProviderScope<NavKey>.groupsEntries(backStack: MutableList<NavKey>) {
  entry<GroupsNavKey.List> { GroupsListEntry(backStack) }
  entry<GroupsNavKey.Edit> { key -> GroupsEditEntry(key, backStack) }
}

@Composable
private fun GroupsListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<GroupsViewModel>()
  val dialogues = koinInject<Dialogues>()
  val context = LocalContext.current
  val deleteTitle = stringResource(R.string.delete)
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      GroupsViewModel.NavigationEvent.AddGroup -> backStack.add(GroupsNavKey.Edit())
      is GroupsViewModel.NavigationEvent.OpenEdit -> backStack.add(GroupsNavKey.Edit(event.id))
      is GroupsViewModel.NavigationEvent.ConfirmDelete -> {
        dialogues.askConfirmation(context, deleteTitle) { confirmed ->
          if (confirmed) viewModel.deleteGroup(event.id)
        }
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
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onCopyKeepClick = viewModel::onCopyKeepClick,
    onCopyReplaceClick = viewModel::onCopyReplaceClick,
    onDialogDismiss = viewModel::onDialogDismiss,
  )
}
