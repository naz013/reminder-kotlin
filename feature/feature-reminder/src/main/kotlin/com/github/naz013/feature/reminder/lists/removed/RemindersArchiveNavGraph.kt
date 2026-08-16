package com.github.naz013.feature.reminder.lists.removed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.remindersArchiveEntries(
  backStack: MutableList<NavKey>,
  navigateBeyondBackStack: (NavKey) -> Unit,
) {
  entry<RemindersArchiveNavKey.List> { ListEntry(backStack, navigateBeyondBackStack) }
}

@Composable
private fun ListEntry(
  backStack: MutableList<NavKey>,
  navigateBeyondBackStack: (NavKey) -> Unit,
) {
  val viewModel = koinViewModel<RemindersArchiveViewModel>()

  val dialogDispatcher = rememberDialogDispatcher()
  val toastDispatcher = rememberToastDispatcher()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is RemindersArchiveViewModel.NavigationEvent.OpenEdit -> {
        navigateBeyondBackStack(BuildReminderNavKey.Main(id = event.id))
      }

      is RemindersArchiveViewModel.NavigationEvent.ConfirmDeleteReminder -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.delete,
          textRes = R.string.are_you_sure,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.no,
          onPositive = { viewModel.deleteReminder(event.id) },
        )
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
    }
  }

  val state by viewModel.state.collectAsState(RemindersArchiveScreenState())
  RemindersArchiveScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onDeleteAllClick = viewModel::onDeleteAllClick,
    onItemClick = viewModel::onItemClick,
    onMenuAction = viewModel::onMenuAction,
  )
}
