package com.github.naz013.tags

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.tags.compose.TagEditScreen
import com.github.naz013.tags.compose.TagEditState
import com.github.naz013.tags.compose.TagEditViewModel
import com.github.naz013.tags.compose.TagsScreen
import com.github.naz013.tags.compose.TagsScreenState
import com.github.naz013.tags.compose.TagsViewModel
import com.github.naz013.tags.details.TagDetailsScreen
import com.github.naz013.tags.details.TagDetailsState
import com.github.naz013.tags.details.TagDetailsViewModel
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.tagsEntries(
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit = {},
  onReminderPreviewClick: (String) -> Unit = {},
  onNotePreviewClick: (String) -> Unit = {},
  onBirthdayPreviewClick: (String) -> Unit = {},
  onGoogleTaskPreviewClick: (String) -> Unit = {},
) {
  entry<TagsNavKey.Manage> { TagsManageEntry(backStack) }
  entry<TagsNavKey.Edit> { key -> TagsEditEntry(key, backStack, adsContent) }
  entry<TagsNavKey.Details> { key ->
    TagsDetailsEntry(
      key,
      backStack,
      adsContent,
      onReminderPreviewClick,
      onNotePreviewClick,
      onBirthdayPreviewClick,
      onGoogleTaskPreviewClick,
    )
  }
}

@Composable
private fun TagsManageEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<TagsViewModel>()
  val dialogDispatcher = rememberDialogDispatcher()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is TagsViewModel.NavigationEvent.OpenEdit -> backStack.add(TagsNavKey.Edit(event.id))
      is TagsViewModel.NavigationEvent.OpenDetails -> backStack.add(TagsNavKey.Details(event.id))
      is TagsViewModel.NavigationEvent.ConfirmDelete -> {
        dialogDispatcher.showDialog(
          textRes = R.string.delete_tag_permanently,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.deleteTag(event.id) },
        )
      }
    }
  }

  val state by viewModel.state.collectAsState(TagsScreenState())
  TagsScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onAddClick = viewModel::onAddClick,
    onTagClick = viewModel::onTagClick,
    onTagMenuAction = viewModel::onTagMenuAction,
  )
}

@Composable
private fun TagsEditEntry(
  key: TagsNavKey.Edit,
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit = {},
) {
  val viewModel = koinViewModel<TagEditViewModel> { parametersOf(key.id) }
  val dialogDispatcher = rememberDialogDispatcher()

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      TagEditViewModel.NavigationEvent.Back -> if (backStack.size > 1) backStack.removeLastOrNull()
    }
  }

  val state by viewModel.state.collectAsState(TagEditState())
  TagEditScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
    },
    adsContent = adsContent
  )
}

@Composable
private fun TagsDetailsEntry(
  key: TagsNavKey.Details,
  backStack: MutableList<NavKey>,
  adsContent: @Composable () -> Unit,
  onReminderPreviewClick: (String) -> Unit,
  onNotePreviewClick: (String) -> Unit,
  onBirthdayPreviewClick: (String) -> Unit,
  onGoogleTaskPreviewClick: (String) -> Unit,
) {
  val viewModel = koinViewModel<TagDetailsViewModel> { parametersOf(key.id) }
  val dialogDispatcher = rememberDialogDispatcher()

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(viewModel, lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          viewModel.refreshState()
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is TagDetailsViewModel.NavigationEvent.OpenEdit -> backStack.add(TagsNavKey.Edit(event.id))
      is TagDetailsViewModel.NavigationEvent.ConfirmDelete -> {
        dialogDispatcher.showDialog(
          textRes = R.string.delete_tag_permanently,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.onDeleteConfirmed() },
        )
      }

      TagDetailsViewModel.NavigationEvent.Deleted -> if (backStack.size > 1) backStack.removeLastOrNull()
      is TagDetailsViewModel.NavigationEvent.OpenReminderPreview -> onReminderPreviewClick(event.id)
      is TagDetailsViewModel.NavigationEvent.OpenNotePreview -> onNotePreviewClick(event.id)
      is TagDetailsViewModel.NavigationEvent.OpenBirthdayPreview -> onBirthdayPreviewClick(event.id)
      is TagDetailsViewModel.NavigationEvent.OpenGoogleTaskPreview -> onGoogleTaskPreviewClick(event.id)
    }
  }

  val state by viewModel.state.collectAsState(TagDetailsState())
  TagDetailsScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onEditClick = viewModel::onEditClick,
    onDeleteClick = viewModel::onDeleteClick,
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onTypeSelected = viewModel::onTypeSelected,
    onItemClick = viewModel::onItemClick,
    adsContent = adsContent,
  )
}
