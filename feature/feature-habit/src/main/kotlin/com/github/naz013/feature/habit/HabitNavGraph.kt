package com.github.naz013.feature.habit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.habit.list.HabitListScreen
import com.github.naz013.feature.habit.list.HabitListState
import com.github.naz013.feature.habit.list.HabitListViewModel
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel

/**
 * [onOpenHabitEditor] receives the habit id to edit, or null for a new habit. The create/edit
 * screen is registered by a later change; until then the host supplies a no-op.
 */
fun EntryProviderScope<NavKey>.habitEntries(
  backStack: MutableList<NavKey>,
  onOpenHabitEditor: (habitId: String?) -> Unit = {},
) {
  entry<HabitNavKey.List> { HabitListEntry(backStack, onOpenHabitEditor) }
}

@Composable
private fun HabitListEntry(
  backStack: MutableList<NavKey>,
  onOpenHabitEditor: (String?) -> Unit,
) {
  val viewModel = koinViewModel<HabitListViewModel>()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is HabitListViewModel.NavigationEvent.OpenEditor -> onOpenHabitEditor(event.id)
    }
  }

  val state by viewModel.state.collectAsState(HabitListState())
  HabitListScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onAddClick = viewModel::onAddClick,
    onHabitClick = viewModel::onHabitClick,
    onCheckInClick = viewModel::onCheckInClick,
  )
}
