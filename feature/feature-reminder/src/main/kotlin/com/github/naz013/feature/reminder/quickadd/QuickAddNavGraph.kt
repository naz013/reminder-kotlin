package com.github.naz013.feature.reminder.quickadd

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.reminder.build.BuildReminderNavKey
import com.github.naz013.ui.common.compose.foundation.navigation.quickAddOverlay
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.quickAddEntries(backStack: MutableList<NavKey>) {
  entry<QuickAddNavKey.Sheet>(metadata = quickAddOverlay()) { SheetEntry(backStack) }
}

@Composable
private fun SheetEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<QuickAddViewModel>()
  val state by viewModel.state.collectAsState()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is QuickAddViewModel.ViewModelEvent.OpenFullEditor -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
        backStack.add(
          BuildReminderNavKey.Main(
            deepLinkText = event.text.ifBlank { null },
            deepLinkQuickAdd = event.quickAddDeepLink,
          ),
        )
      }

      QuickAddViewModel.ViewModelEvent.Dismiss -> {
        if (backStack.size > 1) backStack.removeLastOrNull()
      }
    }
  }

  QuickAddScreen(
    input = state.input,
    matchedRanges = state.matchedRanges,
    canSave = state.canSave,
    preview = state.preview,
    onInputChange = viewModel::onInputChanged,
    onDateSelected = viewModel::onDateSelected,
    onTimeSelected = viewModel::onTimeSelected,
    onRepeatOptionSelected = viewModel::onRepeatOptionSelected,
    onSaveClick = viewModel::onSaveClick,
    onEditManuallyClick = viewModel::onEditManuallyClick,
    onDismissRequest = { if (backStack.size > 1) backStack.removeLastOrNull() },
  )
}
