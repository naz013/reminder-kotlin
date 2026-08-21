package com.github.naz013.feature.routine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.routine.edit.RoutineEditScreen
import com.github.naz013.feature.routine.edit.RoutineEditState
import com.github.naz013.feature.routine.edit.RoutineEditViewModel
import com.github.naz013.feature.routine.execution.RoutineExecutionScreen
import com.github.naz013.feature.routine.execution.RoutineExecutionState
import com.github.naz013.feature.routine.execution.RoutineExecutionViewModel
import com.github.naz013.feature.routine.list.RoutinesListScreen
import com.github.naz013.feature.routine.list.RoutinesListState
import com.github.naz013.feature.routine.list.RoutinesListViewModel
import com.github.naz013.feature.routine.preview.RoutinePreviewScreen
import com.github.naz013.feature.routine.preview.RoutinePreviewState
import com.github.naz013.feature.routine.preview.RoutinePreviewViewModel
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.routineEntries(
  backStack: MutableList<NavKey>,
  onManageTagsClick: () -> Unit = {},
) {
  entry<RoutineNavKey.List> { RoutinesListEntry(backStack) }
  entry<RoutineNavKey.Edit> { key -> RoutineEditEntry(key, backStack, onManageTagsClick) }
  entry<RoutineNavKey.Preview> { key -> RoutinePreviewEntry(key, backStack) }
  entry<RoutineNavKey.Execute> { key -> RoutineExecutionEntry(key, backStack) }
}

@Composable
private fun RoutinesListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<RoutinesListViewModel>()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is RoutinesListViewModel.NavigationEvent.OpenEdit -> backStack.add(RoutineNavKey.Edit(event.id))
      is RoutinesListViewModel.NavigationEvent.OpenPreview -> backStack.add(RoutineNavKey.Preview(event.id))
      is RoutinesListViewModel.NavigationEvent.OpenExecute -> backStack.add(RoutineNavKey.Execute(event.id))
    }
  }

  val state by viewModel.state.collectAsState(RoutinesListState())
  RoutinesListScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onTagSelected = viewModel::onTagSelected,
    onSortOrderSelected = viewModel::onSortOrderSelected,
    onAddClick = viewModel::onAddClick,
    onRoutineClick = viewModel::onRoutineClick,
    onStartClick = viewModel::onStartClick,
  )
}

@Composable
private fun RoutineEditEntry(
  key: RoutineNavKey.Edit,
  backStack: MutableList<NavKey>,
  onManageTagsClick: () -> Unit,
) {
  val viewModel = koinViewModel<RoutineEditViewModel> { parametersOf(key.id) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      RoutineEditViewModel.NavigationEvent.Back -> if (backStack.size > 1) backStack.removeLastOrNull()
      RoutineEditViewModel.NavigationEvent.OpenManageTags -> onManageTagsClick()
    }
  }

  val state by viewModel.state.collectAsState(RoutineEditState())
  RoutineEditScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onTitleChange = viewModel::onTitleChange,
    onDescriptionChange = viewModel::onDescriptionChange,
    onColorSelected = viewModel::onColorSelected,
    onPinToggleClick = viewModel::onPinToggleClick,
    onRecurrenceOptionChange = viewModel::onRecurrenceOptionChange,
    onAddStepClick = viewModel::onAddStepClick,
    onStepTitleChange = viewModel::onStepTitleChange,
    onStepDurationSelected = viewModel::onStepDurationSelected,
    onStepTimeSelected = viewModel::onStepTimeSelected,
    onRemoveStepClick = viewModel::onRemoveStepClick,
    onMoveStepUp = viewModel::onMoveStepUp,
    onMoveStepDown = viewModel::onMoveStepDown,
    onTagToggle = viewModel::onTagToggle,
    onManageTagsClick = viewModel::onManageTagsClick,
    onSaveClick = viewModel::onSaveClick,
    onDeleteClick = viewModel::onDeleteClick,
  )
}

@Composable
private fun RoutinePreviewEntry(
  key: RoutineNavKey.Preview,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<RoutinePreviewViewModel> { parametersOf(key.id) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is RoutinePreviewViewModel.NavigationEvent.OpenEdit -> backStack.add(RoutineNavKey.Edit(event.id))
      is RoutinePreviewViewModel.NavigationEvent.OpenExecute -> backStack.add(RoutineNavKey.Execute(event.id))
      RoutinePreviewViewModel.NavigationEvent.Back -> if (backStack.size > 1) backStack.removeLastOrNull()
    }
  }

  val state by viewModel.state.collectAsState(RoutinePreviewState.Loading)
  RoutinePreviewScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onEditClick = viewModel::onEditClick,
    onPinToggleClick = viewModel::onPinToggleClick,
    onResetStepsClick = viewModel::onResetStepsClick,
    onDeleteClick = viewModel::onDeleteClick,
    onStepCheckToggle = viewModel::onStepCheckToggle,
    onStartClick = viewModel::onStartClick,
  )
}

@Composable
private fun RoutineExecutionEntry(
  key: RoutineNavKey.Execute,
  backStack: MutableList<NavKey>,
) {
  val viewModel = koinViewModel<RoutineExecutionViewModel> { parametersOf(key.id) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      RoutineExecutionViewModel.NavigationEvent.Back -> if (backStack.size > 1) backStack.removeLastOrNull()
    }
  }

  val hapticFeedback = LocalHapticFeedback.current
  viewModel.stepTransitionEvent.ObserveEvent {
    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
  }

  val state by viewModel.state.collectAsState(RoutineExecutionState.Loading)
  RoutineExecutionScreen(
    state = state,
    onBackClick = viewModel::onBackClick,
    onPlayPauseClick = viewModel::onPlayPauseClick,
    onAddMinuteClick = viewModel::onAddMinuteClick,
    onSkipClick = viewModel::onSkipClick,
    onPreviousStepClick = viewModel::onPreviousStepClick,
    onCompleteStepClick = viewModel::onCompleteStepClick,
  )
}
