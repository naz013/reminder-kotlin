package com.github.naz013.feature.note.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.note.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.SelectionOverlay
import com.github.naz013.ui.common.compose.foundation.SelectionTopBar
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.CloudBubble
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.component.SearchBar
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.note.ListLayoutMode
import com.github.naz013.ui.note.NoteCard
import com.github.naz013.ui.note.UiNoteListItem
import com.github.naz013.ui.tag.TagFilterRow

private const val GRID_COLUMNS = 2
private val GRID_CARD_HEIGHT = 180.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotesScreen(
  modifier: Modifier = Modifier,
  state: NotesScreenState,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  onBackClick: (() -> Unit)?,
  onSearchQueryChange: (String) -> Unit,
  onSortOrderSelected: (String) -> Unit,
  onLayoutModeSelected: (ListLayoutMode) -> Unit,
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
    snackbarHost = { SnackbarHost(snackbarHostState) },
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
          layoutMode = state.layoutMode,
          onLayoutModeSelected = onLayoutModeSelected,
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
            layoutMode = state.layoutMode,
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
  layoutMode: ListLayoutMode,
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
  val listContentPadding = PaddingValues(
    start = 16.dp,
    end = 16.dp,
    top = contentPadding.calculateTopPadding() + 8.dp,
    bottom = contentPadding.calculateBottomPadding() + fabBottomPadding,
  )

  fun trailingContent(note: UiNoteListItem): @Composable BoxScope.() -> Unit = {
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
  }

  when (layoutMode) {
    ListLayoutMode.LIST -> {
      LazyColumn(
        modifier = modifier,
        contentPadding = listContentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(notes, key = { it.id }) { note ->
          NoteCard(
            note = note,
            onClick = { onNoteClick(note.id) },
            onLongClick = { onNoteLongClick(note.id) },
            onImageClick = { imageId -> if (isSelectionMode) onNoteClick(note.id) else onImageClick(note, imageId) },
            modifier = Modifier.animateItem(),
            trailingContent = trailingContent(note),
          )
        }
      }
    }

    ListLayoutMode.GRID -> {
      LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        modifier = modifier,
        contentPadding = listContentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(notes, key = { it.id }) { note ->
          NoteCard(
            note = note,
            onClick = { onNoteClick(note.id) },
            onLongClick = { onNoteLongClick(note.id) },
            onImageClick = { imageId -> if (isSelectionMode) onNoteClick(note.id) else onImageClick(note, imageId) },
            modifier = Modifier.animateItem().height(GRID_CARD_HEIGHT),
            trailingContent = trailingContent(note),
          )
        }
      }
    }

    ListLayoutMode.STAGGERED_GRID -> {
      LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(GRID_COLUMNS),
        modifier = modifier,
        contentPadding = listContentPadding,
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
            trailingContent = trailingContent(note),
          )
        }
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
  layoutMode: ListLayoutMode,
  onLayoutModeSelected: (ListLayoutMode) -> Unit,
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
      LayoutModeToggleButton(currentMode = layoutMode, onModeSelected = onLayoutModeSelected)
      if (onArchiveClick != null || onSettingsClick != null) {
        OverflowMenuButton(onArchiveClick = onArchiveClick, onSettingsClick = onSettingsClick)
      }
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

/**
 * Selection popup shared by the sort-order and layout-mode toggle buttons: an icon button that
 * opens a [CloudBubble] (the same speech-bubble used by the calendar view-mode switcher) listing
 * every option with the active one highlighted.
 */
