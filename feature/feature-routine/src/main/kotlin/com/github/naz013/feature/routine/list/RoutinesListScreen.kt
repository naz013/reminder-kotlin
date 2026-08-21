package com.github.naz013.feature.routine.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.component.SearchBar
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.routine.RoutineCard
import com.github.naz013.ui.tag.TagChipRow
import com.github.naz013.ui.tag.TagFilterRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutinesListScreen(
  state: RoutinesListState,
  onBackClick: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onTagSelected: (String?) -> Unit,
  onSortOrderSelected: (RoutineSortOrder) -> Unit,
  onAddClick: () -> Unit,
  onRoutineClick: (String) -> Unit,
  onStartClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.routines)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        actions = {
          SortMenu(sortOrder = state.sortOrder, onSortOrderSelected = onSortOrderSelected)
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onAddClick,
        icon = { Icon(AppIcons.Fluent.Add, contentDescription = null) },
        text = { Text(stringResource(R.string.new_routine)) },
      )
    },
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      SearchBar(
        query = state.query,
        onQueryChange = onSearchQueryChange,
        placeholder = stringResource(R.string.search),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      )
      TagFilterRow(
        allTags = state.allTags,
        selectedTagId = state.selectedTagId,
        onTagSelected = onTagSelected,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
      )
      when (val listState = state.listState) {
        is RoutinesListDisplayState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }

        is RoutinesListDisplayState.Empty -> {
          RoutinesEmptyState(modifier = Modifier.fillMaxSize())
        }

        is RoutinesListDisplayState.Ready -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          ) {
            items(listState.routines, key = { it.id }) { routine ->
              val tags = state.tagsByRoutineId[routine.id].orEmpty()
              RoutineCard(
                routine = routine,
                startButtonLabel = stringResource(R.string.start_routine),
                onClick = { onRoutineClick(routine.id) },
                onStartClick = { onStartClick(routine.id) },
                tagsContent = if (tags.isEmpty()) null else ({ TagChipRow(tags = tags) }),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SortMenu(
  sortOrder: RoutineSortOrder,
  onSortOrderSelected: (RoutineSortOrder) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    MenuIconButton(
      icon = AppIcons.Fluent.ArrowSort,
      contentDescription = stringResource(R.string.sort),
      onClick = { expanded = true },
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = listOf(
        PopupMenuItem(
          id = RoutineSortOrder.CREATION_DATE.ordinal,
          title = stringResource(R.string.sort_by_date),
          iconRes = if (sortOrder == RoutineSortOrder.CREATION_DATE) DrawableCatalog.Fluent.Checkmark else null,
        ),
        PopupMenuItem(
          id = RoutineSortOrder.NAME.ordinal,
          title = stringResource(R.string.sort_by_name),
          iconRes = if (sortOrder == RoutineSortOrder.NAME) DrawableCatalog.Fluent.Checkmark else null,
        ),
      ),
      onItemClick = { id -> onSortOrderSelected(RoutineSortOrder.entries[id]) },
    )
  }
}

@Composable
private fun RoutinesEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
  ) {
    Icon(
      painter = AppIcons.Builder.Timer,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    )
    Text(
      text = stringResource(R.string.no_routines),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp),
    )
  }
}
