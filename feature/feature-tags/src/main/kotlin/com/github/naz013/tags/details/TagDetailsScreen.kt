package com.github.naz013.tags.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.tags.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.component.SearchBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun TagDetailsScreen(
  modifier: Modifier = Modifier,
  state: TagDetailsState,
  onBackClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onTypeSelected: (TagContentType) -> Unit,
  onItemClick: (TagDetailItem) -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            TagColorDot(color = state.color)
            Text(
              text = state.title,
              modifier = Modifier.padding(start = 12.dp),
            )
          }
        },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        actions = {
          OverflowMenu(
            canDelete = state.canDelete,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    if (state.isLoading) {
      Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
      return@Scaffold
    }

    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      SearchBar(
        query = state.searchQuery,
        onQueryChange = onSearchQueryChange,
        placeholder = stringResource(R.string.search),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      )
      TypeFilterRow(
        selected = state.selectedType,
        onSelect = onTypeSelected,
        modifier = Modifier.padding(bottom = 8.dp),
      )

      if (state.sections.isEmpty()) {
        TagDetailsEmptyState(modifier = Modifier.fillMaxSize())
      } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
          item { adsContent() }

          state.sections.forEach { section ->
            item(key = "header_${section.type}") {
              SectionHeader(text = stringResource(section.type.titleRes()))
            }
            items(section.items, key = { it.id }) { item ->
              TagDetailItemRow(
                item = item,
                onClick = { onItemClick(item) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
              )
            }
          }
        }
      }
    }
  }
}

private fun TagContentType.titleRes(): Int =
  when (this) {
    TagContentType.REMINDER -> R.string.reminders
    TagContentType.NOTE -> R.string.notes
    TagContentType.GOOGLE_TASK -> R.string.google_tasks
    TagContentType.BIRTHDAY -> R.string.birthdays
    TagContentType.ALL -> R.string.all
  }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeFilterRow(
  selected: TagContentType,
  onSelect: (TagContentType) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    TagContentType.entries.forEach { type ->
      FilterChip(
        selected = selected == type,
        onClick = { onSelect(type) },
        label = { Text(stringResource(type.titleRes())) },
      )
    }
  }
}

@Composable
private fun TagColorDot(color: Color) {
  Box(
    modifier =
      Modifier
        .size(14.dp)
        .clip(CircleShape)
        .background(color),
  )
}

@Composable
private fun OverflowMenu(
  canDelete: Boolean,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val items =
    buildList {
      add(PopupMenuItem(id = OverflowAction.EDIT.ordinal, title = stringResource(R.string.edit), iconRes = R.drawable.ic_fluent_edit))
      if (canDelete) {
        add(
          PopupMenuItem(
            id = OverflowAction.DELETE.ordinal,
            title = stringResource(R.string.delete),
            iconRes = R.drawable.ic_fluent_delete,
          ),
        )
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
      items = items,
      onItemClick = { id ->
        expanded = false
        when (OverflowAction.entries[id]) {
          OverflowAction.EDIT -> onEditClick()
          OverflowAction.DELETE -> onDeleteClick()
        }
      },
    )
  }
}

private enum class OverflowAction { EDIT, DELETE }

@Composable
private fun SectionHeader(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
  )
}

@Composable
private fun TagDetailsEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = AppIcons.Builder.Tag,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    )
    Text(
      text = stringResource(R.string.tag_has_no_items),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun TagDetailsScreenPreview() {
  AppTheme {
    TagDetailsScreen(
      state =
        TagDetailsState(
          isLoading = false,
          title = "Work",
          color = Color.Blue,
          canDelete = true,
          sections = emptyList(),
        ),
      onBackClick = {},
      onEditClick = {},
      onDeleteClick = {},
      onSearchQueryChange = {},
      onTypeSelected = {},
      onItemClick = {},
      adsContent = {},
    )
  }
}
