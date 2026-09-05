package com.github.naz013.feature.reminder.lists.removed

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.reminder.UiReminderList
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.SearchBar

private val HEADER_ELEVATION = 3.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersArchiveScreen(
  state: RemindersArchiveScreenState,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
  onBackClick: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onDeleteAllClick: () -> Unit,
  onItemClick: (UiReminderList) -> Unit,
  onMenuAction: (UiReminderList, ArchiveReminderMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()
  val isScrolled by remember { derivedStateOf { lazyListState.canScrollBackward } }
  val headerElevation by animateDpAsState(
    targetValue = if (isScrolled) HEADER_ELEVATION else 0.dp,
    label = "archiveHeaderElevation",
  )

  Scaffold(
    modifier = modifier,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      Surface(color = MaterialTheme.colorScheme.background, shadowElevation = headerElevation) {
        Column {
          RemindersArchiveTopBar(
            onBackClick = onBackClick,
            canDeleteAll = state.listState is ListState.Ready,
            onDeleteAllClick = onDeleteAllClick,
          )

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
        }
      }
    },
  ) { padding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(padding),
    ) {
      when (val listState = state.listState) {
        is ListState.Loading -> {
          Box(
            modifier = Modifier.fillMaxSize().weight(1f),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator()
          }
        }

        is ListState.Empty -> {
          ArchiveEmptyState(modifier = Modifier.fillMaxSize().weight(1f))
        }

        is ListState.Ready -> {
          LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize().weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(listState.items, key = { it.id }) { item ->
              ArchiveReminderRow(
                item = item,
                onClick = { onItemClick(item) },
                onMenuAction = { action -> onMenuAction(item, action) },
                modifier = Modifier.animateItem(),
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemindersArchiveTopBar(
  onBackClick: () -> Unit,
  canDeleteAll: Boolean,
  onDeleteAllClick: () -> Unit,
) {
  TopAppBar(
    title = { Text(stringResource(R.string.reminders_archive)) },
    navigationIcon = {
      MenuIconButton(
        icon = AppIcons.Builder.ArrowLeft,
        contentDescription = null,
        onClick = onBackClick,
      )
    },
    actions = {
      if (canDeleteAll) {
        MenuIconButton(
          icon = painterResource(R.drawable.ic_fluent_broom),
          contentDescription = stringResource(R.string.delete_all),
          onClick = onDeleteAllClick,
        )
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
  )
}

@Composable
private fun ArchiveEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_fluent_archive),
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    )
    Text(
      text = stringResource(R.string.archive_is_empty),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun RemindersArchiveScreenEmptyPreview() {
  AppTheme {
    RemindersArchiveScreen(
      state = RemindersArchiveScreenState(listState = ListState.Empty),
      onBackClick = {},
      onSearchQueryChange = {},
      onDeleteAllClick = {},
      onItemClick = {},
      onMenuAction = { _, _ -> },
    )
  }
}
