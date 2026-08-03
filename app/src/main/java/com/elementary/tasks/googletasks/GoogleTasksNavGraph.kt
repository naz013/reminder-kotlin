package com.elementary.tasks.googletasks

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.ads.AdBanner
import com.elementary.tasks.ads.NormalAdBanner
import com.elementary.tasks.core.cloud.compose.rememberGoogleTasksLogin
import com.elementary.tasks.core.os.compose.rememberPermissionRequesterRationale
import com.elementary.tasks.core.utils.ui.compose.rememberDateTimePicker
import com.elementary.tasks.googletasks.list.TaskListScreen
import com.elementary.tasks.googletasks.list.TaskListState
import com.elementary.tasks.googletasks.list.TaskListViewModel
import com.elementary.tasks.googletasks.preview.PreviewGoogleTaskScreen
import com.elementary.tasks.googletasks.preview.PreviewGoogleTaskState
import com.elementary.tasks.googletasks.preview.PreviewGoogleTaskViewModel
import com.elementary.tasks.googletasks.task.EditGoogleTaskScreen
import com.elementary.tasks.googletasks.task.EditGoogleTaskState
import com.elementary.tasks.googletasks.task.EditGoogleTaskViewModel
import com.elementary.tasks.googletasks.tasklist.EditGoogleTaskListScreen
import com.elementary.tasks.googletasks.tasklist.EditGoogleTaskListState
import com.elementary.tasks.googletasks.tasklist.EditGoogleTaskListViewModel
import com.elementary.tasks.navigation.nav3.hideKeyboard
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.common.Permissions
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.googleTasksEntries(backStack: MutableList<NavKey>) {
  entry<GoogleTasksNavKey.List> { GoogleTasksListEntry(backStack) }
  entry<GoogleTasksNavKey.TaskList> { key -> TaskListEntry(key, backStack) }
  entry<GoogleTasksNavKey.TaskPreview> { key -> TaskPreviewEntry(key, backStack) }
  entry<GoogleTasksNavKey.TaskEdit> { key -> TaskEditEntry(key, backStack) }
  entry<GoogleTasksNavKey.ListEdit> { key -> ListEditEntry(key, backStack) }
}

@Composable
private fun GoogleTasksListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<GoogleTasksViewModel>()

  val googleTasksLogin = rememberGoogleTasksLogin(
    onResult = { viewModel.onGoogleTasksLoginStateChanged(it) },
    onFail = { viewModel.onGoogleTasksAuthFailed() },
  )

  val permissionRequester = rememberPermissionRequesterRationale()
  val toastDispatcher = rememberToastDispatcher()
  val dialogDispatcher = rememberDialogDispatcher()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      GoogleTasksViewModel.ViewModelEvent.MoveBack -> backStack.removeLastOrNull()

      GoogleTasksViewModel.ViewModelEvent.Login -> {
        permissionRequester.request(
          Permissions.GET_ACCOUNTS,
          onGranted = { googleTasksLogin.login() },
        )
      }

      GoogleTasksViewModel.ViewModelEvent.ShowLoginError -> {
        dialogDispatcher.showDialog(
          textRes = R.string.failed_to_login,
          positiveButtonRes = R.string.ok,
        )
      }

      is GoogleTasksViewModel.ViewModelEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }
    }
  }

  val state by viewModel.state.collectAsState(GoogleTasksState())
  GoogleTasksScreen(
    state = state,
    onBackClick = { viewModel.onBackPressed() },
    onConnectClick = { viewModel.onLoginClicked() },
    onAddListClick = { backStack.add(GoogleTasksNavKey.ListEdit()) },
    onAddTaskClick = { backStack.add(GoogleTasksNavKey.TaskEdit()) },
    onTaskListClick = { listId -> backStack.add(GoogleTasksNavKey.TaskList(listId)) },
    onTaskClick = { id -> backStack.add(GoogleTasksNavKey.TaskPreview(id)) },
    onTaskToggle = viewModel::toggleTask,
    onRefresh = viewModel::sync,
  )
}

@Composable
private fun TaskListEntry(
  key: GoogleTasksNavKey.TaskList,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<TaskListViewModel> { parametersOf(key.listId) }

  val toastDispatcher = rememberToastDispatcher()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      TaskListViewModel.TaskListEvent.MoveBack -> backStack.removeLastOrNull()

      is TaskListViewModel.TaskListEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }

      is TaskListViewModel.TaskListEvent.EditTaskList -> {
        backStack.add(GoogleTasksNavKey.ListEdit(event.listId))
      }
    }
  }

  val state by viewModel.state.collectAsState(TaskListState())
  TaskListScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onEditListClick = { viewModel.onEditClicked() },
    onDeleteListClick = viewModel::onDeleteListClick,
    onDeleteConfirmed = viewModel::deleteGoogleTaskList,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    onClearCompletedClick = viewModel::clearList,
    onTaskClick = { id -> backStack.add(GoogleTasksNavKey.TaskPreview(id)) },
    onTaskToggle = viewModel::toggleTask,
    onAddTaskClick = { backStack.add(GoogleTasksNavKey.TaskEdit(listId = key.listId)) },
    onRefresh = viewModel::sync,
  )
}

