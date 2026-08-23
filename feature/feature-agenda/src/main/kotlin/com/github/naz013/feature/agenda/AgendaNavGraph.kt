package com.github.naz013.feature.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.common.Permissions
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.agendaEntries(
  backStack: MutableList<NavKey>,
  onOpenReminderPreview: (id: String) -> Unit,
  onOpenReminderEdit: (id: String) -> Unit,
  onOpenNewReminder: () -> Unit,
  onOpenNewTodo: () -> Unit,
  onOpenBirthdayPreview: (id: String) -> Unit,
  onOpenBirthdayEdit: (id: String) -> Unit,
  onOpenNewBirthday: () -> Unit,
  onOpenArchive: () -> Unit,
  onOpenGroups: () -> Unit,
  onOpenTags: () -> Unit,
) {
  entry<AgendaNavKey.List> {
    AgendaEntry(
      backStack = backStack,
      onOpenReminderPreview = onOpenReminderPreview,
      onOpenReminderEdit = onOpenReminderEdit,
      onOpenNewReminder = onOpenNewReminder,
      onOpenNewTodo = onOpenNewTodo,
      onOpenBirthdayPreview = onOpenBirthdayPreview,
      onOpenBirthdayEdit = onOpenBirthdayEdit,
      onOpenNewBirthday = onOpenNewBirthday,
      onOpenArchive = onOpenArchive,
      onOpenGroups = onOpenGroups,
      onOpenTags = onOpenTags,
    )
  }
}

@Composable
private fun AgendaEntry(
  backStack: MutableList<NavKey>,
  onOpenReminderPreview: (id: String) -> Unit,
  onOpenReminderEdit: (id: String) -> Unit,
  onOpenNewReminder: () -> Unit,
  onOpenNewTodo: () -> Unit,
  onOpenBirthdayPreview: (id: String) -> Unit,
  onOpenBirthdayEdit: (id: String) -> Unit,
  onOpenNewBirthday: () -> Unit,
  onOpenArchive: () -> Unit,
  onOpenGroups: () -> Unit,
  onOpenTags: () -> Unit,
) {
  val viewModel = koinViewModel<AgendaViewModel>()
  val permissionRequester = rememberPermissionRequesterRationale()
  val dialogDispatcher = rememberDialogDispatcher()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is AgendaViewModel.NavigationEvent.OpenReminderPreview -> {
        onOpenReminderPreview(event.id)
      }

      is AgendaViewModel.NavigationEvent.OpenReminderEdit -> {
        onOpenReminderEdit(event.id)
      }

      AgendaViewModel.NavigationEvent.OpenNewReminder -> {
        onOpenNewReminder()
      }

      AgendaViewModel.NavigationEvent.OpenNewTodo -> {
        onOpenNewTodo()
      }

      is AgendaViewModel.NavigationEvent.OpenBirthdayPreview -> {
        onOpenBirthdayPreview(event.id)
      }

      is AgendaViewModel.NavigationEvent.OpenBirthdayEdit -> {
        onOpenBirthdayEdit(event.id)
      }

      AgendaViewModel.NavigationEvent.OpenNewBirthday -> {
        onOpenNewBirthday()
      }

      AgendaViewModel.NavigationEvent.OpenArchive -> {
        onOpenArchive()
      }

      AgendaViewModel.NavigationEvent.OpenGroups -> {
        onOpenGroups()
      }

      AgendaViewModel.NavigationEvent.OpenTags -> {
        onOpenTags()
      }

      is AgendaViewModel.NavigationEvent.RequestGpsPermission -> {
        permissionRequester.request(
          listOf(Permissions.FOREGROUND_SERVICE, Permissions.FOREGROUND_SERVICE_LOCATION),
          onGranted = { viewModel.toggleReminder(event.id) },
        )
      }

      is AgendaViewModel.NavigationEvent.ConfirmArchiveReminder -> {
        dialogDispatcher.showDialog(
          titleRes = R.string.move_to_archive,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = {
            viewModel.moveReminderToArchive(event.id)
          }
        )
      }

      is AgendaViewModel.NavigationEvent.ConfirmDeleteReminder -> {
        dialogDispatcher.showDialog(
          textRes = R.string.delete_reminder_permanently,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = {
            viewModel.deleteReminder(event.id)
          }
        )
      }

      is AgendaViewModel.NavigationEvent.ConfirmDeleteBirthday -> {
        dialogDispatcher.showDialog(
          textRes = R.string.delete_birthday_permanently,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.deleteBirthday(event.id) }
        )
      }
    }
  }

  val state by viewModel.agendaScreenState.collectAsState(AgendaScreenState())
  AgendaScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onCategoryToggle = viewModel::onCategoryToggle,
    onSmartListSelected = viewModel::onSmartListSelected,
    onTagFilterSelected = viewModel::onTagFilterSelected,
    onGroupFilterSelected = viewModel::onGroupFilterSelected,
    onAddReminderClick = viewModel::onAddReminderClick,
    onAddTodoClick = viewModel::onAddTodoClick,
    onAddBirthdayClick = viewModel::onAddBirthdayClick,
    onArchiveClick = viewModel::onArchiveClick,
    onGroupsClick = viewModel::onGroupsClick,
    onTagsClick = viewModel::onTagsClick,
    onItemClick = viewModel::onItemClick,
    onAgendaMenuAction = viewModel::onAgendaMenuAction,
    hasScrolledToToday = viewModel.hasScrolledToToday,
    onScrolledToToday = viewModel::onScrolledToToday,
  )
}
