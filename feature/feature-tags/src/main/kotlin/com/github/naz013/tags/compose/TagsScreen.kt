package com.github.naz013.tags.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.tags.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.SelectionOverlay
import com.github.naz013.ui.common.compose.foundation.SelectionTopBar
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.EmptyState
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.icon.DrawableCatalog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun TagsScreen(
  modifier: Modifier = Modifier,
  state: TagsScreenState,
  onBackClick: () -> Unit,
  onAddClick: () -> Unit,
  onTagClick: (String) -> Unit,
  onTagLongClick: (String) -> Unit,
  onTagMenuAction: (TagState, TagMenuAction) -> Unit,
  onSelectionCancel: () -> Unit,
  onDeleteSelectedClick: () -> Unit,
  onChangeColorClick: () -> Unit,
) {
  val isSelectionMode = state.selectedCount > 0

  BackHandler(enabled = isSelectionMode) { onSelectionCancel() }

  Scaffold(
    modifier = modifier,
    topBar = {
      if (isSelectionMode) {
        TagsSelectionTopBar(
          selectedCount = state.selectedCount,
          onCancelClick = onSelectionCancel,
          onDeleteClick = onDeleteSelectedClick,
          onChangeColorClick = onChangeColorClick,
        )
      } else {
        TopAppBar(
          title = { Text(stringResource(R.string.tags)) },
          navigationIcon = {
            MenuIconButton(
              icon = AppIcons.Builder.ArrowLeft,
              contentDescription = stringResource(com.github.naz013.ui.common.R.string.cd_back),
              onClick = onBackClick
            )
          },
          actions = {
            MenuIconButton(
              icon = AppIcons.Fluent.Add,
              contentDescription = stringResource(R.string.new_tag),
              onClick = onAddClick,
              iconColor = MaterialTheme.colorScheme.primary,
            )
          },
          colors = TopAppbarColor
        )
      }
    },
  ) { padding ->
    when (val listState = state.listState) {
      is TagsListState.Loading -> {
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is TagsListState.Empty -> {
        EmptyState(
          icon = AppIcons.Builder.Tag,
          message = stringResource(R.string.no_tags),
          modifier = Modifier.fillMaxSize().padding(padding),
        )
      }

      is TagsListState.Ready -> {
        FlowRow(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
              start = 16.dp,
              end = 16.dp,
              top = padding.calculateTopPadding() + 8.dp,
              bottom = padding.calculateBottomPadding() + 88.dp
            ),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listState.tags.forEach { tag ->
            TagListItem(
              tag = tag,
              isSelectionMode = isSelectionMode,
              onClick = { onTagClick(tag.id) },
              onLongClick = { onTagLongClick(tag.id) },
              onMenuAction = { action -> onTagMenuAction(tag, action) },
            )
          }
        }
      }
    }
  }
}

private enum class TagsSelectionAction { CHANGE_COLOR, DELETE }

@Composable
private fun TagsSelectionTopBar(
  selectedCount: Int,
  onCancelClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onChangeColorClick: () -> Unit,
) {
  SelectionTopBar(
    title = pluralStringResource(R.plurals.tags_selected_count, selectedCount, selectedCount),
    onCancelClick = onCancelClick,
    actions = tagsSelectionMenuItems(),
    onActionClick = { id ->
      when (TagsSelectionAction.entries[id]) {
        TagsSelectionAction.CHANGE_COLOR -> onChangeColorClick()
        TagsSelectionAction.DELETE -> onDeleteClick()
      }
    },
  )
}

@Composable
private fun tagsSelectionMenuItems(): List<PopupMenuItem> =
  listOf(
    PopupMenuItem(
      id = TagsSelectionAction.CHANGE_COLOR.ordinal,
      title = stringResource(R.string.change_color),
      iconRes = DrawableCatalog.Fluent.ColorBackground,
    ),
    PopupMenuItem(
      id = TagsSelectionAction.DELETE.ordinal,
      title = stringResource(R.string.delete),
      iconRes = DrawableCatalog.Fluent.Delete,
    ),
  )

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TagListItem(
  modifier: Modifier = Modifier,
  tag: TagState,
  isSelectionMode: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onMenuAction: (TagMenuAction) -> Unit,
) {
  Card(
    modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
    colors = CardDefaults.cardColors(
      containerColor = if (tag.isHighlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
    ),
    border = if (tag.isHighlighted) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
  ) {
    Row(
      modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Canvas(modifier = Modifier.size(20.dp)) {
        drawCircle(color = tag.color)
      }
      Text(text = tag.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f, fill = false))
      Box {
        SelectionOverlay(
          isSelectionMode = isSelectionMode,
          isSelected = tag.isSelected,
          onToggleSelected = onClick,
        ) {
          TagMenu(onMenuAction = onMenuAction)
        }
      }
    }
  }
}

@Composable
private fun BoxScope.TagMenu(onMenuAction: (TagMenuAction) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  val items = listOf(
    PopupMenuItem(id = TagMenuAction.EDIT.ordinal, title = stringResource(R.string.edit), iconRes = DrawableCatalog.Fluent.Edit),
    PopupMenuItem(id = TagMenuAction.DELETE.ordinal, title = stringResource(R.string.delete), iconRes = DrawableCatalog.Fluent.Delete),
  )
  MenuIconButton(
    icon = AppIcons.Fluent.MoreVertical,
    contentDescription = stringResource(R.string.more_options),
    onClick = { expanded = true },
  )
  AppDropdownMenu(
    expanded = expanded,
    onDismissRequest = { expanded = false },
    items = items,
    onItemClick = { id ->
      expanded = false
      onMenuAction(TagMenuAction.entries[id])
    },
  )
}

@Preview(showBackground = true)
@Composable
private fun TagsScreenPreview() {
  AppTheme {
    TagsScreen(
      state = TagsScreenState(
        listState = TagsListState.Ready(
          tags = listOf(
            TagState(id = "1", name = "Work", color = Color.Red),
            TagState(id = "2", name = "Home", color = Color.Cyan)
          )
        )
      ),
      onBackClick = {},
      onAddClick = {},
      onTagClick = {},
      onTagLongClick = {},
      onTagMenuAction = { _, _ -> },
      onSelectionCancel = {},
      onDeleteSelectedClick = {},
      onChangeColorClick = {},
    )
  }
}
