package com.github.nsy.reviewsadmin.ui.reviewlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.naz013.reviews.AppSource
import com.github.naz013.reviews.Review
import com.github.nsy.reviewsadmin.R
import org.koin.androidx.compose.koinViewModel
import org.threeten.bp.format.DateTimeFormatter

/**
 * Screen displaying a table view of reviews for a specific app source.
 *
 * @param appSource The app source to display reviews for
 * @param onNavigateBack Callback when back button is pressed
 * @param onReviewClick Callback when a review row is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListScreen(
  appSource: AppSource,
  onNavigateBack: () -> Unit,
  onReviewClick: (Review) -> Unit,
  viewModel: ReviewListViewModel = koinViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(appSource) {
    viewModel.loadReviews(appSource)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("${appSource.name} Reviews") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.back)
            )
          }
        },
        actions = {
          IconButton(onClick = { viewModel.loadReviews(appSource) }) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = stringResource(R.string.refresh)
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
      is ReviewListUiState.Loading -> {
        Box(
          modifier = Modifier.fillMaxSize().padding(paddingValues),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      }
      is ReviewListUiState.Success -> {
        ReviewTable(
          reviews = state.reviews,
          sortColumn = state.sortColumn,
          sortOrder = state.sortOrder,
          onSort = { column -> viewModel.sortBy(column) },
          onReviewClick = onReviewClick,
          modifier = Modifier.fillMaxSize().padding(paddingValues)
        )
      }
      is ReviewListUiState.Error -> {
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
            Button(onClick = { viewModel.loadReviews(appSource) }) {
              Text(stringResource(R.string.retry))
            }
          }
        }
      }
    }
  }
}

/**
 * Displays reviews in a table format with sortable columns.
 */
@Composable
private fun ReviewTable(
  reviews: List<Review>,
  sortColumn: SortColumn,
  sortOrder: SortOrder,
  onSort: (SortColumn) -> Unit,
  onReviewClick: (Review) -> Unit,
  modifier: Modifier = Modifier
) {
  val horizontalScrollState = rememberScrollState()

  LazyColumn(
    modifier = modifier
  ) {
    // Header Row
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(horizontalScrollState)
      ) {
        TableHeaderRow(
          sortColumn = sortColumn,
          sortOrder = sortOrder,
          onSort = onSort
        )
      }
    }

    // Data Rows
    items(reviews) { review ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(horizontalScrollState)
      ) {
        TableDataRow(
          review = review,
          onClick = { onReviewClick(review) }
        )
      }
    }
  }
}

/**
 * Header row with sortable column titles.
 */
