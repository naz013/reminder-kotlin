package com.github.naz013.feature.googletask

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.naz013.ui.googletask.GoogleTaskItemState
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.google.android.gms.common.SignInButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleTasksScreen(
  state: GoogleTasksState,
  onBackClick: () -> Unit,
  onConnectClick: () -> Unit,
  onAddListClick: () -> Unit,
  onAddTaskClick: () -> Unit,
  onTaskListClick: (String) -> Unit,
  onTaskClick: (String) -> Unit,
  onTaskToggle: (String) -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.google_tasks)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        actions = {
          if (state.isLoggedIn) {
            MenuIconButton(
              icon = painterResource(R.drawable.ic_fluent_task_list_add),
              contentDescription = stringResource(R.string.new_tasks_list),
              onClick = onAddListClick,
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
    floatingActionButton = {
      if (state.isLoggedIn) {
        ExtendedFloatingActionButton(
          onClick = onAddTaskClick,
          containerColor = state.fabContainerColor ?: FloatingActionButtonDefaults.containerColor,
          contentColor = state.fabContentColor ?: MaterialTheme.colorScheme.onPrimaryContainer,
          icon = { Icon(painterResource(R.drawable.ic_fluent_add), contentDescription = null) },
          text = { Text(stringResource(R.string.new_task)) },
        )
      }
    },
  ) { padding ->
    if (!state.isLoggedIn) {
      NotLoggedInContent(
        onConnectClick = onConnectClick,
        modifier =
          Modifier
            .fillMaxSize()
            .padding(padding),
      )
      return@Scaffold
    }

    PullToRefreshBox(
      isRefreshing = state.isLoading,
      onRefresh = onRefresh,
      modifier =
        Modifier
          .fillMaxSize()
          .padding(padding),
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        if (state.taskLists.isNotEmpty()) {
          LazyRow(
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(state.taskLists, key = { it.id }) { entry ->
              TaskListTile(entry = entry, onClick = { onTaskListClick(entry.id) })
            }
          }
        }

        if (state.tasks.isEmpty()) {
          GoogleTasksEmptyState(
            modifier =
              Modifier
                .fillMaxSize()
                .weight(1f),
          )
        } else {
          LazyColumn(
            modifier =
              Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
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
}

@Composable
private fun TaskListTile(
  entry: UiGoogleTaskListEntry,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val color = Color(entry.color)
  Box(
    modifier =
      modifier
        .background(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 10.dp),
  ) {
    Text(
      text = entry.title,
      style = MaterialTheme.typography.titleMedium,
      color = color,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
internal fun GoogleTaskRow(
  task: GoogleTaskItemState,
  onClick: () -> Unit,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val accentColor = task.taskListColor?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp, horizontal = 12.dp),
    ) {
      Icon(
        painter =
          painterResource(
            if (task.isCompleted) R.drawable.ic_fluent_checkbox_checked else R.drawable.ic_fluent_checkbox_unchecked,
          ),
        contentDescription = null,
        tint = accentColor,
        modifier =
          Modifier
            .size(28.dp)
            .clickable(onClick = onToggle),
      )
      Column(
        modifier =
          Modifier
            .weight(1f)
            .padding(start = 12.dp),
      ) {
        Text(
          text = task.text,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (!task.notes.isNullOrEmpty()) {
          Text(
            text = task.notes,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      if (!task.dueDate.isNullOrEmpty()) {
        Text(
          text = task.dueDate,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
internal fun GoogleTasksEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_fluent_task_list_add),
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    )
    Text(
      text = stringResource(R.string.no_google_tasks),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp),
    )
  }
}

@Composable
private fun NotLoggedInContent(
  onConnectClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .background(MaterialTheme.colorScheme.background),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(R.string.you_not_logged_to_google_tasks),
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 24.dp),
    )
    val context = LocalContext.current
    AndroidView(
      modifier = Modifier.padding(top = 16.dp),
      factory = {
        SignInButton(context).apply {
          setSize(SignInButton.SIZE_WIDE)
        }
      },
      update = { button -> button.setOnClickListener { onConnectClick() } },
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun GoogleTasksScreenPreview() {
  AppTheme {
    GoogleTasksScreen(
      state = GoogleTasksState(isLoggedIn = true),
      onBackClick = {},
      onConnectClick = {},
      onAddListClick = {},
      onAddTaskClick = {},
      onTaskListClick = {},
      onTaskClick = {},
      onTaskToggle = {},
      onRefresh = {},
    )
  }
}
