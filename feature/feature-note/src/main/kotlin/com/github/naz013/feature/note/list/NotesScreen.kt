package com.github.naz013.feature.note.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.note.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.SelectionOverlay
import com.github.naz013.ui.common.compose.foundation.SelectionTopBar
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.component.SearchBar
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.note.NoteCard
import com.github.naz013.ui.note.UiNoteListItem
import com.github.naz013.ui.tag.TagFilterRow

private const val GRID_COLUMNS = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotesScreen(
  modifier: Modifier = Modifier,
  state: NotesScreenState,
  onBackClick: (() -> Unit)?,
  onSearchQueryChange: (String) -> Unit,
  onSortOrderSelected: (String) -> Unit,
  onGridToggleClick: () -> Unit,
  onArchiveClick: (() -> Unit)?,
  onSettingsClick: (() -> Unit)?,
  onAddClick: (() -> Unit)?,
  onNoteClick: (String) -> Unit,
  onNoteLongClick: (String) -> Unit,
  onNoteMenuAction: (UiNoteListItem, NoteMenuAction) -> Unit,
  onImageClick: (UiNoteListItem, Int) -> Unit,
  onTagSelected: (String?) -> Unit,
  onSelectionCancel: () -> Unit,
  onDeleteSelectedClick: () -> Unit,
  onArchiveSelectedClick: () -> Unit,
  onMergeSelectedClick: () -> Unit,
  onChangeColorClick: () -> Unit,
) {
  val isSelectionMode = state.selectedCount > 0

  BackHandler(enabled = isSelectionMode) { onSelectionCancel() }

  Scaffold(
    modifier = modifier,
    topBar = {
      if (isSelectionMode) {
        NotesSelectionTopBar(
          selectedCount = state.selectedCount,
          isArchived = state.isArchived,
          onCancelClick = onSelectionCancel,
          onDeleteClick = onDeleteSelectedClick,
          onArchiveClick = onArchiveSelectedClick,
          onMergeClick = onMergeSelectedClick,
          onChangeColorClick = onChangeColorClick,
        )
      } else {
        NotesTopBar(
          title = stringResource(if (state.isArchived) R.string.notes_archive else R.string.notes),
          onBackClick = onBackClick,
          isGrid = state.isGrid,
          onGridToggleClick = onGridToggleClick,
          sortOrder = state.sortOrder,
          onSortOrderSelected = onSortOrderSelected,
          onArchiveClick = onArchiveClick,
          onSettingsClick = onSettingsClick,
          onAddClick = onAddClick
        )
      }
    },
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
    ) {
      if (state.listState !is ListState.Empty || state.searchQuery.isNotEmpty()) {
        SearchBar(
          query = state.searchQuery,
          onQueryChange = onSearchQueryChange,
          placeholder = stringResource(R.string.search),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }

      if (state.listState !is ListState.Loading) {
        TagFilterRow(
          allTags = state.allTags,
          selectedTagId = state.selectedTagId,
          onTagSelected = onTagSelected,
          modifier = Modifier.padding(bottom = 8.dp),
        )
      }

      when (val listState = state.listState) {
        is ListState.Loading -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .weight(1f),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator()
          }
        }

        is ListState.Empty -> {
          NotesEmptyState(
            isArchived = state.isArchived,
            modifier = Modifier
              .fillMaxSize()
              .weight(1f),
          )
        }

        is ListState.Ready -> {
          NotesList(
            notes = listState.notes,
            isGrid = state.isGrid,
            isArchived = state.isArchived,
            isSelectionMode = isSelectionMode,
            contentPadding = PaddingValues(),
            hasFab = onAddClick != null,
            onNoteClick = onNoteClick,
            onNoteLongClick = onNoteLongClick,
            onNoteMenuAction = onNoteMenuAction,
            onImageClick = onImageClick,
            modifier = Modifier
              .fillMaxSize()
              .weight(1f),
          )
        }
      }
    }
  }
}

