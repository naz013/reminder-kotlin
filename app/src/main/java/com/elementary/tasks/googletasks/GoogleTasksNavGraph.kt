package com.elementary.tasks.googletasks

import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.fragment.findNavController
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.SuperUtil
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
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.Permissions
import com.github.naz013.ui.common.fragment.hideKeyboard
import com.github.naz013.ui.common.fragment.toast
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Builds the Google Tasks island's [NavDisplay] — the "screens" (Nav3 entries) themselves and
 * the routing between them. [GoogleTasksFragment] only owns the backstack and the
 * Android-framework glue (Google sign-in, date/time pickers, dialogs) that these entries react
 * to.
 */
@Composable
internal fun GoogleTasksFragment.GoogleTasksNavGraph(backStack: MutableList<NavKey>) {
  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryDecorators =
      listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
    transitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_ENTER_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_EXIT_SCALE)
      )
    },
    popTransitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
      )
    },
    predictivePopTransitionSpec = {
      (
        fadeIn(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleIn(animationSpec = navScreenSpring(), initialScale = NAV_ANIM_EXIT_SCALE)
      ) togetherWith (
        fadeOut(tween(NAV_ANIM_FADE_DURATION_MS)) +
          scaleOut(animationSpec = navScreenSpring(), targetScale = NAV_ANIM_ENTER_SCALE)
      )
    },
    entryProvider =
      entryProvider {
        entry<GoogleTasksNavKey.List> { GoogleTasksListEntry(backStack) }
        entry<GoogleTasksNavKey.TaskList> { key -> TaskListEntry(key, backStack) }
        entry<GoogleTasksNavKey.TaskPreview> { key -> TaskPreviewEntry(key, backStack) }
        entry<GoogleTasksNavKey.TaskEdit> { key -> TaskEditEntry(key, backStack) }
        entry<GoogleTasksNavKey.ListEdit> { key -> ListEditEntry(key, backStack) }
      },
  )
}

private fun navScreenSpring() =
  spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
  )

private const val NAV_ANIM_FADE_DURATION_MS = 250
private const val NAV_ANIM_ENTER_SCALE = 0.92f
private const val NAV_ANIM_EXIT_SCALE = 1.08f

@Composable
private fun GoogleTasksFragment.GoogleTasksListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<GoogleTasksViewModel>()
  bindLifecycle(viewModel)
  DisposableEffect(viewModel) {
    activeGoogleTasksViewModel = viewModel
    onDispose {
      if (activeGoogleTasksViewModel === viewModel) activeGoogleTasksViewModel = null
    }
  }
  LaunchedEffect(Unit) {
    viewModel.updateLoginStatus(googleLogin.isGoogleTasksLogged)
    analyticsEventSender.send(ScreenUsedEvent(Screen.GOOGLE_TASKS_LIST))
  }

  val state by viewModel.state.collectAsState()
  GoogleTasksScreen(
    state = state,
    onBackClick = { findNavController().popBackStack() },
    onConnectClick = {
      permissionFlow.askPermission(Permissions.GET_ACCOUNTS) {
        withActivityCheck { googleLogin.loginTasks() }
      }
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
private fun GoogleTasksFragment.TaskListEntry(
  key: GoogleTasksNavKey.TaskList,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<TaskListViewModel> { parametersOf(key.listId) }
  bindLifecycle(viewModel)
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      TaskListEvent.Deleted -> backStack.removeLastOrNull()
    }
  }
  viewModel.errorEvent.ObserveEvent { toast(it) }

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
private fun GoogleTasksFragment.TaskPreviewEntry(
  key: GoogleTasksNavKey.TaskPreview,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<PreviewGoogleTaskViewModel> { parametersOf(key.id) }
  bindLifecycle(viewModel)
  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      PreviewGoogleTaskEvent.Deleted -> backStack.removeLastOrNull()
    }
  }
  viewModel.errorEvent.ObserveEvent { toast(it) }

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
private fun GoogleTasksFragment.TaskEditEntry(
  key: GoogleTasksNavKey.TaskEdit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<EditGoogleTaskViewModel> { parametersOf(key.id, key.listId) }
  bindLifecycle(viewModel)
  DisposableEffect(viewModel) {
    onDispose {
      hideKeyboard()
      appWidgetUpdater.updateScheduleWidget()
    }
  }
  LaunchedEffect(Unit) { viewModel.checkDeepLink(arguments) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is EditGoogleTaskEvent.ShowDatePicker -> {
        dateTimePickerProvider.showDatePicker(
          fragmentManager = childFragmentManager,
          date = event.date,
          title = getString(R.string.select_date),
        ) { viewModel.onDateSet(it) }
      }

      is EditGoogleTaskEvent.ShowTimePicker -> {
        dateTimePickerProvider.showTimePicker(
          fragmentManager = childFragmentManager,
          time = event.time,
          title = getString(R.string.select_time),
        ) { viewModel.onTimeSet(it) }
      }

      EditGoogleTaskEvent.Saved, EditGoogleTaskEvent.Deleted -> backStack.removeLastOrNull()
    }
  }
  viewModel.errorEvent.ObserveEvent { toast(it) }

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
private fun GoogleTasksFragment.ListEditEntry(
  key: GoogleTasksNavKey.ListEdit,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<EditGoogleTaskListViewModel> { parametersOf(key.id) }
  bindLifecycle(viewModel)
  DisposableEffect(viewModel) {
    onDispose {
      hideKeyboard()
      appWidgetUpdater.updateScheduleWidget()
    }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      EditGoogleTaskListEvent.Saved, EditGoogleTaskListEvent.Deleted -> backStack.removeLastOrNull()
    }
  }
  viewModel.errorEvent.ObserveEvent { toast(it) }

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

private fun GoogleTasksFragment.withActivityCheck(action: () -> Unit) {
  val act = activity ?: return
  if (!SuperUtil.checkGooglePlayServicesAvailability(act)) {
    Toast.makeText(act, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show()
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
private fun GoogleTasksFragment.GoogleTaskAdBanner(adsProvider: AdsProvider) {
  if (BuildParams.isPro || !AdsProvider.hasAds()) return
  val context = LocalContext.current
  AndroidView(
    modifier = Modifier.fillMaxWidth(),
    factory = { FrameLayout(context) },
    update = { viewGroup -> adsProvider.showBanner(viewGroup, AdsProvider.GOOGLE_TASKS_PREVIEW_BANNER_ID) },
  )
}
