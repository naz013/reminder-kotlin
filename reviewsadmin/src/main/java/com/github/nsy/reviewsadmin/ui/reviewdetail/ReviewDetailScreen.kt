package com.github.nsy.reviewsadmin.ui.reviewdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.naz013.reviews.Review
import com.github.nsy.reviewsadmin.R
import org.koin.androidx.compose.koinViewModel
import org.threeten.bp.format.DateTimeFormatter

/**
 * Screen displaying detailed information about a review.
 *
 * @param review The review to display
 * @param onNavigateBack Callback when back button is pressed
 * @param onViewLogs Callback when View Logs button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetailScreen(
  review: Review,
  onNavigateBack: () -> Unit,
  onViewLogs: (String) -> Unit = {},
  viewModel: ReviewDetailViewModel = koinViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  val currentReview by viewModel.currentReview.collectAsState()

  // Initialize the ViewModel with the review
  LaunchedEffect(review.id) {
    viewModel.setReview(review)
  }

  // Use currentReview if available, otherwise use the passed review
  val displayReview = currentReview ?: review

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Review Details") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.back)
            )
          }
        },
        actions = {
          IconButton(onClick = { viewModel.refreshReview(review.id) }) {
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
    },
    bottomBar = {
      BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Delete Button
          Button(
            onClick = { viewModel.deleteReview(displayReview) },
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error
            )
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete")
          }

          // Done/Undone Button
          Button(
            onClick = { viewModel.toggleProcessed(displayReview) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (displayReview.processed) {
                MaterialTheme.colorScheme.secondary
              } else {
                MaterialTheme.colorScheme.primary
              }
            )
          ) {
            Icon(
              imageVector = if (displayReview.processed) Icons.Default.Close else Icons.Default.Check,
              contentDescription = if (displayReview.processed) "Mark as Undone" else "Mark as Done",
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (displayReview.processed) "Mark Undone" else "Mark Done")
          }
        }
      }
    }
  ) { paddingValues ->
    when (val state = uiState) {
      is ReviewDetailUiState.Idle -> {
        ReviewDetailContent(
          review = displayReview,
          onViewLogs = onViewLogs,
          modifier = Modifier.fillMaxSize().padding(paddingValues)
        )
      }
      is ReviewDetailUiState.Deleted -> {
        // Navigate back after deletion
        onNavigateBack()
      }
      is ReviewDetailUiState.Error -> {
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
          }
        }
      }
    }
  }
}

/**
 * Displays the detailed content of a review.
 */
@Composable
private fun ReviewDetailContent(
  review: Review,
  onViewLogs: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  Column(
    modifier = modifier
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // Status Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = if (review.processed) {
          MaterialTheme.colorScheme.surfaceVariant
        } else {
          MaterialTheme.colorScheme.primaryContainer
        }
      )
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Status:",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = if (review.processed) "Processed" else "Pending",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (review.processed) {
            MaterialTheme.colorScheme.secondary
          } else {
            MaterialTheme.colorScheme.primary
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Rating
    DetailField(
      label = "Rating",
      value = String.format("%.1f ⭐", review.rating)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Comment
    DetailField(
      label = "Comment",
      value = review.comment
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Timestamp
    DetailField(
      label = "Timestamp",
      value = review.timestamp.format(dateFormatter)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // App Version
    DetailField(
      label = "App Version",
      value = review.appVersion
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Source
    DetailField(
      label = "Source",
      value = review.source.name
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Locale
    DetailField(
      label = "Locale",
      value = review.userLocale
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Device Info
    DetailField(
      label = "Device Info",
      value = review.deviceInfo
    )

    Spacer(modifier = Modifier.height(16.dp))

    // User ID
    DetailField(
      label = "User ID",
      value = review.userId.ifEmpty { "N/A" }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // User Email with Write Email button
    val context = LocalContext.current
    val userEmail = review.userEmail
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Text(
          text = "User Email",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = userEmail ?: "N/A",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (!userEmail.isNullOrEmpty()) {
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = {
              val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$userEmail")
              }
              context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Write an Email")
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Log File URL
    val logUrl = review.logFileUrl
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Text(
          text = "Log File",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (logUrl != null) {
          Button(
            onClick = { onViewLogs(logUrl) },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("View Logs")
          }
        } else {
          Text(
            text = "Not Available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Review ID
    DetailField(
      label = "Review ID",
      value = review.id
    )
  }
}

/**
 * Displays a single detail field with label and value.
 */
@Composable
private fun DetailField(
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
