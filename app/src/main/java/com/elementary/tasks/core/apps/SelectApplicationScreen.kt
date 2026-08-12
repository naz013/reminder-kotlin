package com.elementary.tasks.core.apps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.SearchBar
import org.koin.androidx.compose.koinViewModel

@Deprecated("After S")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectApplicationScreen(
  onBackClick: () -> Unit,
  onAppSelected: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val viewModel = koinViewModel<SelectApplicationViewModel>()
  val state by viewModel.state.collectAsState()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is SelectApplicationViewModel.ViewModelEvent.AppSelected -> onAppSelected(event.packageName)
    }
  }

  BackHandler(onBack = onBackClick)

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.choose_application)) },
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
  ) { padding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(padding),
    ) {
      SearchBar(
        query = state.searchQuery,
        onQueryChange = viewModel::onSearchQueryChange,
        placeholder = stringResource(R.string.search),
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
      )

      when (val listState = state.listState) {
        is AppListState.Loading -> {
          Box(
            modifier =
              Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator()
          }
        }

        is AppListState.Empty -> {
          SelectApplicationEmptyState(
            modifier =
              Modifier
                .fillMaxSize()
                .weight(1f),
          )
        }

        is AppListState.Ready -> {
          LazyColumn(
            modifier =
              Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            items(listState.apps, key = { it.packageName }) { app ->
              ApplicationListItem(app = app, onClick = { viewModel.onAppClick(app) })
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ApplicationListItem(
  app: UiApplicationList,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
      if (app.icon != null) {
        Image(
          bitmap = app.icon.asImageBitmap(),
          contentDescription = null,
          modifier = Modifier.size(40.dp),
        )
      } else {
        Box(modifier = Modifier.size(40.dp))
      }
      Text(
        text = app.name,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
          Modifier
            .weight(1f)
            .padding(start = 16.dp),
      )
    }
  }
}

@Composable
private fun SelectApplicationEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Image(
      painter = painterResource(R.drawable.ic_human_resources),
      contentDescription = null,
      modifier = Modifier.size(dimensionResource(R.dimen.empty_image_size)),
    )
    Text(
      text = stringResource(R.string.applications_not_found),
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun SelectApplicationScreenEmptyPreview() {
  AppTheme {
    SelectApplicationEmptyState(modifier = Modifier.fillMaxSize())
  }
}
