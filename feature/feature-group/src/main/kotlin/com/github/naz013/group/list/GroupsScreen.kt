package com.github.naz013.group.list

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.SelectionTopBar
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.group.UiGroupList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GroupsScreen(
  state: GroupsScreenState,
  onBackClick: () -> Unit,
  onAddClick: () -> Unit,
  onGroupClick: (String) -> Unit,
  onGroupLongClick: (String) -> Unit,
  onGroupMenuAction: (UiGroupList, GroupMenuAction) -> Unit,
  onSelectionCancel: () -> Unit,
  onDeleteSelectedClick: () -> Unit,
  onChangeColorClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isSelectionMode = state.selectedCount > 0
  val canDeleteSelection = (state.listState as? ListState.Ready)
    ?.groups?.none { it.isSelected && it.isDefaultGroup } ?: false

  BackHandler(enabled = isSelectionMode) { onSelectionCancel() }

  Scaffold(
    modifier = modifier,
    topBar = {
      if (isSelectionMode) {
        GroupsSelectionTopBar(
          selectedCount = state.selectedCount,
          canDeleteSelection = canDeleteSelection,
          onCancelClick = onSelectionCancel,
          onDeleteClick = onDeleteSelectedClick,
          onChangeColorClick = onChangeColorClick,
        )
      } else {
        TopAppBar(
          title = { Text(stringResource(R.string.groups)) },
          navigationIcon = {
            MenuIconButton(
              icon = AppIcons.Builder.ArrowLeft,
              contentDescription = null,
              onClick = onBackClick,
            )
          },
          actions = {
            MenuIconButton(
              icon = AppIcons.Fluent.Add,
              contentDescription = stringResource(R.string.create_group),
              onClick = onAddClick,
              iconColor = MaterialTheme.colorScheme.primary,
            )
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
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
              isSelectionMode = isSelectionMode,
              onClick = { onGroupClick(group.id) },
              onLongClick = { onGroupLongClick(group.id) },
              onMenuAction = { action -> onGroupMenuAction(group, action) },
              modifier = Modifier.animateItem(),
            )
          }
        }
      }
    }
  }
}

private enum class GroupsSelectionAction { CHANGE_COLOR, DELETE }

@Composable
private fun GroupsSelectionTopBar(
  selectedCount: Int,
  canDeleteSelection: Boolean,
  onCancelClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onChangeColorClick: () -> Unit,
) {
  SelectionTopBar(
    title = pluralStringResource(R.plurals.groups_selected_count, selectedCount, selectedCount),
    onCancelClick = onCancelClick,
    actions = groupsSelectionMenuItems(canDeleteSelection),
    onActionClick = { id ->
      when (GroupsSelectionAction.entries[id]) {
        GroupsSelectionAction.CHANGE_COLOR -> onChangeColorClick()
        GroupsSelectionAction.DELETE -> onDeleteClick()
      }
    },
  )
}

@Composable
private fun groupsSelectionMenuItems(canDeleteSelection: Boolean): List<PopupMenuItem> =
  buildList {
    add(
      PopupMenuItem(
        id = GroupsSelectionAction.CHANGE_COLOR.ordinal,
        title = stringResource(R.string.change_color),
        iconRes = R.drawable.ic_fluent_color_background,
      )
    )
    if (canDeleteSelection) {
      add(
        PopupMenuItem(
          id = GroupsSelectionAction.DELETE.ordinal,
          title = stringResource(R.string.delete),
          iconRes = R.drawable.ic_fluent_delete,
        )
      )
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
      onGroupLongClick = {},
      onSelectionCancel = {},
      onDeleteSelectedClick = {},
      onChangeColorClick = {},
      onGroupMenuAction = { _, _ -> },
    )
  }
}
