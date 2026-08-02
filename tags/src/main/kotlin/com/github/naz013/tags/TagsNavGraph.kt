package com.github.naz013.tags

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.tags.compose.ObserveEvent
import com.github.naz013.tags.compose.TagEditScreen
import com.github.naz013.tags.compose.TagEditState
import com.github.naz013.tags.compose.TagEditViewModel
import com.github.naz013.tags.compose.TagsScreen
import com.github.naz013.tags.compose.TagsScreenState
import com.github.naz013.tags.compose.TagsViewModel
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.tagsEntries(backStack: MutableList<NavKey>) {
  entry<TagsNavKey.Manage> { TagsManageEntry(backStack) }
  entry<TagsNavKey.Edit> { key -> TagsEditEntry(key, backStack) }
}

@Composable
private fun TagsManageEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<TagsViewModel>()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is TagsViewModel.NavigationEvent.OpenEdit -> backStack.add(TagsNavKey.Edit(event.id))
    }
  }

  val state by viewModel.state.collectAsState(TagsScreenState())
  TagsScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onAddClick = viewModel::onAddClick,
    onTagClick = viewModel::onTagClick
  )
}

@Composable
private fun TagsEditEntry(
  key: TagsNavKey.Edit,
  backStack: MutableList<NavKey>
) {
  val viewModel = koinViewModel<TagEditViewModel> { parametersOf(key.id) }
  val dialogDispatcher = rememberDialogDispatcher()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      TagEditViewModel.NavigationEvent.Back -> backStack.removeLastOrNull()
    }
  }

  val state by viewModel.state.collectAsState(TagEditState())
  TagEditScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onNameChange = viewModel::onNameChanged,
    onColorSelected = viewModel::onColorSelected,
    onSaveClick = viewModel::onSaveClick,
    onDeleteClick = {
      dialogDispatcher.showDialog(
        textRes = R.string.delete_tag_permanently,
        positiveButtonRes = R.string.yes,
        negativeButtonRes = R.string.cancel,
        onPositive = viewModel::onDeleteClick
      )
    }
  )
}
