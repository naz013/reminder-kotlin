package com.github.naz013.feature.googletask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.AppPullToRefreshBox
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.googletask.GoogleTaskItemState
import com.github.naz013.ui.tag.TagFilterRow

private const val MENU_ITEM_EDIT = 0
private const val MENU_ITEM_DELETE = 1
private const val MENU_ITEM_CLEAR = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskListScreen(
  state: TaskListState,
  onBackClick: () -> Unit,
  onEditListClick: () -> Unit,
  onDeleteListClick: () -> Unit,
  onDeleteConfirmed: () -> Unit,
  onDeleteDismiss: () -> Unit,
  onClearCompletedClick: () -> Unit,
  onTaskClick: (String) -> Unit,
  onTaskToggle: (String) -> Unit,
  onAddTaskClick: () -> Unit,
  onRefresh: () -> Unit,
  onTagSelected: (String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(state.title) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        actions = {
          var menuExpanded by remember { mutableStateOf(false) }
          MenuIconButton(
            icon = painterResource(R.drawable.ic_fluent_more_vertical),
            contentDescription = stringResource(R.string.more_options),
            onClick = { menuExpanded = true },
          )
          AppDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            items = taskListMenuItems(canDelete = state.canDelete),
            onItemClick = { id ->
              menuExpanded = false
              when (id) {
                MENU_ITEM_EDIT -> onEditListClick()
                MENU_ITEM_DELETE -> onDeleteListClick()
                MENU_ITEM_CLEAR -> onClearCompletedClick()
              }
            },
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onAddTaskClick,
        containerColor = state.fabContainerColor ?: FloatingActionButtonDefaults.containerColor,
        contentColor = state.fabContentColor ?: MaterialTheme.colorScheme.onPrimaryContainer,
        icon = { Icon(painterResource(R.drawable.ic_fluent_add), contentDescription = null) },
        text = { Text(stringResource(R.string.new_task)) },
      )
    },
  ) { padding ->
    AppPullToRefreshBox(
      isRefreshing = state.isSyncing,
      onRefresh = onRefresh,
      modifier =
        Modifier
          .fillMaxSize()
          .padding(padding),
    ) {
      // A single LazyColumn is used even for the empty state (rather than swapping in a plain
      // Column) so there is always a scrollable descendant to dispatch nested-scroll drag events
      // to the pull-to-refresh gesture — a non-scrollable child never triggers it.
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        item {
          TagFilterRow(
            allTags = state.allTags,
            selectedTagId = state.selectedTagId,
            onTagSelected = onTagSelected,
          )
        }
        if (state.tasks.isEmpty()) {
          item {
            GoogleTasksEmptyState(modifier = Modifier.fillParentMaxSize())
          }
        } else {
          items(state.tasks, key = { it.id }) { task ->
            GoogleTaskRow(
              task = task,
              onClick = { onTaskClick(task.id) },
              onToggle = { onTaskToggle(task.id) },
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }
        }
      }
    }
  }

  if (state.showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = onDeleteDismiss,
      text = { Text(stringResource(R.string.delete_this_list)) },
      confirmButton = {
        TextButton(onClick = onDeleteConfirmed) { Text(stringResource(R.string.yes)) }
      },
      dismissButton = {
        TextButton(onClick = onDeleteDismiss) { Text(stringResource(R.string.no)) }
      },
    )
  }
}

@Composable
private fun taskListMenuItems(canDelete: Boolean): List<PopupMenuItem> =
  buildList {
    add(PopupMenuItem(id = MENU_ITEM_EDIT, title = stringResource(R.string.edit_list)))
    if (canDelete) {
      add(PopupMenuItem(id = MENU_ITEM_DELETE, title = stringResource(R.string.delete_list)))
    }
    add(PopupMenuItem(id = MENU_ITEM_CLEAR, title = stringResource(R.string.delete_completed_tasks)))
  }

@Preview(showBackground = true)
@Composable
private fun TaskListScreenPreview() {
  AppTheme {
    TaskListScreen(
      state =
        TaskListState(
          title = "Groceries",
          tasks =
            listOf(
              GoogleTaskItemState(
                id = "1",
                text = "Buy milk",
                notes = null,
                dueDate = "Tomorrow",
                isCompleted = false,
                taskListColor = Color(0xFF4CAF50).toArgb(),
                reminderId = null,
              ),
              GoogleTaskItemState(
                id = "2",
                text = "Buy bread",
                notes = "Whole grain",
                dueDate = null,
                isCompleted = true,
                taskListColor = Color(0xFF4CAF50).toArgb(),
                reminderId = null,
              ),
            ),
          fabContainerColor = Color(0xFF4CAF50),
          fabContentColor = Color.White,
          canDelete = true,
        ),
      onBackClick = {},
      onEditListClick = {},
      onDeleteListClick = {},
      onDeleteConfirmed = {},
      onDeleteDismiss = {},
      onClearCompletedClick = {},
      onTaskClick = {},
      onTaskToggle = {},
      onAddTaskClick = {},
      onRefresh = {},
      onTagSelected = {},
    )
  }
}
