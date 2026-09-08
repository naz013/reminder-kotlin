package com.github.naz013.feature.places.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.EmptyState
import com.github.naz013.ui.common.compose.foundation.component.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlacesScreen(
  modifier: Modifier = Modifier,
  state: PlacesScreenState,
  onBackClick: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onAddClick: () -> Unit,
  onPlaceClick: (String) -> Unit,
  onPlaceMenuAction: (String, PlaceMenuAction) -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.places)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBackClick,
          )
        },
        colors = TopAppbarColor,
        actions = {
          MenuIconButton(
            icon = AppIcons.Fluent.Add,
            contentDescription = stringResource(R.string.acc_add),
            onClick = onAddClick,
            iconColor = MaterialTheme.colorScheme.primary
          )
        }
      )
    },
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
    ) {
      if (state.listState !is ListState.Empty || state.searchQuery.isNotEmpty()) {
        SearchBar(
          query = state.searchQuery,
          onQueryChange = onSearchQueryChange,
          placeholder = stringResource(R.string.search_place),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }

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
          EmptyState(
            icon = AppIcons.Fluent.Place,
            message = stringResource(R.string.no_places),
            modifier = Modifier
              .fillMaxSize()
              .weight(1f),
          )
        }

        is ListState.Ready -> {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .weight(1f),
            contentPadding = PaddingValues(
              start = 16.dp,
              end = 16.dp,
              top = 8.dp,
              bottom = 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            items(listState.places, key = { it.id }) { place ->
              PlaceListItemCard(
                place = place,
                onClick = { onPlaceClick(place.id) },
                onMenuAction = { action -> onPlaceMenuAction(place.id, action) },
              )
            }
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PlacesScreenPreview() {
  AppTheme {
    PlacesScreen(
      state = PlacesScreenState(listState = ListState.Empty),
      onBackClick = {},
      onSearchQueryChange = {},
      onAddClick = {},
      onPlaceClick = {},
      onPlaceMenuAction = { _, _ -> },
    )
  }
}