@Composable
private fun NotesList(
  modifier: Modifier = Modifier,
  notes: List<UiNoteListItem>,
  isGrid: Boolean,
  isArchived: Boolean,
  isSelectionMode: Boolean,
  contentPadding: PaddingValues,
  hasFab: Boolean,
  onNoteClick: (String) -> Unit,
  onNoteLongClick: (String) -> Unit,
  onNoteMenuAction: (UiNoteListItem, NoteMenuAction) -> Unit,
  onImageClick: (UiNoteListItem, Int) -> Unit,
) {
  val fabBottomPadding = if (hasFab) 88.dp else 0.dp
  if (!isGrid) {
    LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = contentPadding.calculateTopPadding() + 8.dp,
        bottom = contentPadding.calculateBottomPadding() + fabBottomPadding,
      ),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(notes, key = { it.id }) { note ->
        NoteCard(
          note = note,
          onClick = { onNoteClick(note.id) },
          onLongClick = { onNoteLongClick(note.id) },
          onImageClick = { imageId -> if (isSelectionMode) onNoteClick(note.id) else onImageClick(note, imageId) },
          modifier = Modifier.animateItem(),
          trailingContent = {
            SelectionOverlay(
              isSelectionMode = isSelectionMode,
              isSelected = note.isSelected,
              onToggleSelected = { onNoteClick(note.id) },
            ) {
              NoteOverflowMenu(
                isArchived = isArchived,
                isPinned = note.isPinned,
                textColor = note.textColor,
                onMenuAction = { action -> onNoteMenuAction(note, action) },
              )
            }
          },
        )
      }
    }
  } else {
    LazyVerticalStaggeredGrid(
      columns = StaggeredGridCells.Fixed(GRID_COLUMNS),
      modifier = modifier,
      contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = contentPadding.calculateTopPadding() + 8.dp,
        bottom = contentPadding.calculateBottomPadding() + fabBottomPadding,
      ),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalItemSpacing = 8.dp,
    ) {
      items(notes, key = { it.id }) { note ->
        NoteCard(
          note = note,
          onClick = { onNoteClick(note.id) },
          onLongClick = { onNoteLongClick(note.id) },
          onImageClick = { imageId -> if (isSelectionMode) onNoteClick(note.id) else onImageClick(note, imageId) },
          modifier = Modifier.animateItem(),
          trailingContent = {
            SelectionOverlay(
              isSelectionMode = isSelectionMode,
              isSelected = note.isSelected,
              onToggleSelected = { onNoteClick(note.id) },
            ) {
              NoteOverflowMenu(
                isArchived = isArchived,
                isPinned = note.isPinned,
                textColor = note.textColor,
                onMenuAction = { action -> onNoteMenuAction(note, action) },
              )
            }
          },
        )
      }
    }
  }
}

@Composable
private fun BoxScope.NoteOverflowMenu(
  isArchived: Boolean,
  isPinned: Boolean,
  textColor: androidx.compose.ui.graphics.Color,
  onMenuAction: (NoteMenuAction) -> Unit,
) {
  var menuExpanded by remember { mutableStateOf(false) }
  MenuIconButton(
    icon = painterResource(R.drawable.ic_fluent_more_vertical),
    iconColor = textColor,
    contentDescription = stringResource(R.string.more_options),
    onClick = { menuExpanded = true },
  )
  AppDropdownMenu(
    expanded = menuExpanded,
    onDismissRequest = { menuExpanded = false },
    items = noteMenuItems(isArchived, isPinned),
    onItemClick = { id -> onMenuAction(NoteMenuAction.entries[id]) },
  )
}

@Composable
private fun noteMenuItems(isArchived: Boolean, isPinned: Boolean): List<PopupMenuItem> {
  val pinAction = if (isPinned) NoteMenuAction.UNPIN to R.string.unpin else NoteMenuAction.PIN to R.string.pin
  val actions = if (isArchived) {
    listOf(
      NoteMenuAction.OPEN to R.string.open,
      NoteMenuAction.EDIT to R.string.edit,
      NoteMenuAction.UNARCHIVE to R.string.notes_unarchive,
      NoteMenuAction.DELETE to R.string.delete,
    )
  } else {
    listOf(
      NoteMenuAction.OPEN to R.string.open,
      NoteMenuAction.SHARE to R.string.share,
      NoteMenuAction.SHOW_IN_STATUS_BAR to R.string.show_note_in_notifications,
      NoteMenuAction.EDIT to R.string.edit,
      pinAction,
      NoteMenuAction.ARCHIVE to R.string.notes_move_to_archive,
      NoteMenuAction.DELETE to R.string.delete,
    )
  }
  return actions.map { [action, titleRes] ->
    PopupMenuItem(id = action.ordinal, title = stringResource(titleRes), iconRes = action.iconRes())
  }
}

