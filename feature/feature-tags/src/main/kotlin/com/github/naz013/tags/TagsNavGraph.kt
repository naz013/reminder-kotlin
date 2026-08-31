package com.github.naz013.tags

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
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
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.dialog.rememberColorPickerDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.navigation.DetailPanePlaceholder
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.tagsEntries(
  backStack: MutableList<NavKey>,
  isRenderedAsDetailPane: (NavKey) -> Boolean,
  adsContent: @Composable () -> Unit = {},
  onReminderPreviewClick: (String) -> Unit = {},
  onNotePreviewClick: (String) -> Unit = {},
  onBirthdayPreviewClick: (String) -> Unit = {},
  onGoogleTaskPreviewClick: (String) -> Unit = {},
) {
  entry<TagsNavKey.Manage>(
    metadata = ListDetailSceneStrategy.listPane(
      detailPlaceholder = {
        DetailPanePlaceholder(
          text = stringResource(R.string.select_tag_to_see_details),
          icon = AppIcons.Builder.Tag,
        )
      },
    ),
  ) { TagsManageEntry(backStack) }
  entry<TagsNavKey.Edit>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    TagsEditEntry(key, backStack, renderAsDetailPane, adsContent)
  }
  entry<TagsNavKey.Details>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
    // Fixed at first composition, not re-read on every recomposition - see the matching comment
    // in ReminderPreviewNavGraph.kt.
    val renderAsDetailPane = remember(key) { isRenderedAsDetailPane(key) }
    TagsDetailsEntry(
      key,
      backStack,
      renderAsDetailPane,
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

  val selectedItemId =
    backStack.lastOrNull()?.let { key ->
      when (key) {
        is TagsNavKey.Details -> key.id
        is TagsNavKey.Edit -> key.id
        else -> null
      }
    }
  LaunchedEffect(selectedItemId) { viewModel.onSelectedItemIdChanged(selectedItemId) }

  viewModel.navigationEvent.ObserveEvent { event ->
    when (event) {
      is TagsViewModel.NavigationEvent.OpenEdit -> {
        backStack.navigateToEditDetailPane(TagsNavKey.Edit(event.id)) {
          it is TagsNavKey.Details && it.id == event.id
        }
      }

      is TagsViewModel.NavigationEvent.OpenDetails -> {
        backStack.navigateToDetailPane(TagsNavKey.Details(event.id))
      }

      is TagsViewModel.NavigationEvent.ConfirmDelete -> {
        dialogDispatcher.showDialog(
          textRes = R.string.delete_tag_permanently,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.deleteTag(event.id) },
        )
      }

      is TagsViewModel.NavigationEvent.ConfirmDeleteSelected -> {
        dialogDispatcher.showDialog(
          title = event.title,
          positiveButtonRes = R.string.yes,
          negativeButtonRes = R.string.cancel,
          onPositive = { viewModel.deleteSelectedTags(event.ids) },
        )
      }
    }
  }

  val colorPickerDialogDispatcher = rememberColorPickerDialogDispatcher()
  val themeProvider = koinInject<ThemeProvider>()

  val state by viewModel.state.collectAsState(TagsScreenState())
  TagsScreen(
    state = state,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onAddClick = viewModel::onAddClick,
    onTagClick = viewModel::onTagClick,
    onTagLongClick = viewModel::onTagLongClick,
    onTagMenuAction = viewModel::onTagMenuAction,
    onSelectionCancel = viewModel::onSelectionCancel,
    onDeleteSelectedClick = viewModel::onDeleteSelectedClick,
    onChangeColorClick = {
      colorPickerDialogDispatcher.showDialog(
        titleRes = R.string.acc_select_color,
        colors = themeProvider.colorsForSliderThemed(),
        selectedIndex = 0,
        onColorSelected = viewModel::applySelectedColor,
      )
    },
  )
}

@Composable
private fun TagsEditEntry(
  key: TagsNavKey.Edit,
  backStack: MutableList<NavKey>,
  renderAsDetailPane: Boolean,
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
    renderAsDetailPane = renderAsDetailPane,
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
  renderAsDetailPane: Boolean,
  adsContent: @Composable () -> Unit,
  onReminderPreviewClick: (String) -> Unit,
  onNotePreviewClick: (String) -> Unit,
  onBirthdayPreviewClick: (String) -> Unit,
  onGoogleTaskPreviewClick: (String) -> Unit,
) {
  val viewModel = koinViewModel<TagDetailsViewModel> { parametersOf(key.id) }
  val dialogDispatcher = rememberDialogDispatcher()

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
    renderAsDetailPane = renderAsDetailPane,
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onEditClick = viewModel::onEditClick,
    onDeleteClick = viewModel::onDeleteClick,
    onSearchQueryChange = viewModel::onSearchQueryChange,
    onTypeSelected = viewModel::onTypeSelected,
    onItemClick = viewModel::onItemClick,
    adsContent = adsContent,
  )
}

/**
 * Navigation for the tags two-pane list's detail pane: if the current top entry is itself a tag
 * details or edit form, replace it instead of stacking another one on top. Mirrors
 * `BirthdaysNavGraph.kt`'s identically-purposed private helper - kept local here since
 * Manage/Edit/Details are all registered by this same graph.
 */
private fun MutableList<NavKey>.navigateToDetailPane(key: NavKey) {
  val top = lastOrNull()
  if (top is TagsNavKey.Details || top is TagsNavKey.Edit) {
    removeLastOrNull()
  }
  add(key)
}

/**
 * Navigation into an Edit screen from the tags detail pane: if the detail pane is currently
 * showing the Details of that very same tag ([isSameItemDetails] matches the top entry), push
 * Edit on top of it instead of replacing it - see the matching comment in `BirthdaysNavGraph.kt`.
 */
private fun MutableList<NavKey>.navigateToEditDetailPane(key: NavKey, isSameItemDetails: (NavKey) -> Boolean) {
  val top = lastOrNull()
  if (top != null && isSameItemDetails(top)) {
    add(key)
  } else {
    navigateToDetailPane(key)
  }
}
