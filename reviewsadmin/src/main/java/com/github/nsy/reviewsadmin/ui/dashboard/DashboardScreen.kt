package com.github.nsy.reviewsadmin.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.naz013.reviews.AppSource
import com.github.nsy.reviewsadmin.R
import org.koin.androidx.compose.koinViewModel

/**
 * Main dashboard screen displaying review statistics.
 *
 * Shows total reviews by app source with period statistics for each source.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  onSignOut: () -> Unit,
  onAppSourceClick: (AppSource) -> Unit,
  viewModel: DashboardViewModel = koinViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.dashboard_title)) },
        actions = {
          IconButton(onClick = { viewModel.loadData() }) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = stringResource(R.string.refresh)
            )
          }
          IconButton(onClick = onSignOut) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ExitToApp,
              contentDescription = stringResource(R.string.sign_out)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )
    }
  ) { paddingValues ->
    when (val state = uiState) {
      is DashboardUiState.Loading -> {
        Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      }
      is DashboardUiState.Success -> {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
          // Show statistics for each app source
          items(state.data.reviewsBySource) { sourceData ->
            AppSourceSection(
              sourceData = sourceData,
              onTotalReviewsClick = { onAppSourceClick(sourceData.source) }
            )
          }
        }
      }
      is DashboardUiState.Error -> {
        Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
          ) {
            Text(
              text = state.message,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.loadData() }) {
              Text(stringResource(R.string.retry))
            }
          }
        }
      }
    }
  }
}

/**
 * Section displaying all statistics for a specific app source.
 */
@Composable
private fun AppSourceSection(
  sourceData: AppSourceData,
  onTotalReviewsClick: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // App source header with total count
    Text(
      text = "${sourceData.sourceName} Version",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary
    )

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onTotalReviewsClick() },
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(R.string.total_reviews),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
          if (sourceData.newReviewsCount > 0) {
            Surface(
              color = MaterialTheme.colorScheme.secondary,
              shape = MaterialTheme.shapes.small
            ) {
              Text(
                text = stringResource(R.string.new_reviews_count, sourceData.newReviewsCount),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = stringResource(R.string.review_count),
              style = MaterialTheme.typography.bodyMedium
            )
            Text(
              text = sourceData.count.toString(),
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = stringResource(R.string.average_rating),
              style = MaterialTheme.typography.bodyMedium
            )
            Text(
              text = String.format("%.2f", sourceData.averageRating),
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Period statistics for this source
    Text(
      text = stringResource(R.string.statistics_by_period),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(top = 8.dp)
    )

    sourceData.periodStatistics.forEach { periodStats ->
      PeriodStatisticsCard(periodStats)
    }

    // Add divider between sources
    HorizontalDivider(
      modifier = Modifier.padding(vertical = 8.dp),
      thickness = 2.dp,
      color = MaterialTheme.colorScheme.outlineVariant
    )
  }
}

/**
 * Card displaying statistics for a specific time period.
 */
@Composable
private fun PeriodStatisticsCard(stats: PeriodStatistics) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(
        text = stringResource(R.string.last_n_days, stats.days),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSecondaryContainer
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = stringResource(R.string.average_rating),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
          Text(
            text = String.format("%.2f", stats.averageRating),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = stringResource(R.string.review_count),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
          Text(
            text = stats.count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
      }
    }
  }
}
