package com.elementary.tasks.groups.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
  state: GroupsScreenState,
  onBackClick: () -> Unit,
  onAddClick: () -> Unit,
  onGroupClick: (String) -> Unit,
  onGroupMenuAction: (UiGroupList, GroupMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.groups)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddClick) {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_add),
          contentDescription = stringResource(R.string.create_group),
        )
      }
    },
  ) { padding ->
    when (val listState = state.listState) {
      is ListState.Loading -> {
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is ListState.Empty -> {
        GroupsEmptyState(modifier = Modifier.fillMaxSize().padding(padding))
      }

      is ListState.Ready -> {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding =
            PaddingValues(
              start = 16.dp,
              end = 16.dp,
              top = padding.calculateTopPadding() + 8.dp,
              bottom = padding.calculateBottomPadding() + 88.dp,
            ),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(listState.groups, key = { it.id }) { group ->
            GroupListItem(
              group = group,
              onClick = { onGroupClick(group.id) },
              onMenuAction = { action -> onGroupMenuAction(group, action) },
              modifier = Modifier.animateItem(),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun GroupsEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = AppIcons.Fluent.Group,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    )
    Text(
      text = stringResource(R.string.no_groups),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun GroupsScreenPreview() {
  AppTheme {
    GroupsScreen(
      state =
        GroupsScreenState(
          listState =
            ListState.Ready(
              groups =
                listOf(
                  UiGroupList(
                    id = "1",
                    title = "Def",
                    color = 0xFF2196F3.toInt(),
                    colorPosition = 5,
                    contrastColor = 0xFFFFFFFF.toInt(),
                    canDelete = false,
                    canSetAsDefault = false,
                    isDefaultGroup = true,
                    reminderCountText = "12 reminders",
                  ),
                  UiGroupList(
                    id = "2",
                    title = "General",
                    color = 0xFF2196F3.toInt(),
                    colorPosition = 5,
                    contrastColor = 0xFFFFFFFF.toInt(),
                    canDelete = true,
                    canSetAsDefault = true,
                    isDefaultGroup = false,
                    reminderCountText = "1 reminder",
                  ),
                  UiGroupList(
                    id = "3",
                    title = "Work",
                    color = 0xFFF44336.toInt(),
                    colorPosition = 0,
                    contrastColor = 0xFFFFFFFF.toInt(),
                    canDelete = true,
                    canSetAsDefault = true,
                    isDefaultGroup = false,
                    reminderCountText = "0 reminders",
                  ),
                ),
            ),
        ),
      onBackClick = {},
      onAddClick = {},
      onGroupClick = {},
      onGroupMenuAction = { _, _ -> },
    )
  }
}
