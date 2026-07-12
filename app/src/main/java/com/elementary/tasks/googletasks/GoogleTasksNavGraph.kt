package com.elementary.tasks.googletasks

import android.app.Activity
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.compose.rememberGoogleTasksLogin
import com.elementary.tasks.core.os.compose.PermissionRationaleDialog
import com.elementary.tasks.core.os.compose.rememberPermissionRequester
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.googletasks.list.TaskListEvent
import com.elementary.tasks.googletasks.list.TaskListScreen
import com.elementary.tasks.googletasks.list.TaskListViewModel
import com.elementary.tasks.googletasks.preview.PreviewGoogleTaskEvent
import com.elementary.tasks.googletasks.preview.PreviewGoogleTaskScreen
import com.elementary.tasks.googletasks.preview.PreviewGoogleTaskViewModel
import com.elementary.tasks.googletasks.task.EditGoogleTaskEvent
import com.elementary.tasks.googletasks.task.EditGoogleTaskScreen
import com.elementary.tasks.googletasks.task.EditGoogleTaskViewModel
import com.elementary.tasks.googletasks.tasklist.EditGoogleTaskListEvent
import com.elementary.tasks.googletasks.tasklist.EditGoogleTaskListScreen
import com.elementary.tasks.googletasks.tasklist.EditGoogleTaskListViewModel
import com.elementary.tasks.navigation.nav3.hideKeyboard
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.Permissions
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Contributes the Google Tasks island's screens (Nav3 entries) and the routing between them into
 * the app's single, shared [androidx.navigation3.ui.NavDisplay] (see
 * [com.elementary.tasks.navigation.nav3.AppNavGraph]).
 */
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
  bindLifecycle(viewModel)
  val context = LocalContext.current
  val activity = LocalActivity.current as FragmentActivity
  val analyticsEventSender = koinInject<AnalyticsEventSender>()
  val permissionRequester = rememberPermissionRequester()
  var showLoginError by remember { mutableStateOf(false) }
  val googleTasksLogin =
    rememberGoogleTasksLogin(
      onResult = { isLogged ->
        viewModel.updateLoginStatus(isLogged)
        if (!isLogged) showLoginError = true
      },
      onFail = { showLoginError = true },
    )

  LaunchedEffect(Unit) {
    viewModel.updateLoginStatus(googleTasksLogin.isLogged)
    analyticsEventSender.send(ScreenUsedEvent(Screen.GOOGLE_TASKS_LIST))
  }

  val state by viewModel.state.collectAsState()
  PermissionRationaleDialog(permissionRequester)
  if (showLoginError) {
    AlertDialog(
      onDismissRequest = { showLoginError = false },
      text = { Text(stringResource(R.string.failed_to_login)) },
      confirmButton = {
        TextButton(onClick = { showLoginError = false }) { Text(stringResource(R.string.ok)) }
      },
    )
  }
  GoogleTasksScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onConnectClick = {
      permissionRequester.request(
        Permissions.GET_ACCOUNTS,
        onGranted = { withActivityCheck(activity) { googleTasksLogin.login() } },
      )
    },
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
  bindLifecycle(viewModel)
  val context = LocalContext.current
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      TaskListEvent.Deleted -> backStack.removeLastOrNull()
    }
  }
  viewModel.errorEvent.ObserveEvent { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }

  val state by viewModel.state.collectAsState()
  TaskListScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onEditListClick = {
      viewModel.currentTaskList?.also { backStack.add(GoogleTasksNavKey.ListEdit(it.listId)) }
    },
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
  bindLifecycle(viewModel)
  val context = LocalContext.current
  val adsProvider = remember { AdsProvider() }
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      PreviewGoogleTaskEvent.Deleted -> backStack.removeLastOrNull()
    }
  }
  viewModel.errorEvent.ObserveEvent { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }

  val state by viewModel.state.collectAsState()
  PreviewGoogleTaskScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onEditClick = { backStack.add(GoogleTasksNavKey.TaskEdit(id = key.id)) },
    onDeleteClick = viewModel::onDeleteClick,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    onCompleteClick = viewModel::onComplete,
    adsContent = { GoogleTaskAdBanner(adsProvider) },
  )
}

@Composable
private fun TaskEditEntry(
  key: GoogleTasksNavKey.TaskEdit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<EditGoogleTaskViewModel> { parametersOf(key.id, key.listId) }
  bindLifecycle(viewModel)
  val context = LocalContext.current
  val activity = LocalActivity.current as FragmentActivity
  val dateTimePickerProvider = koinInject<DateTimePickerProvider>()
  val appWidgetUpdater = koinInject<AppWidgetUpdater>()
  DisposableEffect(viewModel) {
    onDispose {
      context.hideKeyboard()
      appWidgetUpdater.updateScheduleWidget()
    }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is EditGoogleTaskEvent.ShowDatePicker -> {
        dateTimePickerProvider.showDatePicker(
          fragmentManager = activity.supportFragmentManager,
          date = event.date,
          title = context.getString(R.string.select_date),
        ) { viewModel.onDateSet(it) }
      }

      is EditGoogleTaskEvent.ShowTimePicker -> {
        dateTimePickerProvider.showTimePicker(
          fragmentManager = activity.supportFragmentManager,
          time = event.time,
          title = context.getString(R.string.select_time),
        ) { viewModel.onTimeSet(it) }
      }

      EditGoogleTaskEvent.Saved, EditGoogleTaskEvent.Deleted -> backStack.removeLastOrNull()
    }
  }
  viewModel.errorEvent.ObserveEvent { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }

  val state by viewModel.state.collectAsState()
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
  bindLifecycle(viewModel)
  val context = LocalContext.current
  val appWidgetUpdater = koinInject<AppWidgetUpdater>()
  DisposableEffect(viewModel) {
    onDispose {
      context.hideKeyboard()
      appWidgetUpdater.updateScheduleWidget()
    }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      EditGoogleTaskListEvent.Saved, EditGoogleTaskListEvent.Deleted -> backStack.removeLastOrNull()
    }
  }
  viewModel.errorEvent.ObserveEvent { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }

  val state by viewModel.state.collectAsState()
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

private fun withActivityCheck(
  activity: Activity,
  action: () -> Unit,
) {
  if (!SuperUtil.checkGooglePlayServicesAvailability(activity)) {
    Toast.makeText(activity, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show()
    return
  }
  action()
}

@Composable
private fun bindLifecycle(observer: DefaultLifecycleObserver) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(observer, lifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}

@Composable
private fun GoogleTaskAdBanner(adsProvider: AdsProvider) {
  if (BuildParams.isPro || !AdsProvider.hasAds()) return
  val context = LocalContext.current
  AndroidView(
    modifier = Modifier.fillMaxWidth(),
    factory = { FrameLayout(context) },
    update = { viewGroup -> adsProvider.showBanner(viewGroup, AdsProvider.GOOGLE_TASKS_PREVIEW_BANNER_ID) },
  )
}
