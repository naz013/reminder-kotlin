package com.elementary.tasks.reminder.todo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.core.compose.rememberDateTimeManager
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.navigation.nav3.rememberAppNavBridge
import com.github.naz013.tags.TagsNavKey
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
        appNavBridge.navigate(BuildReminderNavKey.Main(id = event.reminderId, seedFromTodoEdit = true))
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
    onGroupChanged = viewModel::onGroupChanged,
    onTagToggle = viewModel::onTagToggle,
    onManageTagsClick = { backStack.add(TagsNavKey.Manage) },
    onSaveClick = viewModel::onSaveClick,
    onExtendClick = viewModel::onExtendClick,
  )
}
