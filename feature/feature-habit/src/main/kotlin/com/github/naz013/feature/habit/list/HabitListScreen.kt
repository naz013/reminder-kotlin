package com.github.naz013.feature.habit.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.EmptyState

private val MIN_CARD_WIDTH = 320.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitListScreen(
  modifier: Modifier = Modifier,
  state: HabitListState,
  onBackClick: () -> Unit,
  onAddClick: () -> Unit,
  onHabitClick: (String) -> Unit,
  onCheckInClick: (String) -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.habits)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBackClick,
          )
        },
        actions = {
          MenuIconButton(
            icon = AppIcons.Fluent.Add,
            contentDescription = stringResource(R.string.new_habit),
            onClick = onAddClick,
            iconColor = MaterialTheme.colorScheme.primary,
          )
        },
        colors = TopAppbarColor,
      )
    },
  ) { padding ->
    when (val listState = state.listState) {
      is HabitListDisplayState.Loading -> {
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is HabitListDisplayState.Empty -> {
        EmptyState(
          modifier = Modifier.fillMaxSize().padding(padding),
          icon = AppIcons.Fluent.CalendarCheckmark,
          message = stringResource(R.string.no_habits),
        )
      }

      is HabitListDisplayState.Ready -> {
        LazyVerticalGrid(
          modifier = Modifier.fillMaxSize().padding(padding),
          columns = GridCells.Adaptive(minSize = MIN_CARD_WIDTH),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(listState.habits, key = { it.id }) { habit ->
            HabitCard(
              habit = habit,
              onClick = { onHabitClick(habit.id) },
              onCheckInClick = { onCheckInClick(habit.id) },
            )
          }
        }
      }
    }
  }
}
