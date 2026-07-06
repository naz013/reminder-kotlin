package com.elementary.tasks.googletasks.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.elementary.tasks.googletasks.GoogleTaskRow
import com.elementary.tasks.googletasks.GoogleTasksEmptyState

/**
 * Body content only - the title/back-arrow/menu chrome is the native Toolbar owned by
 * [com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment].
 */
@Composable
fun TaskListScreen(
  state: TaskListState,
  onTaskClick: (String) -> Unit,
  onTaskToggle: (String) -> Unit,
  onAddTaskClick: () -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
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
    PullToRefreshBox(
      isRefreshing = state.isLoading,
      onRefresh = onRefresh,
      modifier =
        Modifier
          .fillMaxSize()
          .padding(padding),
    ) {
      if (state.tasks.isEmpty()) {
        GoogleTasksEmptyState(modifier = Modifier.fillMaxSize())
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(state.tasks, key = { it.id }) { task ->
            GoogleTaskRow(
              task = task,
              onClick = { onTaskClick(task.id) },
              onToggle = { onTaskToggle(task.id) },
            )
          }
        }
      }
    }
  }
}
