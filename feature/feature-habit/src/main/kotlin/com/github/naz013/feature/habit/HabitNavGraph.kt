package com.github.naz013.feature.habit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.common.Permissions
import com.github.naz013.feature.habit.editor.HabitEditorScreen
import com.github.naz013.feature.habit.editor.HabitEditorViewModel
import com.github.naz013.feature.habit.list.HabitListScreen
import com.github.naz013.feature.habit.list.HabitListViewModel
import com.github.naz013.feature.habit.preview.HabitPreviewScreen
import com.github.naz013.feature.habit.preview.HabitPreviewViewModel
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.habitEntries(
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit = {},
) {
  entry<HabitNavKey.List> { HabitListEntry(backStack) }
  entry<HabitNavKey.Preview> { key -> HabitPreviewEntry(key, backStack, adsContent) }
  entry<HabitNavKey.Editor> { key -> HabitEditorEntry(key, backStack, adsContent) }
}

@Composable
private fun HabitListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<HabitListViewModel>()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      HabitListViewModel.NavigationEvent.Back -> backStack.popBackStack()
      is HabitListViewModel.NavigationEvent.OpenEditor -> backStack.add(HabitNavKey.Editor(event.id))
      is HabitListViewModel.NavigationEvent.OpenPreview -> backStack.add(HabitNavKey.Preview(event.id))
    }
  }

  val state by viewModel.state.collectAsState()
  HabitListScreen(
    state = state,
    onBackClick = viewModel::onBackClick,
    onAddClick = viewModel::onAddClick,
    onHabitClick = viewModel::onHabitClick,
    onCheckInClick = viewModel::onCheckInClick,
  )
}

@Composable
private fun HabitPreviewEntry(
  key: HabitNavKey.Preview,
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit,
) {
  val viewModel = koinViewModel<HabitPreviewViewModel> { parametersOf(key.habitId) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      HabitPreviewViewModel.NavigationEvent.Back -> backStack.popBackStack()
      is HabitPreviewViewModel.NavigationEvent.OpenEditor -> backStack.add(HabitNavKey.Editor(event.id))
    }
  }

  val state by viewModel.state.collectAsState()
  HabitPreviewScreen(
    state = state,
    onBackClick = viewModel::onBackClick,
    onEditClick = viewModel::onEditClick,
    onCheckInClick = viewModel::onCheckInClick,
    onDeleteClick = viewModel::onDeleteClick,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDeleteDismissed = viewModel::onDeleteDismissed,
    adsContent = adsContent,
  )
}

@Composable
private fun HabitEditorEntry(
  key: HabitNavKey.Editor,
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit,
) {
  val viewModel = koinViewModel<HabitEditorViewModel> { parametersOf(key.habitId) }

  val permissionRequester = rememberPermissionRequesterRationale()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      HabitEditorViewModel.NavigationEvent.Back -> backStack.popBackStack()
      HabitEditorViewModel.NavigationEvent.RequestNotificationPermission -> permissionRequester.request(
        Permissions.POST_NOTIFICATION,
        onGranted = viewModel::onNotificationPermissionGranted,
      )
    }
  }

  val state by viewModel.state.collectAsState()
  HabitEditorScreen(
    state = state,
    onBackClick = viewModel::onBackClick,
    onNameChange = viewModel::onNameChange,
    onDescriptionChange = viewModel::onDescriptionChange,
    onFrequencyChange = viewModel::onFrequencyChange,
    onTargetDecrease = viewModel::onTargetDecrease,
    onTargetIncrease = viewModel::onTargetIncrease,
    onColorSelected = viewModel::onColorSelected,
    onIconSelected = viewModel::onIconSelected,
    onReminderToggle = viewModel::onReminderToggle,
    onReminderTimeChange = viewModel::onReminderTimeChange,
    onRemindOnlyIfNotDoneChange = viewModel::onRemindOnlyIfNotDoneChange,
    onSaveClick = viewModel::onSaveClick,
    onDeleteClick = viewModel::onDeleteClick,
    onDeleteConfirmed = viewModel::onDeleteConfirmed,
    onDeleteDismissed = viewModel::onDeleteDismissed,
    adsContent = adsContent,
  )
}

/** Never pops the last entry, so a stray Back event can't leave the host with an empty stack. */
private fun MutableList<NavKey>.popBackStack() {
  if (size > 1) removeLastOrNull()
}
