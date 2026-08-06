package com.elementary.tasks.notes.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SortByAlpha
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.component.SearchBar

private const val GRID_COLUMNS = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
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
  onNoteMenuAction: (UiNoteListItem, NoteMenuAction) -> Unit,
  onImageClick: (UiNoteListItem, Int) -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
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
    },
  ) { padding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(padding),
    ) {
      if (state.listState !is ListState.Empty || state.searchQuery.isNotEmpty()) {
        SearchBar(
          query = state.searchQuery,
          onQueryChange = onSearchQueryChange,
          placeholder = stringResource(R.string.search),
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }

      when (val listState = state.listState) {
        is ListState.Loading -> {
          Box(
            modifier =
              Modifier
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
            modifier =
              Modifier
                .fillMaxSize()
                .weight(1f),
          )
        }

        is ListState.Ready -> {
          NotesList(
            notes = listState.notes,
            isGrid = state.isGrid,
            isArchived = state.isArchived,
            contentPadding = PaddingValues(),
            hasFab = onAddClick != null,
            onNoteClick = onNoteClick,
            onNoteMenuAction = onNoteMenuAction,
            onImageClick = onImageClick,
            modifier =
              Modifier
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
  notes: List<UiNoteListItem>,
  isGrid: Boolean,
  isArchived: Boolean,
  contentPadding: PaddingValues,
  hasFab: Boolean,
  onNoteClick: (String) -> Unit,
  onNoteMenuAction: (UiNoteListItem, NoteMenuAction) -> Unit,
  onImageClick: (UiNoteListItem, Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val fabBottomPadding = if (hasFab) 88.dp else 0.dp
  if (isGrid) {
    LazyColumn(
      modifier = modifier,
      contentPadding =
        PaddingValues(
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
          isArchived = isArchived,
          onClick = { onNoteClick(note.id) },
          onMenuAction = { action -> onNoteMenuAction(note, action) },
          onImageClick = { imageId -> onImageClick(note, imageId) },
          modifier = Modifier.animateItem(),
        )
      }
    }
  } else {
    LazyVerticalStaggeredGrid(
      columns = StaggeredGridCells.Fixed(GRID_COLUMNS),
      modifier = modifier,
      contentPadding =
        PaddingValues(
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
          isArchived = isArchived,
          onClick = { onNoteClick(note.id) },
          onMenuAction = { action -> onNoteMenuAction(note, action) },
          onImageClick = { imageId -> onImageClick(note, imageId) },
          modifier = Modifier.animateItem(),
        )
      }
    }
  }
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
      text =
        stringResource(
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
    colors =
      TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
      ),
  )
}

@Composable
private fun SortMenuButton(
  sortOrder: String,
  onSortOrderSelected: (String) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val items =
    listOf(
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
      items = items.mapIndexed { index, (_, title) -> PopupMenuItem(id = index, title = title) },
      onItemClick = { index -> onSortOrderSelected(items[index].first) },
    )
  }
}

private fun sortOrderIcon(sortOrder: String): ImageVector =
  when (sortOrder) {
    NoteSortProcessor.DATE_AZ -> Icons.Filled.ArrowUpward
    NoteSortProcessor.TEXT_AZ, NoteSortProcessor.TEXT_ZA -> Icons.Filled.SortByAlpha
    else -> Icons.Filled.ArrowDownward
  }

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
  val actions =
    buildList {
      add(
        OverflowAction(
          0,
          stringResource(if (isGrid) R.string.grid_view else R.string.list_view),
          if (isGrid) R.drawable.ic_fluent_grid else R.drawable.ic_fluent_list,
          onGridToggleClick
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
      onNoteMenuAction = { _, _ -> },
      onImageClick = { _, _ -> },
    )
  }
}
