package com.github.naz013.feature.birthday.list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.logic.reminder.smartlist.SmartListFilter
import com.github.naz013.ui.agenda.AgendaMenuAction
import com.github.naz013.ui.agenda.BirthdayAgendaRow
import com.github.naz013.ui.agenda.UiAgendaBirthday
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.SelectionTopBar
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet
import com.github.naz013.ui.common.compose.foundation.component.BottomSheetHeader
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.component.SearchBar
import com.github.naz013.ui.tag.TagFilterRow

private val HEADER_ELEVATION = 3.dp
private val BIRTHDAY_SMART_LIST_FILTERS = listOf(SmartListFilter.TODAY, SmartListFilter.THIS_WEEK)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BirthdaysScreen(
  state: BirthdaysScreenState,
  onBackClick: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onSmartListSelected: (SmartListFilter?) -> Unit,
  onTagFilterSelected: (String?) -> Unit,
  onAddClick: () -> Unit,
  onItemClick: (UiAgendaBirthday) -> Unit,
  onItemLongClick: (String) -> Unit,
  onMenuAction: (UiAgendaBirthday, AgendaMenuAction) -> Unit,
  onDeleteConfirmed: () -> Unit,
  onDeleteDismiss: () -> Unit,
  onSelectionCancel: () -> Unit,
  onDeleteSelectedClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()
  val isScrolled by remember { derivedStateOf { lazyListState.canScrollBackward } }
  val headerElevation by animateDpAsState(
    targetValue = if (isScrolled) HEADER_ELEVATION else 0.dp,
    label = "birthdaysHeaderElevation",
  )
  var showFilterSheet by remember { mutableStateOf(false) }
  val hasActiveFilters = state.selectedSmartList != null || state.selectedTagId != null
  val isSelectionMode = state.selectedCount > 0

  BackHandler(enabled = isSelectionMode) { onSelectionCancel() }

  Scaffold(
    modifier = modifier,
    topBar = {
      if (isSelectionMode) {
        BirthdaysSelectionTopBar(
          selectedCount = state.selectedCount,
          onCancelClick = onSelectionCancel,
          onDeleteClick = onDeleteSelectedClick,
        )
      } else {
        Surface(color = MaterialTheme.colorScheme.background, shadowElevation = headerElevation) {
          Column {
            BirthdaysTopBar(
              onBackClick = onBackClick,
              onAddClick = onAddClick,
              onFilterClick = { showFilterSheet = true },
              hasActiveFilters = hasActiveFilters,
            )

            if (state.hasAnyItems) {
              SearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = stringResource(R.string.search),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 8.dp),
              )
            }
          }
        }
      }
    },
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
    ) {
      when (val listState = state.listState) {
        is ListState.Loading -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .weight(1f),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator()
          }
        }

        is ListState.Empty -> {
          BirthdaysEmptyState(modifier = Modifier
            .fillMaxSize()
            .weight(1f))
        }

        is ListState.Ready -> {
          LazyColumn(
            state = lazyListState,
            modifier = Modifier
              .fillMaxSize()
              .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(listState.items, key = { it.id }) { item ->
              BirthdayAgendaRow(
                item = item,
                onClick = { onItemClick(item) },
                onMenuAction = { action -> onMenuAction(item, action) },
                modifier = Modifier.animateItem(),
                onLongClick = { onItemLongClick(item.id) },
                isSelectionMode = isSelectionMode,
                onToggleSelected = { onItemClick(item) },
              )
            }
          }
        }
      }
    }
  }

  if (showFilterSheet) {
    BirthdaysFilterBottomSheet(
      state = state,
      hasActiveFilters = hasActiveFilters,
      onDismissRequest = { showFilterSheet = false },
      onSmartListSelected = onSmartListSelected,
      onTagFilterSelected = onTagFilterSelected,
    )
  }

  if (state.confirmDeleteId != null) {
    AlertDialog(
      onDismissRequest = onDeleteDismiss,
      text = { Text(stringResource(R.string.are_you_sure)) },
      confirmButton = {
        TextButton(onClick = onDeleteConfirmed) { Text(stringResource(R.string.yes)) }
      },
      dismissButton = {
        TextButton(onClick = onDeleteDismiss) { Text(stringResource(R.string.no)) }
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdaysFilterBottomSheet(
  state: BirthdaysScreenState,
  hasActiveFilters: Boolean,
  onDismissRequest: () -> Unit,
  onSmartListSelected: (SmartListFilter?) -> Unit,
  onTagFilterSelected: (String?) -> Unit,
) {
  AppModalBottomSheet(
    onDismissRequest = onDismissRequest,
  ) {
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(bottom = 16.dp),
    ) {
      BottomSheetHeader(title = stringResource(R.string.filter))

      FilterSection(title = stringResource(R.string.smart_lists)) {
        SmartListChipRow(selected = state.selectedSmartList, onSelect = onSmartListSelected)
      }

      if (state.availableTags.isNotEmpty()) {
        FilterSection(title = stringResource(R.string.tags)) {
          TagFilterRow(
            allTags = state.availableTags,
            selectedTagId = state.selectedTagId,
            onTagSelected = onTagFilterSelected,
          )
        }
      }

      if (hasActiveFilters) {
        Button(
          onClick = {
            onSmartListSelected(null)
            onTagFilterSelected(null)
          },
          modifier = Modifier.padding(horizontal = 16.dp),
        ) {
          Text(stringResource(R.string.filters_reset))
        }
      }
    }
  }
}

@Composable
private fun FilterSection(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
    Text(text = title, style = MaterialTheme.typography.titleSmall)
    Box(modifier = Modifier.padding(top = 8.dp)) {
      content()
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SmartListChipRow(
  selected: SmartListFilter?,
  onSelect: (SmartListFilter?) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    BIRTHDAY_SMART_LIST_FILTERS.forEach { filter ->
      FilterChip(
        selected = filter == selected,
        onClick = { onSelect(filter) },
        label = { Text(stringResource(filter.titleRes())) },
      )
    }
  }
}

private fun SmartListFilter.titleRes(): Int =
  when (this) {
    SmartListFilter.TODAY -> R.string.smart_list_today
    SmartListFilter.THIS_WEEK -> R.string.smart_list_this_week
    SmartListFilter.OVERDUE -> R.string.smart_list_overdue
    SmartListFilter.NO_GROUP -> R.string.smart_list_no_group
  }

@Composable
private fun BirthdaysEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_fluent_food_cake),
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    )
    Text(
      text = stringResource(R.string.no_events),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp),
    )
  }
}

