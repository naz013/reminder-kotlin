package com.github.naz013.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.logic.search.SearchResult
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.navigation.DetailPanePlaceholder
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.searchEntries(
  backStack: MutableList<NavKey>,
  onOpenResult: (SearchResult) -> Unit,
) {
  entry<SearchNavKey.Main>(
    metadata = ListDetailSceneStrategy.listPane(
      detailPlaceholder = {
        DetailPanePlaceholder(
          text = stringResource(R.string.search_start_prompt),
          icon = AppIcons.Fluent.Search,
        )
      },
    ),
  ) {
    SearchEntry(backStack = backStack, onOpenResult = onOpenResult)
  }
}

@Composable
private fun SearchEntry(
  backStack: MutableList<NavKey>,
  onOpenResult: (SearchResult) -> Unit,
) {
  val viewModel = koinViewModel<SearchViewModel>()
  val state by viewModel.state.collectAsState()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is SearchViewModel.SearchEvent.OpenResult -> onOpenResult(event.result)
      is SearchViewModel.SearchEvent.MoveBack -> backStack.removeLastOrNull()
    }
  }

  SearchScreen(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    state = state,
    onBackClick = { viewModel.onBackClicked() },
    onQueryChange = { viewModel.onQueryChanged(it) },
    onRecentQueryClick = { viewModel.onRecentQueryClicked(it) },
    onFilterSelect = { viewModel.onFilterSelected(it) },
    onResultClick = { viewModel.onResultClicked(it) },
  )
}
