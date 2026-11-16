package com.github.naz013.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.logic.search.SearchResult
import com.github.naz013.logic.search.SearchResultType
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.SearchBar

@Composable
internal fun SearchScreen(
  modifier: Modifier = Modifier,
  state: SearchScreenState,
  onBackClick: () -> Unit,
  onQueryChange: (String) -> Unit,
  onRecentQueryClick: (String) -> Unit,
  onFilterSelect: (SearchResultType?) -> Unit,
  onResultClick: (SearchResult) -> Unit,
) {
  Column(modifier = modifier.statusBarsPadding()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      MenuIconButton(
        icon = AppIcons.Builder.ArrowLeft,
        contentDescription = stringResource(R.string.cd_back),
        onClick = onBackClick,
      )
      SearchBar(
        modifier = Modifier.weight(1f),
        query = state.query,
        onQueryChange = onQueryChange,
        placeholder = stringResource(R.string.search_global_hint),
      )
    }
    when {
      state.isLoading && state.sections.isEmpty() -> CenteredBox { CircularProgressIndicator() }
      state.showRecent -> RecentQueries(
        modifier = Modifier.fillMaxSize(),
        queries = state.recentQueries,
        onQueryClick = onRecentQueryClick,
      )
      state.showNoResults -> CenteredBox {
        Text(
          text = stringResource(R.string.search_no_results, state.query.trim()),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      else -> Results(
        modifier = Modifier.fillMaxSize(),
        state = state,
        onFilterSelect = onFilterSelect,
        onResultClick = onResultClick,
      )
    }
  }
}

@Composable
private fun CenteredBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    content()
  }
}

@Composable
private fun RecentQueries(
  modifier: Modifier = Modifier,
  queries: List<String>,
  onQueryClick: (String) -> Unit,
) {
  if (queries.isEmpty()) {
    CenteredBox(modifier) {
      Text(
        text = stringResource(R.string.search_start_prompt),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    return
  }
  LazyColumn(modifier = modifier) {
    item(key = "recent_header") {
      SectionHeader(title = stringResource(R.string.search_recent_title), count = null)
    }
    items(queries, key = { "recent_$it" }) { query ->
      ListItem(
        modifier = Modifier.clickable { onQueryClick(query) },
        leadingContent = {
          Icon(
            painter = AppIcons.Fluent.History,
            contentDescription = null,
          )
        },
        headlineContent = { Text(text = query) },
      )
    }
  }
}

@Composable
private fun Results(
  modifier: Modifier = Modifier,
  state: SearchScreenState,
  onFilterSelect: (SearchResultType?) -> Unit,
  onResultClick: (SearchResult) -> Unit,
) {
  LazyColumn(modifier = modifier) {
    item(key = "filters") {
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        item(key = "all") {
          FilterChip(
            selected = state.selectedType == null,
            onClick = { onFilterSelect(null) },
            label = { Text(text = stringResource(R.string.all)) },
          )
        }
        items(state.chips, key = { it.type.name }) { chip ->
          FilterChip(
            selected = state.selectedType == chip.type,
            onClick = { onFilterSelect(chip.type) },
            label = { Text(text = "${stringResource(chip.type.labelRes())} (${chip.count})") },
          )
        }
      }
    }
    state.sections.forEach { section ->
      item(key = "header_${section.type.name}") {
        SectionHeader(title = stringResource(section.type.labelRes()), count = section.count)
      }
      items(section.items, key = { "${it.type.name}_${it.id}" }) { result ->
        ListItem(
          modifier = Modifier.clickable { onResultClick(result) },
          headlineContent = { Text(text = result.title, maxLines = 2) },
          supportingContent = (result.subtitle ?: result.matchedTag?.let { "#$it" })?.let {
            { Text(text = it, maxLines = 2) }
          },
        )
      }
    }
  }
}

@Composable
private fun SectionHeader(modifier: Modifier = Modifier, title: String, count: Int?) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.primary,
    )
    if (count != null) {
      Text(
        text = count.toString(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
  val reminders = listOf(SearchResult(SearchResultType.REMINDER, "1", "Buy milk", "Tomorrow"))
  AppTheme {
    SearchScreen(
      state = SearchScreenState(
        query = "milk",
        hasSearched = true,
        chips = listOf(SearchFilterChip(SearchResultType.REMINDER, 1)),
        sections = listOf(SearchSection(SearchResultType.REMINDER, 1, reminders)),
      ),
      onBackClick = {},
      onQueryChange = {},
      onRecentQueryClick = {},
      onFilterSelect = {},
      onResultClick = {},
    )
  }
}