private enum class BirthdaysSelectionAction { DELETE }

@Composable
private fun BirthdaysSelectionTopBar(
  selectedCount: Int,
  onCancelClick: () -> Unit,
  onDeleteClick: () -> Unit,
) {
  SelectionTopBar(
    title = pluralStringResource(R.plurals.birthdays_selected_count, selectedCount, selectedCount),
    onCancelClick = onCancelClick,
    actions = listOf(
      PopupMenuItem(
        id = BirthdaysSelectionAction.DELETE.ordinal,
        title = stringResource(R.string.delete),
        iconRes = R.drawable.ic_fluent_delete,
      ),
    ),
    onActionClick = { id ->
      when (BirthdaysSelectionAction.entries[id]) {
        BirthdaysSelectionAction.DELETE -> onDeleteClick()
      }
    },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdaysTopBar(
  onBackClick: () -> Unit,
  onAddClick: () -> Unit,
  onFilterClick: () -> Unit,
  hasActiveFilters: Boolean,
) {
  TopAppBar(
    title = { Text(stringResource(R.string.birthdays)) },
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
        contentDescription = stringResource(R.string.acc_add),
        onClick = onAddClick,
        iconColor = MaterialTheme.colorScheme.primary,
      )
      BadgedBox(
        badge = { if (hasActiveFilters) Badge() },
      ) {
        MenuIconButton(
          icon = Icons.Default.FilterList,
          contentDescription = stringResource(R.string.filter),
          onClick = onFilterClick,
        )
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
  )
}

@Preview(showBackground = true)
@Composable
private fun BirthdaysScreenEmptyPreview() {
  AppTheme {
    BirthdaysScreen(
      state = BirthdaysScreenState(listState = ListState.Empty),
      onBackClick = {},
      onSearchQueryChange = {},
      onSmartListSelected = {},
      onTagFilterSelected = {},
      onAddClick = {},
      onItemClick = {},
      onItemLongClick = {},
      onMenuAction = { _, _ -> },
      onDeleteConfirmed = {},
      onDeleteDismiss = {},
      onSelectionCancel = {},
      onDeleteSelectedClick = {},
    )
  }
}