@Composable
private fun TableHeaderRow(
  sortColumn: SortColumn,
  sortOrder: SortOrder,
  onSort: (SortColumn) -> Unit
) {
  Row(
    modifier = Modifier
      .width(1000.dp)
      .background(MaterialTheme.colorScheme.primaryContainer)
      .border(1.dp, MaterialTheme.colorScheme.outline)
      .padding(vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    TableHeaderCell(
      text = stringResource(R.string.rating),
      column = SortColumn.RATING,
      currentSortColumn = sortColumn,
      sortOrder = sortOrder,
      onSort = onSort,
      modifier = Modifier.width(70.dp)
    )
    VerticalDivider(
      modifier = Modifier.height(32.dp),
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outline
    )
    TableHeaderCell(
      text = stringResource(R.string.comment),
      column = SortColumn.COMMENT,
      currentSortColumn = sortColumn,
      sortOrder = sortOrder,
      onSort = onSort,
      modifier = Modifier.width(400.dp)
    )
    VerticalDivider(
      modifier = Modifier.height(32.dp),
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outline
    )
    TableHeaderCell(
      text = stringResource(R.string.app_version),
      column = SortColumn.APP_VERSION,
      currentSortColumn = sortColumn,
      sortOrder = sortOrder,
      onSort = onSort,
      modifier = Modifier.width(100.dp)
    )
    VerticalDivider(
      modifier = Modifier.height(32.dp),
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outline
    )
    TableHeaderCell(
      text = stringResource(R.string.timestamp),
      column = SortColumn.TIMESTAMP,
      currentSortColumn = sortColumn,
      sortOrder = sortOrder,
      onSort = onSort,
      modifier = Modifier.width(150.dp)
    )
    VerticalDivider(
      modifier = Modifier.height(32.dp),
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outline
    )
    TableHeaderCell(
      text = stringResource(R.string.has_logs),
      column = SortColumn.HAS_LOGS,
      currentSortColumn = sortColumn,
      sortOrder = sortOrder,
      onSort = onSort,
      modifier = Modifier.width(100.dp)
    )
    VerticalDivider(
      modifier = Modifier.height(32.dp),
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outline
    )
    TableHeaderCell(
      text = stringResource(R.string.locale),
      column = SortColumn.LOCALE,
      currentSortColumn = sortColumn,
      sortOrder = sortOrder,
      onSort = onSort,
      modifier = Modifier.width(80.dp)
    )
  }
}

/**
 * Individual header cell with sorting indicator.
 */
@Composable
private fun TableHeaderCell(
  text: String,
  column: SortColumn,
  currentSortColumn: SortColumn,
  sortOrder: SortOrder,
  onSort: (SortColumn) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clickable { onSort(column) }
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onPrimaryContainer
    )
    if (currentSortColumn == column) {
      Icon(
        imageVector = if (sortOrder == SortOrder.ASCENDING) {
          Icons.Default.ArrowDropUp
        } else {
          Icons.Default.ArrowDropDown
        },
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onPrimaryContainer
      )
    }
  }
}

/**
 * Data row displaying a single review.
 */
@Composable
private fun TableDataRow(
  review: Review,
  onClick: () -> Unit
) {
  val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  val backgroundColor = if (review.processed) {
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
  } else {
    MaterialTheme.colorScheme.surface
  }
  val contentAlpha = if (review.processed) 0.6f else 1f

  Row(
    modifier = Modifier
      .width(1000.dp)
      .background(backgroundColor)
      .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
      .padding(vertical = 12.dp)
      .clickable(onClick = onClick),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Rating
    Box(
      modifier = Modifier
        .width(70.dp)
        .padding(horizontal = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = String.format("%.1f", review.rating),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
      )
    }

    VerticalDivider(
      modifier = Modifier.height(40.dp),
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant
    )

    // Comment
    Box(
      modifier = Modifier
        .width(400.dp)
        .padding(horizontal = 8.dp)
    ) {
      Text(
        text = review.comment,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
      )
    }

    VerticalDivider(
      modifier = Modifier.height(40.dp),
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant
    )

    // App Version
    Box(
      modifier = Modifier
        .width(100.dp)
        .padding(horizontal = 8.dp)
    ) {
      Text(
        text = review.appVersion,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
      )
    }

    VerticalDivider(
      modifier = Modifier.height(40.dp),
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant
    )

    // Timestamp
    Box(
      modifier = Modifier
        .width(150.dp)
        .padding(horizontal = 8.dp)
    ) {
      Text(
        text = review.timestamp.format(dateFormatter),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
      )
    }

    VerticalDivider(
      modifier = Modifier.height(40.dp),
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant
    )

    // Has Logs Flag
    Box(
      modifier = Modifier
        .width(100.dp)
        .padding(horizontal = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (review.logFileUrl != null) {
          Icons.Default.Check
        } else {
          Icons.Default.Close
        },
        contentDescription = if (review.logFileUrl != null) "Has logs" else "No logs",
        tint = if (review.logFileUrl != null) {
          MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha)
        } else {
          MaterialTheme.colorScheme.error.copy(alpha = contentAlpha)
        },
        modifier = Modifier.size(20.dp)
      )
    }

    VerticalDivider(
      modifier = Modifier.height(40.dp),
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outlineVariant
    )

    // Locale
    Box(
      modifier = Modifier
        .width(80.dp)
        .padding(horizontal = 8.dp)
    ) {
      Text(
        text = review.userLocale,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
      )
    }
  }
}
