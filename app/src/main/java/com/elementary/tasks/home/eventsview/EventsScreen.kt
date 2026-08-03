package com.elementary.tasks.home.eventsview

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.component.SearchBar
import com.github.naz013.usecase.reminders.smartlist.SmartListFilter

private val HEADER_ELEVATION = 3.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
  state: EventsScreenState,
  onBackClick: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onCategoryToggle: (EventCategory) -> Unit,
  onSmartListSelected: (SmartListFilter?) -> Unit,
  onAddReminderClick: () -> Unit,
  onAddShoppingClick: () -> Unit,
  onAddBirthdayClick: () -> Unit,
  onArchiveClick: () -> Unit,
  onGroupsClick: () -> Unit,
  onTagsClick: () -> Unit,
  onItemClick: (UiEventItem) -> Unit,
  onEventMenuAction: (UiEventItem, EventMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()
  val isScrolled by remember { derivedStateOf { lazyListState.canScrollBackward } }
  val headerElevation by animateDpAsState(
    targetValue = if (isScrolled) HEADER_ELEVATION else 0.dp,
    label = "eventsHeaderElevation",
  )

  Scaffold(
    modifier = modifier,
    topBar = {
      Surface(color = MaterialTheme.colorScheme.background, shadowElevation = headerElevation) {
        Column {
          EventsTopBar(
            onBackClick = onBackClick,
            onAddReminderClick = onAddReminderClick,
            onAddShoppingClick = onAddShoppingClick,
            onAddBirthdayClick = onAddBirthdayClick,
            onArchiveClick = onArchiveClick,
            onGroupsClick = onGroupsClick,
            onTagsClick = onTagsClick,
          )

          if (state.listState !is ListState.Empty || state.searchQuery.isNotEmpty()) {
            SearchBar(
              query = state.searchQuery,
              onQueryChange = onSearchQueryChange,
              placeholder = stringResource(R.string.search),
              modifier =
                Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp),
            )
          }

          CategoryChipRow(
            selected = state.selectedCategories,
            onToggle = onCategoryToggle,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          )

          SmartListChipRow(
            selected = state.selectedSmartList,
            onSelect = onSmartListSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          )
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
          EventsEmptyState(modifier = Modifier.fillMaxSize().weight(1f))
        }

        is ListState.Ready -> {
          EventsList(
            items = listState.items,
            lazyListState = lazyListState,
            onItemClick = onItemClick,
            onEventMenuAction = onEventMenuAction,
            modifier = Modifier.fillMaxSize().weight(1f),
          )
        }
      }
    }
  }
}