@Composable
private fun <T> SelectableOptionsButton(
  icon: Painter,
  contentDescription: String,
  currentValue: T,
  options: List<Triple<T, Painter, String>>,
  onValueSelected: (T) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    MenuIconButton(
      icon = icon,
      contentDescription = contentDescription,
      onClick = { expanded = true },
    )
    if (expanded) {
      val containerColor = MaterialTheme.colorScheme.surfaceContainer
      val contentColor = MaterialTheme.colorScheme.onSurface
      CloudBubble(
        onDismissRequest = { expanded = false },
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = Modifier.width(200.dp),
      ) {
        Column {
          options.forEach { (value, optionIcon, label) ->
            SelectableOptionRow(
              icon = optionIcon,
              label = label,
              selected = value == currentValue,
              contentColor = contentColor,
              onClick = {
                expanded = false
                onValueSelected(value)
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SelectableOptionRow(
  icon: Painter,
  label: String,
  selected: Boolean,
  contentColor: Color,
  onClick: () -> Unit,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.small)
      .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 12.dp),
  ) {
    val rowContentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else contentColor
    Icon(
      painter = icon,
      contentDescription = null,
      tint = rowContentColor,
      modifier = Modifier.size(20.dp),
    )
    Text(
      text = label,
      color = rowContentColor,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
      modifier = Modifier.weight(1f),
    )
    if (selected) {
      Icon(
        painter = painterResource(R.drawable.ic_fluent_checkmark),
        contentDescription = null,
        tint = rowContentColor,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

@Composable
private fun SortMenuButton(
  sortOrder: String,
  onSortOrderSelected: (String) -> Unit,
) {
  val options = listOf(
    Triple(NoteSortProcessor.DATE_AZ, painterResource(sortOrderIconRes(NoteSortProcessor.DATE_AZ)), stringResource(R.string.by_date_az)),
    Triple(NoteSortProcessor.DATE_ZA, painterResource(sortOrderIconRes(NoteSortProcessor.DATE_ZA)), stringResource(R.string.by_date_za)),
    Triple(NoteSortProcessor.TEXT_AZ, painterResource(sortOrderIconRes(NoteSortProcessor.TEXT_AZ)), stringResource(R.string.name_az)),
    Triple(NoteSortProcessor.TEXT_ZA, painterResource(sortOrderIconRes(NoteSortProcessor.TEXT_ZA)), stringResource(R.string.name_za)),
  )
  SelectableOptionsButton(
    icon = sortOrderIcon(sortOrder),
    contentDescription = stringResource(R.string.order),
    currentValue = sortOrder,
    options = options,
    onValueSelected = onSortOrderSelected,
  )
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

@Composable
private fun LayoutModeToggleButton(
  currentMode: ListLayoutMode,
  onModeSelected: (ListLayoutMode) -> Unit,
) {
  val options = ListLayoutMode.entries.map { mode ->
    Triple(mode, layoutModeIcon(mode), stringResource(layoutModeLabelRes(mode)))
  }
  SelectableOptionsButton(
    icon = layoutModeIcon(currentMode),
    contentDescription = stringResource(R.string.notes_switch_layout),
    currentValue = currentMode,
    options = options,
    onValueSelected = onModeSelected,
  )
}

private fun layoutModeLabelRes(mode: ListLayoutMode): Int =
  when (mode) {
    ListLayoutMode.LIST -> R.string.list_view
    ListLayoutMode.GRID -> R.string.grid_view
    ListLayoutMode.STAGGERED_GRID -> R.string.staggered_grid_view
  }

@Composable
private fun layoutModeIcon(mode: ListLayoutMode): Painter =
  when (mode) {
    ListLayoutMode.LIST -> AppIcons.Fluent.List
    ListLayoutMode.GRID -> AppIcons.Fluent.Grid
    ListLayoutMode.STAGGERED_GRID -> AppIcons.Fluent.GridStaggered
  }

private data class OverflowAction(
  val id: Int,
  val title: String,
  val iconRes: Int,
  val onClick: () -> Unit,
)

@Composable
private fun OverflowMenuButton(
  onArchiveClick: (() -> Unit)?,
  onSettingsClick: (() -> Unit)?,
) {
  var expanded by remember { mutableStateOf(false) }
  val actions = buildList {
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
      onLayoutModeSelected = {},
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
