package com.github.naz013.feature.reminder.lists.removed

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.common.viewmodel.PendingEntity
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.navigation.DetailPanePlaceholder
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.PendingDeleteHost
import com.github.naz013.ui.common.compose.foundation.snackbar.UndoDeleteHost
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.remindersArchiveEntries(
  backStack: MutableList<NavKey>,
  selectedItemId: String?,
  onOpenEdit: (id: String) -> Unit,
) {
  entry<RemindersArchiveNavKey.List>(
    metadata = ListDetailSceneStrategy.listPane(
      detailPlaceholder = {
        DetailPanePlaceholder(
          text = stringResource(R.string.select_reminder_to_see_details),
          icon = AppIcons.Fluent.Archive,
        )
      },
    ),
  ) { ListEntry(backStack, selectedItemId, onOpenEdit) }
}

@Composable
private fun ListEntry(
  backStack: MutableList<NavKey>,
  selectedItemId: String?,
  onOpenEdit: (id: String) -> Unit,
) {
  val viewModel = koinViewModel<RemindersArchiveViewModel>()

  LaunchedEffect(selectedItemId) { viewModel.onSelectedItemIdChanged(selectedItemId) }

  val dialogDispatcher = rememberDialogDispatcher()
  val toastDispatcher = rememberToastDispatcher()
  val snackbarHostState = remember { SnackbarHostState() }
  UndoDeleteHost(controller = viewModel.undoDelete, snackbarHostState = snackbarHostState)
  PendingDeleteHost(
    coordinator = viewModel.pendingDeleteCoordinator,
    type = PendingEntity.REMINDER,
    snackbarHostState = snackbarHostState,
  )

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is RemindersArchiveViewModel.NavigationEvent.OpenEdit -> {
        onOpenEdit(event.id)
      }

      RemindersArchiveViewModel.NavigationEvent.ConfirmDeleteAll -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.delete_all_archived_reminders,
          positiveButtonRes = R.string.yes_delete_all,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.deleteAll() },
        )
      }

      RemindersArchiveViewModel.NavigationEvent.ArchiveEmptied -> {
        toastDispatcher.showToast(messageRes = R.string.archive_was_emptied)
      }

      RemindersArchiveViewModel.NavigationEvent.ReminderRestored -> {
        toastDispatcher.showToast(messageRes = R.string.reminder_restored)
      }

      RemindersArchiveViewModel.NavigationEvent.ReminderRestoredInactive -> {
        toastDispatcher.showToast(messageRes = R.string.reminder_restored_inactive)
      }
    }
  }

  val state by viewModel.state.collectAsState(RemindersArchiveScreenState())
  RemindersArchiveScreen(
    state = state,
    snackbarHostState = snackbarHostState,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onDeleteAllClick = viewModel::onDeleteAllClick,
    onItemClick = viewModel::onItemClick,
    onMenuAction = viewModel::onMenuAction,
  )
}