private fun NoteMenuAction.iconRes(): Int =
  when (this) {
    NoteMenuAction.OPEN -> R.drawable.ic_fluent_open
    NoteMenuAction.EDIT -> R.drawable.ic_fluent_edit
    NoteMenuAction.SHARE -> R.drawable.ic_fluent_share
    NoteMenuAction.SHOW_IN_STATUS_BAR -> R.drawable.ic_fluent_alert
    NoteMenuAction.ARCHIVE, NoteMenuAction.UNARCHIVE -> R.drawable.ic_fluent_archive
    NoteMenuAction.PIN -> DrawableCatalog.Fluent.Pin
    NoteMenuAction.UNPIN -> DrawableCatalog.Fluent.PinOff
    NoteMenuAction.DELETE -> R.drawable.ic_fluent_delete
  }

@Composable
private fun NotesEmptyState(
  isArchived: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_fluent_note),
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    )
    Text(
      text = stringResource(
        if (isArchived) R.string.notes_archive_is_empty else R.string.no_notes,
      ),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesTopBar(
  title: String,
  onBackClick: (() -> Unit)?,
  isGrid: Boolean,
  onGridToggleClick: () -> Unit,
  sortOrder: String,
  onSortOrderSelected: (String) -> Unit,
  onArchiveClick: (() -> Unit)?,
  onSettingsClick: (() -> Unit)?,
  onAddClick: (() -> Unit)?,
) {
  TopAppBar(
    title = { Text(title) },
    navigationIcon = {
      if (onBackClick != null) {
        MenuIconButton(
          icon = AppIcons.Builder.ArrowLeft,
          contentDescription = null,
          onClick = onBackClick,
        )
      }
    },
    actions = {
      if (onAddClick != null) {
        MenuIconButton(
          icon = AppIcons.Fluent.Add,
          contentDescription = stringResource(R.string.acc_add),
          onClick = onAddClick,
          iconColor = MaterialTheme.colorScheme.primary,
        )
      }
      SortMenuButton(sortOrder = sortOrder, onSortOrderSelected = onSortOrderSelected)
      OverflowMenuButton(
        isGrid = isGrid,
        onGridToggleClick = onGridToggleClick,
        onArchiveClick = onArchiveClick,
        onSettingsClick = onSettingsClick
      )
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background,
    ),
  )
}

private enum class NotesSelectionAction { CHANGE_COLOR, MERGE, ARCHIVE, DELETE }

@Composable
private fun NotesSelectionTopBar(
  selectedCount: Int,
  isArchived: Boolean,
  onCancelClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onArchiveClick: () -> Unit,
  onMergeClick: () -> Unit,
  onChangeColorClick: () -> Unit,
) {
  SelectionTopBar(
    title = pluralStringResource(R.plurals.notes_selected_count, selectedCount, selectedCount),
    onCancelClick = onCancelClick,
    actions = notesSelectionMenuItems(isArchived, selectedCount),
    onActionClick = { id ->
      when (NotesSelectionAction.entries[id]) {
        NotesSelectionAction.CHANGE_COLOR -> onChangeColorClick()
        NotesSelectionAction.MERGE -> onMergeClick()
        NotesSelectionAction.ARCHIVE -> onArchiveClick()
        NotesSelectionAction.DELETE -> onDeleteClick()
      }
    },
  )
}

@Composable
private fun notesSelectionMenuItems(isArchived: Boolean, selectedCount: Int): List<PopupMenuItem> =
  buildList {
    if (!isArchived) {
      add(
        PopupMenuItem(
          id = NotesSelectionAction.CHANGE_COLOR.ordinal,
          title = stringResource(R.string.change_color),
          iconRes = R.drawable.ic_fluent_color_background,
        )
      )
    }
    if (selectedCount >= 2) {
      add(
        PopupMenuItem(
          id = NotesSelectionAction.MERGE.ordinal,
          title = stringResource(R.string.merge),
          iconRes = DrawableCatalog.Fluent.Merge,
        )
      )
    }
    add(
      PopupMenuItem(
        id = NotesSelectionAction.ARCHIVE.ordinal,
        title = stringResource(if (isArchived) R.string.notes_unarchive else R.string.notes_move_to_archive),
        iconRes = R.drawable.ic_fluent_archive,
      )
    )
    add(
      PopupMenuItem(
        id = NotesSelectionAction.DELETE.ordinal,
        title = stringResource(R.string.delete),
        iconRes = R.drawable.ic_fluent_delete,
      )
    )
  }

@Composable
private fun SortMenuButton(
  sortOrder: String,
  onSortOrderSelected: (String) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val items = listOf(
    NoteSortProcessor.DATE_AZ to stringResource(R.string.by_date_az),
    NoteSortProcessor.DATE_ZA to stringResource(R.string.by_date_za),
    NoteSortProcessor.TEXT_AZ to stringResource(R.string.name_az),
    NoteSortProcessor.TEXT_ZA to stringResource(R.string.name_za),
  )
  Box {
    MenuIconButton(
      icon = sortOrderIcon(sortOrder),
      contentDescription = stringResource(R.string.order),
      onClick = { expanded = true },
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = items.mapIndexed { index, [sortOrder, title] ->
        PopupMenuItem(id = index, title = title, iconRes = sortOrderIconRes(sortOrder))
      },
      onItemClick = { index -> onSortOrderSelected(items[index].first) },
    )
  }
}

private fun sortOrderIconRes(sortOrder: String): Int =
  when (sortOrder) {
    NoteSortProcessor.DATE_AZ -> DrawableCatalog.Fluent.ArrowUp
    NoteSortProcessor.TEXT_AZ -> DrawableCatalog.Fluent.TextSortAscending
    NoteSortProcessor.TEXT_ZA -> DrawableCatalog.Fluent.TextSortDescending
    else -> DrawableCatalog.Fluent.ArrowDown
  }

@Composable
private fun sortOrderIcon(sortOrder: String): Painter = painterResource(sortOrderIconRes(sortOrder))

private data class OverflowAction(
  val id: Int,
  val title: String,
  val iconRes: Int,
  val onClick: () -> Unit,
)

@Composable
private fun OverflowMenuButton(
  isGrid: Boolean,
  onGridToggleClick: () -> Unit,
  onArchiveClick: (() -> Unit)?,
  onSettingsClick: (() -> Unit)?,
) {
  var expanded by remember { mutableStateOf(false) }
  val actions = buildList {
    add(
      OverflowAction(
        id = 0,
        title = stringResource(if (!isGrid) R.string.grid_view else R.string.list_view),
        iconRes = if (!isGrid) R.drawable.ic_fluent_grid else R.drawable.ic_fluent_list,
        onClick = onGridToggleClick
      )
    )
    if (onArchiveClick != null) {
      add(OverflowAction(1, stringResource(R.string.notes_archive), R.drawable.ic_fluent_archive, onArchiveClick))
    }
    if (onSettingsClick != null) {
      add(OverflowAction(2, stringResource(R.string.action_settings), R.drawable.ic_fluent_settings, onSettingsClick))
    }
  }
  Box {
    MenuIconButton(
      icon = painterResource(R.drawable.ic_fluent_more_vertical),
      contentDescription = stringResource(R.string.more_options),
      onClick = { expanded = true },
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = actions.map { PopupMenuItem(id = it.id, title = it.title, iconRes = it.iconRes) },
      onItemClick = { id -> actions.firstOrNull { it.id == id }?.onClick?.invoke() },
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun NotesScreenEmptyPreview() {
  AppTheme {
    NotesScreen(
      state = NotesScreenState(listState = ListState.Empty),
      onBackClick = null,
      onSearchQueryChange = {},
      onSortOrderSelected = {},
      onGridToggleClick = {},
      onArchiveClick = {},
      onSettingsClick = {},
      onAddClick = {},
      onNoteClick = {},
      onNoteLongClick = {},
      onNoteMenuAction = { _, _ -> },
      onImageClick = { _, _ -> },
      onTagSelected = {},
      onSelectionCancel = {},
      onDeleteSelectedClick = {},
      onArchiveSelectedClick = {},
      onMergeSelectedClick = {},
      onChangeColorClick = {},
    )
  }
}
