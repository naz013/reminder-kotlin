package com.github.naz013.feature.home.dayoverview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DayOverviewPane(modifier: Modifier = Modifier) {
  val viewModel = koinViewModel<DayOverviewViewModel>()
  val state by viewModel.state.collectAsState()
  DayOverviewContent(modifier = modifier, state = state)
}

@Composable
internal fun DayOverviewContent(
  modifier: Modifier = Modifier,
  state: DayOverviewState,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = stringResource(R.string.day_overview_title),
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.primary,
    )
    Text(
      text = state.dateHeading,
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.onBackground,
    )
    if (state.summary.isNotEmpty()) {
      Text(
        text = state.summary,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    WeekStrip(days = state.weekDays)
    NextItemCard(nextItem = state.nextItem)
    FreeTimeCard(freeTime = state.freeTime)
  }
}

@Composable
private fun WeekStrip(
  days: List<DayOverviewState.WeekDay>,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    days.forEach { day ->
      val container = if (day.isToday) {
        MaterialTheme.colorScheme.primary
      } else {
        MaterialTheme.colorScheme.surfaceContainer
      }
      val content = if (day.isToday) {
        MaterialTheme.colorScheme.onPrimary
      } else {
        MaterialTheme.colorScheme.onSurface
      }
      Column(
        modifier = Modifier
          .weight(1f)
          .background(container, RoundedCornerShape(16.dp))
          .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(text = day.weekDayLabel, style = MaterialTheme.typography.labelMedium, color = content)
        Text(text = day.dayOfMonth, style = MaterialTheme.typography.titleMedium, color = content)
      }
    }
  }
}

@Composable
private fun NextItemCard(
  nextItem: DayOverviewState.NextItem?,
  modifier: Modifier = Modifier,
) {
  InfoCard(modifier = modifier, title = stringResource(R.string.day_overview_next_up)) {
    if (nextItem == null) {
      Text(
        text = stringResource(R.string.day_overview_no_next),
        style = MaterialTheme.typography.bodyLarge,
      )
    } else {
      Text(text = nextItem.time, style = MaterialTheme.typography.titleLarge)
      Text(text = nextItem.title, style = MaterialTheme.typography.bodyLarge)
    }
  }
}

@Composable
private fun FreeTimeCard(
  freeTime: DayOverviewState.FreeTimeLabels,
  modifier: Modifier = Modifier,
) {
  InfoCard(modifier = modifier, title = stringResource(R.string.day_overview_free_time)) {
    Text(text = freeTime.current, style = MaterialTheme.typography.titleMedium)
    freeTime.largest?.let {
      Text(text = it, style = MaterialTheme.typography.bodyMedium)
    }
  }
}

@Composable
private fun InfoCard(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
      )
      content()
    }
  }
}
