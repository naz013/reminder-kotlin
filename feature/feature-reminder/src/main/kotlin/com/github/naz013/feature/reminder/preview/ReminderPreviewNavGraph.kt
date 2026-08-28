package com.github.naz013.feature.reminder.preview

import android.content.Intent
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.ui.common.compose.foundation.dialog.rememberListDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.github.naz013.common.Permissions
import java.io.File
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.reminderPreviewEntries(
  backStack: MutableList<NavKey>,
  isRenderedAsDetailPane: (NavKey) -> Boolean,
  navigateBeyondBackStack: (List<NavKey>) -> Unit,
  adsContent: @Composable () -> Unit,
  onShareFile: (title: String?, file: File) -> Unit,
  onOpenIntent: (intent: Intent, title: String) -> Unit,
  onOpenNote: (noteId: String) -> Unit,
  onOpenGoogleTask: (taskId: String) -> Unit,
  onOpenWorkflowRules: (reminderId: String) -> Unit,
  onCallClick: (String) -> Unit,
  onSmsClick: (target: String, message: String) -> Unit,
  onAppClick: (String) -> Unit,
  onUrlClick: (String) -> Unit,
) {
  entry<ReminderPreviewNavKey.Preview>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
    // Fixed at first composition of this entry, not re-read on every recomposition: once the
    // entry is popped (key.id no longer on the backstack), isRenderedAsDetailPane(key) would
    // otherwise flip to false while the pop's exit transition is still animating out, flashing
    // the back arrow before the screen finishes closing.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    PreviewEntry(
      key,
      backStack,
      renderAsDetailPane,
      navigateBeyondBackStack,
      adsContent,
      onShareFile,
      onOpenIntent,
      onOpenNote,
      onOpenGoogleTask,
      onOpenWorkflowRules,
      onCallClick,
      onSmsClick,
      onAppClick,
      onUrlClick,
    )
  }
  entry<ReminderPreviewNavKey.FullscreenMap> { key -> FullscreenMapEntry(key, backStack) }
}

@Composable
private fun PreviewEntry(
  key: ReminderPreviewNavKey.Preview,
  backStack: MutableList<NavKey>,
  renderAsDetailPane: Boolean,
  navigateBeyondBackStack: (List<NavKey>) -> Unit,
  adsContent: @Composable () -> Unit,
  onShareFile: (title: String?, file: File) -> Unit,
  onOpenIntent: (intent: Intent, title: String) -> Unit,
  onOpenNote: (noteId: String) -> Unit,
  onOpenGoogleTask: (taskId: String) -> Unit,
  onOpenWorkflowRules: (reminderId: String) -> Unit,
  onCallClick: (String) -> Unit,
  onSmsClick: (target: String, message: String) -> Unit,
  onAppClick: (String) -> Unit,
  onUrlClick: (String) -> Unit,
) {
  val viewModel = koinViewModel<PreviewReminderViewModel> { parametersOf(key.id) }

  val listDialogDispatcher = rememberListDialogDispatcher()
  val toastDispatcher = rememberToastDispatcher()
  val permissionRequester = rememberPermissionRequesterRationale()

  // ReminderActionActivity (the full-screen alarm popup) is a separate Activity launched on
  // top of this screen, so the composable is never disposed and the state flow's
  // WhileSubscribed subscription never restarts. Reload explicitly on every ON_RESUME so
  // snoozing/completing/deleting from the popup is reflected when we come back.
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(viewModel, lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          viewModel.refresh()
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  viewModel.event.ObserveEvent { event ->
    when (event) {
      PreviewReminderViewModel.ViewModelEvent.MoveBack -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
      }

      is PreviewReminderViewModel.ViewModelEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }

      is PreviewReminderViewModel.ViewModelEvent.ShareData -> {
        onShareFile(event.title, event.file)
      }

      is PreviewReminderViewModel.ViewModelEvent.OpenCalendar -> {
        onOpenIntent(event.intent, event.title)
      }

      is PreviewReminderViewModel.ViewModelEvent.ShowCopyTimeDialog -> {
        listDialogDispatcher.showDialog(
          titleRes = R.string.choose_time,
          items = event.titles,
          onItemClick = { which -> viewModel.copyReminder(event.times[which]) },
        )
      }

      is PreviewReminderViewModel.ViewModelEvent.MakeCall -> {
        permissionRequester.request(Permissions.CALL_PHONE, onGranted = { onCallClick(event.target) })
      }

      is PreviewReminderViewModel.ViewModelEvent.SendSms -> {
        onSmsClick(event.target, event.message)
      }

      is PreviewReminderViewModel.ViewModelEvent.OpenApp -> {
        onAppClick(event.target)
      }

      is PreviewReminderViewModel.ViewModelEvent.OpenLink -> {
        onUrlClick(event.target)
      }
    }
  }

  val state by viewModel.state.collectAsState(PreviewReminderState())
  PreviewReminderScreen(
    state = state,
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onToggleClick = viewModel::onToggleClick,
    onEditClick = { navigateBeyondBackStack(listOf(BuildReminderNavKey.Main(id = key.id))) },
    onShareClick = viewModel::shareReminder,
    onCopyClick = viewModel::onCopyClicked,
    onPinClick = viewModel::onPinToggleClick,
    onSyncToCloudClick = viewModel::onSyncToCloudClick,
    onWorkflowRulesClick = { onOpenWorkflowRules(key.id) },
    onDeleteClick = viewModel::onDeleteClick,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDeleteDismiss = viewModel::onDeleteDismiss,
    onSubTaskCheck = { viewModel.onSubTaskChecked(it) },
    onSubTaskRemove = { viewModel.onSubTaskRemoved(it) },
    onNoteClick = {
      val noteId = state.note?.id
      if (noteId != null) onOpenNote(noteId)
    },
    onGoogleTaskClick = {
      val taskId = state.googleTask?.id
      if (taskId != null) onOpenGoogleTask(taskId)
    },
    onCalendarOpenClick = { viewModel.onOpenCalendarClicked(it.id) },
    onCalendarRemoveClick = { viewModel.deleteEvent(it) },
    onTargetActionClick = viewModel::onTargetActionClick,
    mapContent = {
      EmbeddedMap(state.places) { backStack.add(ReminderPreviewNavKey.FullscreenMap(key.id)) }
    },
    adsContent = adsContent,
  )
}
