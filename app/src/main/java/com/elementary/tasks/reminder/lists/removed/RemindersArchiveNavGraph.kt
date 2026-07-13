package com.elementary.tasks.reminder.lists.removed

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.navigation.nav3.rememberAppNavBridge
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import org.koin.compose.viewmodel.koinViewModel

/**
 * Contributes the reminders archive screen (Nav3 entry) into the app's single, shared
 * [androidx.navigation3.ui.NavDisplay] (see [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
fun EntryProviderScope<NavKey>.remindersArchiveEntries(backStack: MutableList<NavKey>) {
  entry<RemindersArchiveNavKey.List> { ListEntry(backStack) }
}

@Composable
private fun ListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<RemindersArchiveViewModel>()
  bindLifecycle(viewModel)
  val context = LocalContext.current
  val dialogDispatcher = rememberDialogDispatcher()
  val appNavBridge = rememberAppNavBridge()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is RemindersArchiveViewModel.NavigationEvent.OpenEdit -> {
        appNavBridge.navigate(BuildReminderNavKey.Main(id = event.id))
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
        Toast.makeText(context, R.string.archive_was_emptied, Toast.LENGTH_SHORT).show()
      }
    }
  }

  val state by viewModel.state.collectAsState()
  RemindersArchiveScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onDeleteAllClick = viewModel::onDeleteAllClick,
    onItemClick = viewModel::onItemClick,
    onMenuAction = viewModel::onMenuAction,
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
