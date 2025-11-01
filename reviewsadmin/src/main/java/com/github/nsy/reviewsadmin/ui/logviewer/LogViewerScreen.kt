package com.github.nsy.reviewsadmin.ui.logviewer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.nsy.reviewsadmin.R
import org.koin.androidx.compose.koinViewModel

/**
 * Screen displaying log file content in a prettified way.
 *
 * @param logFileUrl The URL of the log file to download and display
 * @param onNavigateBack Callback when back button is pressed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(
  logFileUrl: String,
  onNavigateBack: () -> Unit,
  viewModel: LogViewerViewModel = koinViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(logFileUrl) {
    viewModel.loadLogFile(logFileUrl)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Log Viewer") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.back)
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
      is LogViewerUiState.Loading -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Downloading and extracting logs...",
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }
      is LogViewerUiState.Success -> {
        LogContent(
          logContent = state.logContent,
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        )
      }
      is LogViewerUiState.Error -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
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
            Button(onClick = onNavigateBack) {
              Text("Go Back")
            }
          }
        }
      }
    }
  }
}

/**
 * Displays the log content in a scrollable, monospaced text view.
 */
@Composable
private fun LogContent(
  logContent: String,
  modifier: Modifier = Modifier
) {
  val verticalScrollState = rememberScrollState()
  val horizontalScrollState = rememberScrollState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(verticalScrollState)
      .horizontalScroll(horizontalScrollState)
      .padding(16.dp)
  ) {
    Text(
      text = logContent,
      fontFamily = FontFamily.Monospace,
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      lineHeight = 16.sp,
      modifier = Modifier.fillMaxWidth()
    )
  }
}