@Composable
private fun TaskPreviewEntry(
  key: GoogleTasksNavKey.TaskPreview,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<PreviewGoogleTaskViewModel> { parametersOf(key.id) }

  val toastDispatcher = rememberToastDispatcher()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      PreviewGoogleTaskViewModel.PreviewGoogleTaskEvent.MoveBack -> backStack.removeLastOrNull()
      is PreviewGoogleTaskViewModel.PreviewGoogleTaskEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }
    }
  }

  val state by viewModel.state.collectAsState(PreviewGoogleTaskState())
  PreviewGoogleTaskScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onEditClick = { backStack.add(GoogleTasksNavKey.TaskEdit(id = key.id)) },
    onDeleteClick = viewModel::onDeleteClick,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    onCompleteClick = viewModel::onComplete,
    adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), AdBanner.GoogleTask) },
  )
}

@Composable
private fun TaskEditEntry(
  key: GoogleTasksNavKey.TaskEdit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<EditGoogleTaskViewModel> { parametersOf(key.id, key.listId) }

  val context = LocalContext.current
  val dateTimePicker = rememberDateTimePicker()
  val toastDispatcher = rememberToastDispatcher()

  DisposableEffect(viewModel) {
    onDispose {
      context.hideKeyboard()
    }
  }

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is EditGoogleTaskViewModel.EditGoogleTaskEvent.ShowDatePicker -> {
        dateTimePicker.showDatePicker(
          date = event.date,
          title = event.title,
          onDateSelected = { viewModel.onDateSet(it) },
        )
      }

      is EditGoogleTaskViewModel.EditGoogleTaskEvent.ShowTimePicker -> {
        dateTimePicker.showTimePicker(
          time = event.time,
          title = event.title,
          is24Hour = event.is24Hour,
          onTimeSelected = { viewModel.onTimeSet(it) },
        )
      }

      EditGoogleTaskViewModel.EditGoogleTaskEvent.MoveBack -> backStack.removeLastOrNull()

      is EditGoogleTaskViewModel.EditGoogleTaskEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }
    }
  }

  val state by viewModel.state.collectAsState(EditGoogleTaskState())

  EditGoogleTaskScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onSaveClick = viewModel::save,
    onDeleteMenuClick = viewModel::onDeleteMenuClick,
    onMoveMenuClick = viewModel::onMoveMenuClick,
    onTitleChange = viewModel::onTitleChange,
    onNotesChange = viewModel::onNotesChange,
    onDateFieldClick = viewModel::onDateFieldClick,
    onTimeFieldClick = viewModel::onTimeFieldClick,
    onListFieldClick = viewModel::onListFieldClick,
    onDateTypeSelected = viewModel::onDateTypeSelected,
    onTimeTypeSelected = viewModel::onTimeTypeSelected,
    onListPicked = viewModel::onListPicked,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDialogDismiss = viewModel::onDialogDismiss,
  )
}

@Composable
private fun ListEditEntry(
  key: GoogleTasksNavKey.ListEdit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<EditGoogleTaskListViewModel> { parametersOf(key.id) }

  val context = LocalContext.current
  val toastDispatcher = rememberToastDispatcher()

  DisposableEffect(viewModel) {
    onDispose {
      context.hideKeyboard()
    }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      EditGoogleTaskListViewModel.EditGoogleTaskListEvent.MoveBack -> {
        backStack.removeLastOrNull()
      }

      is EditGoogleTaskListViewModel.EditGoogleTaskListEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }
    }
  }

  val state by viewModel.state.collectAsState(EditGoogleTaskListState())
  EditGoogleTaskListScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onSaveClick = viewModel::save,
    onDeleteMenuClick = viewModel::onDeleteClick,
    onNameChange = viewModel::onNameChange,
    onColorSelected = viewModel::onColorSelected,
    onDefaultToggle = viewModel::onDefaultToggle,
    onDeleteConfirmed = viewModel::deleteGoogleTaskList,
    onDeleteDismiss = viewModel::onDeleteDismiss,
  )
}
