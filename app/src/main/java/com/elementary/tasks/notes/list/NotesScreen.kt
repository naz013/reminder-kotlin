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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem

private const val GRID_COLUMNS = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
  state: NotesScreenState,
  onBackClick: (() -> Unit)?,
  onSearchQueryChange: (String) -> Unit,
  onSortOrderSelected: (String) -> Unit,
  onGridToggleClick: () -> Unit,
  onArchiveClick: (() -> Unit)?,
  onSettingsClick: (() -> Unit)?,
  onAddClick: () -> Unit,
  onNoteClick: (String) -> Unit,
  onNoteMenuAction: (UiNoteListItem, NoteMenuAction) -> Unit,
  onImageClick: (UiNoteListItem, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchExpanded by rememberSaveable { mutableStateOf(false) }

  Scaffold(
    modifier = modifier,
    topBar = {
      NotesTopBar(
        title = stringResource(if (state.isArchived) R.string.notes_archive else R.string.notes),
        searchQuery = state.searchQuery,
        searchExpanded = searchExpanded,
        onSearchExpandedChange = { searchExpanded = it },
        onSearchQueryChange = onSearchQueryChange,
        onBackClick = onBackClick,
        isGrid = state.isGrid,
        onGridToggleClick = onGridToggleClick,
        sortOrder = state.sortOrder,
        onSortOrderSelected = onSortOrderSelected,
        onArchiveClick = onArchiveClick,
        onSettingsClick = onSettingsClick
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddClick) {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_add),
          contentDescription = stringResource(R.string.add_note)
        )
      }
    }
  ) { padding ->
    when (val listState = state.listState) {
      is ListState.Loading -> Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator()
      }

      is ListState.Empty -> NotesEmptyState(
        isArchived = state.isArchived,
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
      )

      is ListState.Ready -> NotesList(
        notes = listState.notes,
        isGrid = state.isGrid,
        isArchived = state.isArchived,
        contentPadding = padding,
        onNoteClick = onNoteClick,
        onNoteMenuAction = onNoteMenuAction,
        onImageClick = onImageClick,
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}

@Composable
private fun NotesList(
  notes: List<UiNoteListItem>,
  isGrid: Boolean,
  isArchived: Boolean,
  contentPadding: PaddingValues,
  onNoteClick: (String) -> Unit,
  onNoteMenuAction: (UiNoteListItem, NoteMenuAction) -> Unit,
  onImageClick: (UiNoteListItem, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  if (isGrid) {
    LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = contentPadding.calculateTopPadding() + 8.dp,
        bottom = contentPadding.calculateBottomPadding() + 88.dp
      ),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(notes, key = { it.id }) { note ->
        NoteCard(
          note = note,
          isArchived = isArchived,
          onClick = { onNoteClick(note.id) },
          onMenuAction = { action -> onNoteMenuAction(note, action) },
          onImageClick = { imageId -> onImageClick(note, imageId) }
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
        bottom = contentPadding.calculateBottomPadding() + 88.dp
      ),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalItemSpacing = 8.dp
    ) {
      items(notes, key = { it.id }) { note ->
        NoteCard(
          note = note,
          isArchived = isArchived,
          onClick = { onNoteClick(note.id) },
          onMenuAction = { action -> onNoteMenuAction(note, action) },
          onImageClick = { imageId -> onImageClick(note, imageId) }
        )
      }
    }
  }
}

@Composable
private fun NotesEmptyState(
  isArchived: Boolean,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_fluent_note),
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    )
    Text(
      text = stringResource(
        if (isArchived) R.string.notes_archive_is_empty else R.string.no_notes
      ),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp)
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesTopBar(
  title: String,
  searchQuery: String,
  searchExpanded: Boolean,
  onSearchExpandedChange: (Boolean) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onBackClick: (() -> Unit)?,
  isGrid: Boolean,
  onGridToggleClick: () -> Unit,
  sortOrder: String,
  onSortOrderSelected: (String) -> Unit,
  onArchiveClick: (() -> Unit)?,
  onSettingsClick: (() -> Unit)?
) {
  TopAppBar(
    title = {
      if (searchExpanded) {
        val keyboardController = LocalSoftwareKeyboardController.current
        TextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          placeholder = { Text(stringResource(R.string.search)) },
          colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
          ),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
        )
      } else {
        Text(title)
      }
    },
    navigationIcon = {
      if (onBackClick != null) {
        MenuIconButton(
          icon = painterResource(R.drawable.ic_builder_arrow_left),
          contentDescription = null,
          onClick = onBackClick
        )
      }
    },
    actions = {
      if (searchExpanded) {
        MenuIconButton(
          icon = painterResource(R.drawable.ic_fluent_dismiss),
          contentDescription = stringResource(R.string.cancel),
          onClick = {
            onSearchQueryChange("")
            onSearchExpandedChange(false)
          }
        )
      } else {
        MenuIconButton(
          icon = painterResource(R.drawable.ic_fluent_search),
          contentDescription = stringResource(R.string.search),
          onClick = { onSearchExpandedChange(true) }
        )
        MenuIconButton(
          icon = painterResource(if (isGrid) R.drawable.ic_fluent_grid else R.drawable.ic_fluent_list),
          contentDescription = stringResource(if (isGrid) R.string.grid_view else R.string.list_view),
          onClick = onGridToggleClick
        )
        SortMenuButton(sortOrder = sortOrder, onSortOrderSelected = onSortOrderSelected)
        if (onArchiveClick != null || onSettingsClick != null) {
          OverflowMenuButton(onArchiveClick = onArchiveClick, onSettingsClick = onSettingsClick)
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background
    )
  )
}

@Composable
private fun SortMenuButton(
  sortOrder: String,
  onSortOrderSelected: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val items = listOf(
    NoteSortProcessor.DATE_AZ to stringResource(R.string.by_date_az),
    NoteSortProcessor.DATE_ZA to stringResource(R.string.by_date_za),
    NoteSortProcessor.TEXT_AZ to stringResource(R.string.name_az),
    NoteSortProcessor.TEXT_ZA to stringResource(R.string.name_za)
  )
  Box {
    MenuIconButton(
      icon = painterResource(R.drawable.ic_fluent_arrow_sort),
      contentDescription = stringResource(R.string.order),
      onClick = { expanded = true }
    )
    NoteDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = items.mapIndexed { index, (_, title) -> PopupMenuItem(id = index, title = title) },
      onItemClick = { index -> onSortOrderSelected(items[index].first) }
    )
  }
}

private data class OverflowAction(val id: Int, val title: String, val iconRes: Int, val onClick: () -> Unit)

@Composable
private fun OverflowMenuButton(
  onArchiveClick: (() -> Unit)?,
  onSettingsClick: (() -> Unit)?
) {
  var expanded by remember { mutableStateOf(false) }
  val actions = buildList {
    if (onArchiveClick != null) {
      add(OverflowAction(0, stringResource(R.string.notes_archive), R.drawable.ic_fluent_archive, onArchiveClick))
    }
    if (onSettingsClick != null) {
      add(OverflowAction(1, stringResource(R.string.action_settings), R.drawable.ic_fluent_settings, onSettingsClick))
    }
  }
  Box {
    MenuIconButton(
      icon = painterResource(R.drawable.ic_fluent_more_vertical),
      contentDescription = stringResource(R.string.more_options),
      onClick = { expanded = true }
    )
    NoteDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = actions.map { PopupMenuItem(id = it.id, title = it.title, iconRes = it.iconRes) },
      onItemClick = { id -> actions.firstOrNull { it.id == id }?.onClick?.invoke() }
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
      onImageClick = { _, _ -> }
    )
  }
}
