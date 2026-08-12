package com.elementary.tasks.reminder.todo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.core.compose.rememberDateTimeManager
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.navigation.nav3.rememberAppNavBridge
import com.github.naz013.tags.TagsNavKey
import com.github.naz013.ui.common.compose.foundation.dialog.DialogDispatcher
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.todoEditEntries(backStack: MutableList<NavKey>) {
  entry<TodoEditNavKey.Main> { key -> TodoEditEntry(key, backStack) }
}

@Composable
private fun TodoEditEntry(
  key: TodoEditNavKey.Main,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<TodoEditViewModel> { parametersOf(key) }
  val appNavBridge = rememberAppNavBridge()
  val toastDispatcher = rememberToastDispatcher()
  val dialogDispatcher = rememberDialogDispatcher()
  val dateTimeManager = rememberDateTimeManager()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      TodoEditViewModel.ViewModelEvent.MoveBack -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
      }

      is TodoEditViewModel.ViewModelEvent.ShowMessage -> {
        toastDispatcher.showToast(messageRes = event.messageRes)
      }

      is TodoEditViewModel.ViewModelEvent.OpenBuilder -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
        appNavBridge.navigate(
          BuildReminderNavKey.Main(
            id = event.reminderId,
            seedFromTodoEdit = true,
            isEditingExtend = event.isEditing,
          ),
        )
      }
    }
  }

  val state by viewModel.state.collectAsState()

  TodoEditScreen(
    state = state,
    dateTimeManager = dateTimeManager,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onTitleChange = viewModel::onTitleChange,
    onSubTasksChanged = viewModel::onSubTasksChanged,
    onGroupSelected = viewModel::onGroupSelected,
    onTagToggle = viewModel::onTagToggle,
    onManageTagsClick = { backStack.add(TagsNavKey.Manage) },
    onSaveClick = viewModel::onSaveClick,
    onExtendClick = viewModel::onExtendClick,
    onDeleteClick = { deleteReminder(dialogDispatcher, state, viewModel) },
  )
}

private fun deleteReminder(
  dialogDispatcher: DialogDispatcher,
  state: TodoEditState,
  viewModel: TodoEditViewModel,
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