@Composable
private fun EventsList(
  items: List<UiEventItem>,
  lazyListState: LazyListState,
  onItemClick: (UiEventItem) -> Unit,
  onEventMenuAction: (UiEventItem, EventMenuAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    state = lazyListState,
    modifier = modifier,
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(items, key = { it.id }) { item ->
      when (item) {
        is UiEventHeader -> {
          Text(
            text = item.text,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
          )
        }

        is UiEventReminder -> {
          ReminderEventRow(
            item = item,
            onClick = { onItemClick(item) },
            onMenuAction = { action -> onEventMenuAction(item, action) },
            modifier = Modifier.animateItem(),
          )
        }

        is UiEventBirthday -> {
          BirthdayEventRow(
            item = item,
            onClick = { onItemClick(item) },
            onMenuAction = { action -> onEventMenuAction(item, action) },
            modifier = Modifier.animateItem(),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChipRow(
  selected: Set<EventCategory>,
  onToggle: (EventCategory) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    EventCategory.entries.forEach { category ->
      FilterChip(
        selected = category in selected,
        onClick = { onToggle(category) },
        label = { Text(stringResource(category.titleRes())) },
      )
    }
  }
}

private fun EventCategory.titleRes(): Int =
  when (this) {
    EventCategory.REMINDERS -> R.string.reminders
    EventCategory.SHOPPING -> R.string.shopping_lists
    EventCategory.BIRTHDAYS -> R.string.birthdays
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
    SmartListFilter.entries.forEach { filter ->
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
    SmartListFilter.OVERDUE -> R.string.smart_list_overdue
    SmartListFilter.THIS_WEEK -> R.string.smart_list_this_week
    SmartListFilter.NO_GROUP -> R.string.smart_list_no_group
  }

@Composable
private fun EventsEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_fluent_alert),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventsTopBar(
  onBackClick: () -> Unit,
  onAddReminderClick: () -> Unit,
  onAddShoppingClick: () -> Unit,
  onAddBirthdayClick: () -> Unit,
  onArchiveClick: () -> Unit,
  onGroupsClick: () -> Unit,
  onTagsClick: () -> Unit,
) {
  TopAppBar(
    title = { Text(stringResource(R.string.events)) },
    navigationIcon = {
      MenuIconButton(
        icon = painterResource(R.drawable.ic_builder_arrow_left),
        contentDescription = null,
        onClick = onBackClick,
      )
    },
    actions = {
      AddMenuButton(
        onAddReminderClick = onAddReminderClick,
        onAddShoppingClick = onAddShoppingClick,
        onAddBirthdayClick = onAddBirthdayClick,
      )
      OverflowMenuButton(
        onArchiveClick = onArchiveClick,
        onGroupsClick = onGroupsClick,
        onTagsClick = onTagsClick,
      )
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
  )
}

@Composable
private fun AddMenuButton(
  onAddReminderClick: () -> Unit,
  onAddShoppingClick: () -> Unit,
  onAddBirthdayClick: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    MenuIconButton(
      icon = AppIcons.Fluent.Add,
      contentDescription = stringResource(R.string.acc_add),
      onClick = { expanded = true },
      iconColor = MaterialTheme.colorScheme.primary,
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items =
        listOf(
          PopupMenuItem(id = 0, title = stringResource(R.string.new_reminder), iconRes = R.drawable.ic_fluent_alert),
          PopupMenuItem(id = 1, title = stringResource(R.string.shopping_list), iconRes = R.drawable.ic_fluent_cart),
          PopupMenuItem(id = 2, title = stringResource(R.string.add_birthday), iconRes = R.drawable.ic_fluent_food_cake),
        ),
      onItemClick = { id ->
        when (id) {
          0 -> onAddReminderClick()
          1 -> onAddShoppingClick()
          2 -> onAddBirthdayClick()
        }
      },
    )
  }
}

@Composable
private fun OverflowMenuButton(
  onArchiveClick: () -> Unit,
  onGroupsClick: () -> Unit,
  onTagsClick: () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  val actions =
    listOf(
      Triple(0, stringResource(R.string.reminders_archive), R.drawable.ic_fluent_archive) to onArchiveClick,
      Triple(1, stringResource(R.string.groups), R.drawable.ic_fluent_group) to onGroupsClick,
      Triple(2, stringResource(R.string.tags), R.drawable.ic_builder_group) to onTagsClick,
    )
  Box {
    MenuIconButton(
      icon = painterResource(R.drawable.ic_fluent_more_vertical),
      contentDescription = stringResource(R.string.more_options),
      onClick = { expanded = true },
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = actions.map { (triple, _) -> PopupMenuItem(id = triple.first, title = triple.second, iconRes = triple.third) },
      onItemClick = { id -> actions.firstOrNull { it.first.first == id }?.second?.invoke() },
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun EventsScreenEmptyPreview() {
  AppTheme {
    EventsScreen(
      state = EventsScreenState(listState = ListState.Empty),
      onBackClick = {},
      onSearchQueryChange = {},
      onCategoryToggle = {},
      onSmartListSelected = {},
      onAddReminderClick = {},
      onAddShoppingClick = {},
      onAddBirthdayClick = {},
      onArchiveClick = {},
      onGroupsClick = {},
      onTagsClick = {},
      onItemClick = {},
      onEventMenuAction = { _, _ -> },
    )
  }
}
