package com.github.naz013.feature.settings.calendar.country

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.SearchBar
import com.github.naz013.ui.common.livedata.ObserveEvent
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HolidayCountryScreen(
  onBackClick: () -> Unit,
  onCountrySelected: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val viewModel = koinViewModel<HolidayCountryViewModel>()
  val state by viewModel.state.collectAsState()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is HolidayCountryViewModel.ViewModelEvent.CountrySelected -> onCountrySelected(event.code)
    }
  }

  BackHandler(onBack = onBackClick)

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.public_holidays_country)) },
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
      modifier = Modifier.fillMaxSize().padding(padding),
    ) {
      SearchBar(
        query = state.searchQuery,
        onQueryChange = viewModel::onSearchQueryChange,
        placeholder = stringResource(R.string.search),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      )

      when (val listState = state.listState) {
        is CountryListState.Loading -> {
          Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }

        is CountryListState.Empty -> {
          Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
            Text(
              text = stringResource(R.string.search_no_countries_found),
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
          }
        }

        is CountryListState.Ready -> {
          LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            items(listState.countries, key = { it.code }) { country ->
              CountryListItem(
                country = country,
                isSelected = country.code == state.selectedCode,
                onClick = { viewModel.onCountryClick(country) },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CountryListItem(
  country: UiCountry,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
      Text(text = country.flagEmoji, style = MaterialTheme.typography.headlineSmall)
      Text(
        text = country.name,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f).padding(start = 16.dp),
      )
      if (isSelected) {
        Icon(
          painter = AppIcons.Fluent.Checkmark,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun HolidayCountryScreenPreview() {
  AppTheme {
    Column {
      CountryListItem(
        country = UiCountry(code = "US", name = "United States", flagEmoji = "🇺🇸"),
        isSelected = true,
        onClick = {},
      )
      CountryListItem(
        country = UiCountry(code = "FR", name = "France", flagEmoji = "🇫🇷"),
        isSelected = false,
        onClick = {},
      )
    }
  }
}
