package com.github.naz013.feature.googletask

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.common.Permissions
import com.github.naz013.feature.googletask.preview.PreviewGoogleTaskScreen
import com.github.naz013.feature.googletask.preview.PreviewGoogleTaskState
import com.github.naz013.feature.googletask.preview.PreviewGoogleTaskViewModel
import com.github.naz013.feature.googletask.task.EditGoogleTaskScreen
import com.github.naz013.feature.googletask.task.EditGoogleTaskState
import com.github.naz013.feature.googletask.task.EditGoogleTaskViewModel
import com.github.naz013.feature.googletask.tasklist.EditGoogleTaskListScreen
import com.github.naz013.feature.googletask.tasklist.EditGoogleTaskListState
import com.github.naz013.feature.googletask.tasklist.EditGoogleTaskListViewModel
import com.github.naz013.tags.TagsNavKey
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.navigation.DetailPanePlaceholder
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.github.naz013.ui.common.compose.hideKeyboard
import com.github.naz013.ui.common.datetime.rememberDateTimePicker
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.github.naz013.ui.googletask.rememberGoogleTasksLogin
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.googleTasksEntries(
  backStack: MutableList<NavKey>,
  isRenderedAsDetailPane: (NavKey) -> Boolean,
  adsContent: @Composable () -> Unit,
) {
  entry<GoogleTasksNavKey.List>(
    metadata = ListDetailSceneStrategy.listPane(
      detailPlaceholder = {
        DetailPanePlaceholder(
          text = stringResource(R.string.select_google_task_list_to_see_details),
          icon = AppIcons.Builder.GoogleTaskList,
        )
      },
    ),
  ) { GoogleTasksListEntry(backStack) }
  entry<GoogleTasksNavKey.TaskList>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    TaskListEntry(key, backStack, renderAsDetailPane)
  }
  entry<GoogleTasksNavKey.TaskPreview> { key -> TaskPreviewEntry(key, backStack, adsContent) }
  entry<GoogleTasksNavKey.TaskEdit> { key -> TaskEditEntry(key, backStack, adsContent) }
  entry<GoogleTasksNavKey.ListEdit> { key -> ListEditEntry(key, backStack, adsContent) }
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
      GoogleTasksViewModel.ViewModelEvent.MoveBack -> if (backStack.size > 1) backStack.removeLastOrNull()

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
    onTaskListClick = { listId -> backStack.navigateToDetailPane(GoogleTasksNavKey.TaskList(listId)) },
    onTaskClick = { id -> backStack.add(GoogleTasksNavKey.TaskPreview(id)) },
    onTaskToggle = viewModel::toggleTask,
    onRefresh = viewModel::sync,
    onTagSelected = viewModel::onTagSelected,
  )
}

@Composable
private fun TaskListEntry(
  key: GoogleTasksNavKey.TaskList,
  backStack: MutableList<NavKey>,
  renderAsDetailPane: Boolean,
) {
  val viewModel = koinViewModel<TaskListViewModel> { parametersOf(key.listId) }

  val toastDispatcher = rememberToastDispatcher()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      TaskListViewModel.TaskListEvent.MoveBack -> if (backStack.size > 1) backStack.removeLastOrNull()

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
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onEditListClick = { viewModel.onEditClicked() },
    onDeleteListClick = viewModel::onDeleteListClick,
    onDeleteConfirmed = viewModel::deleteGoogleTaskList,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    onClearCompletedClick = viewModel::clearList,
    onTaskClick = { id -> backStack.add(GoogleTasksNavKey.TaskPreview(id)) },
    onTaskToggle = viewModel::toggleTask,
    onAddTaskClick = { backStack.add(GoogleTasksNavKey.TaskEdit(listId = key.listId)) },
    onRefresh = viewModel::sync,
    onTagSelected = viewModel::onTagSelected,
  )
}

@Composable
private fun TaskPreviewEntry(
  key: GoogleTasksNavKey.TaskPreview,
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit,
) {
  val viewModel = koinViewModel<PreviewGoogleTaskViewModel> { parametersOf(key.id) }

  val toastDispatcher = rememberToastDispatcher()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      PreviewGoogleTaskViewModel.PreviewGoogleTaskEvent.MoveBack -> if (backStack.size > 1) backStack.removeLastOrNull()
      is PreviewGoogleTaskViewModel.PreviewGoogleTaskEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }
    }
  }

  val state by viewModel.state.collectAsState(PreviewGoogleTaskState())
  PreviewGoogleTaskScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onEditClick = { backStack.add(GoogleTasksNavKey.TaskEdit(id = key.id)) },
    onDeleteClick = viewModel::onDeleteClick,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    onCompleteClick = viewModel::onComplete,
    adsContent = adsContent,
  )
}

@Composable
private fun TaskEditEntry(
  key: GoogleTasksNavKey.TaskEdit,
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit,
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

      EditGoogleTaskViewModel.EditGoogleTaskEvent.MoveBack -> if (backStack.size > 1) backStack.removeLastOrNull()

      is EditGoogleTaskViewModel.EditGoogleTaskEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }

      EditGoogleTaskViewModel.EditGoogleTaskEvent.OpenManageTags -> backStack.add(TagsNavKey.Manage)
    }
  }

  val state by viewModel.state.collectAsState(EditGoogleTaskState())

  EditGoogleTaskScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
    onTagToggle = viewModel::onTagToggle,
    onManageTagsClick = viewModel::onManageTagsClick,
    adsContent = adsContent,
  )
}

@Composable
private fun ListEditEntry(
  key: GoogleTasksNavKey.ListEdit,
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit,
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
        if (backStack.size > 1) backStack.removeLastOrNull()
      }

      is EditGoogleTaskListViewModel.EditGoogleTaskListEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }
    }
  }

  val state by viewModel.state.collectAsState(EditGoogleTaskListState())
  EditGoogleTaskListScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSaveClick = viewModel::save,
    onDeleteMenuClick = viewModel::onDeleteClick,
    onNameChange = viewModel::onNameChange,
    onColorSelected = viewModel::onColorSelected,
    onDefaultToggle = viewModel::onDefaultToggle,
    onDeleteConfirmed = viewModel::deleteGoogleTaskList,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    adsContent = adsContent,
  )
}

/**
 * Navigation for the Google Tasks two-pane list's detail pane: if a task list is already open in
 * the detail pane, replace it instead of stacking another one on top - see the matching comment in
 * `BirthdaysNavGraph.kt`.
 */
private fun MutableList<NavKey>.navigateToDetailPane(key: NavKey) {
  if (lastOrNull() is GoogleTasksNavKey.TaskList) {
    removeLastOrNull()
  }
  add(key)
}
