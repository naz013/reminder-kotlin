package com.github.naz013.insights.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.insights.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

private const val MIN_BAR_FRACTION = 0.02f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
  state: InsightsScreenState,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.insights)) },
        navigationIcon = {
          MenuIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            onClick = onBackClick
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    }
  ) { padding ->
    when (val listState = state.listState) {
      is InsightsListState.Loading -> {
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is InsightsListState.Empty -> {
        InsightsEmptyState(modifier = Modifier.fillMaxSize().padding(padding))
      }

      is InsightsListState.Ready -> {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = padding.calculateTopPadding() + 8.dp,
            bottom = padding.calculateBottomPadding() + 16.dp
          ),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            WeeklyTrendCard(trend = state.weeklyTrend, busiestDay = state.busiestDay)
          }
          items(listState.streaks, key = { it.eventId }) { streak ->
            StreakCard(streak = streak)
          }
        }
      }
    }
  }
}

@Composable
private fun WeeklyTrendCard(
  trend: List<WeeklyTrendUi>,
  busiestDay: DayOfWeek?,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(stringResource(R.string.weekly_trend), style = MaterialTheme.typography.titleMedium)
      if (trend.isNotEmpty()) {
        val maxCount = trend.maxOf { it.count }.coerceAtLeast(1)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(top = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.Bottom
        ) {
          trend.forEach { point ->
            Box(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight(if (point.count == 0) MIN_BAR_FRACTION else point.count.toFloat() / maxCount.toFloat())
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(MaterialTheme.colorScheme.primary)
            )
          }
        }
      }
      busiestDay?.let {
        Text(
          text = stringResource(R.string.busiest_day, it.name),
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(top = 12.dp)
        )
      }
    }
  }
}

@Composable
private fun StreakCard(
  streak: UiStreak,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(streak.title, style = MaterialTheme.typography.titleMedium)
      Text(
        text = stringResource(R.string.streak_current, streak.currentStreakDays),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp)
      )
      Text(
        text = stringResource(R.string.streak_longest, streak.longestStreakDays),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
      )
      Text(
        text = stringResource(R.string.streak_fired_count, streak.firedCount),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
      )
    }
  }
}

@Composable
private fun InsightsEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      painter = AppIcons.Fluent.DataPie,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    )
    Text(
      text = stringResource(R.string.no_insights_yet),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp)
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun InsightsScreenPreview() {
  AppTheme {
    InsightsScreen(
      state = InsightsScreenState(
        listState = InsightsListState.Ready(
          streaks = listOf(
            UiStreak("1", "Take pills", 5, 12, LocalDate.now(), 40),
            UiStreak("2", "Water plants", 1, 3, LocalDate.now(), 8),
          )
        ),
        weeklyTrend = (0 until 8).map { WeeklyTrendUi(LocalDate.now().minusWeeks((7 - it).toLong()), (it + 1) * 2) },
        busiestDay = DayOfWeek.MONDAY
      ),
      onBackClick = {}
    )
  }
}
