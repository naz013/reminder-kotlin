package com.github.naz013.feature.routine

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
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
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.navigation.DetailPanePlaceholder
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.routineEntries(
  backStack: MutableList<NavKey>,
  isRenderedAsDetailPane: (NavKey) -> Boolean,
  adsContent: @Composable () -> Unit = {},
  onManageTagsClick: () -> Unit = {},
) {
  entry<RoutineNavKey.List>(
    metadata = ListDetailSceneStrategy.listPane(
      detailPlaceholder = {
        DetailPanePlaceholder(
          text = stringResource(R.string.select_routine_to_see_details),
          icon = AppIcons.Builder.Timer,
        )
      },
    ),
  ) { RoutinesListEntry(backStack) }
  entry<RoutineNavKey.Edit>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    RoutineEditEntry(key, backStack, renderAsDetailPane, onManageTagsClick)
  }
  entry<RoutineNavKey.Preview>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    RoutinePreviewEntry(key, backStack, renderAsDetailPane, adsContent)
  }
  entry<RoutineNavKey.Execute> { key -> RoutineExecutionEntry(key, backStack, adsContent) }
}

@Composable
private fun RoutinesListEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<RoutinesListViewModel>()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is RoutinesListViewModel.NavigationEvent.OpenEdit -> {
        backStack.navigateToDetailPane(RoutineNavKey.Edit(event.id))
      }

      is RoutinesListViewModel.NavigationEvent.OpenPreview -> {
        backStack.navigateToDetailPane(RoutineNavKey.Preview(event.id))
      }

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
  renderAsDetailPane: Boolean,
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
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onTitleChange = viewModel::onTitleChange,
    onDescriptionChange = viewModel::onDescriptionChange,
    onColorSelected = viewModel::onColorSelected,
    onIconSelected = viewModel::onIconSelected,
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
  renderAsDetailPane: Boolean,
  adsContent: @Composable () -> Unit,
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
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onEditClick = viewModel::onEditClick,
    onPinToggleClick = viewModel::onPinToggleClick,
    onResetStepsClick = viewModel::onResetStepsClick,
    onDeleteClick = viewModel::onDeleteClick,
    onStepCheckToggle = viewModel::onStepCheckToggle,
    onStartClick = viewModel::onStartClick,
    adsContent = adsContent,
  )
}

@Composable
private fun RoutineExecutionEntry(
  key: RoutineNavKey.Execute,
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit,
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
    adsContent = adsContent,
  )
}

/**
 * Navigation for the routines two-pane list's detail pane: if the current top entry is itself a
 * routine preview or edit form, replace it instead of stacking another one on top. Mirrors
 * `BirthdaysNavGraph.kt`'s identically-purposed private helper - kept local here since
 * List/Edit/Preview are all registered by this same graph.
 */
private fun MutableList<NavKey>.navigateToDetailPane(key: NavKey) {
  val top = lastOrNull()
  if (top is RoutineNavKey.Preview || top is RoutineNavKey.Edit) {
    removeLastOrNull()
  }
  add(key)
}
