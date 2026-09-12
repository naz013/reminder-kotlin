package com.github.naz013.wear.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.github.naz013.wear.R
import com.github.naz013.wear.sync.WearActionSender
import com.github.naz013.wearsync.WearReminderSummary
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun WearApp(reminders: StateFlow<List<WearReminderSummary>>, modifier: Modifier = Modifier) {
  val items by reminders.collectAsState()
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  MaterialTheme {
    if (items.isEmpty()) {
      EmptyState(modifier = modifier)
    } else {
      ScalingLazyColumn(modifier = modifier.fillMaxSize()) {
        items(items, key = { it.uuId }) { reminder ->
          ReminderRow(
            reminder = reminder,
            onComplete = {
              scope.launch { WearActionSender.sendComplete(context, reminder.uuId) }
            },
            onSnooze = {
              scope.launch { WearActionSender.sendSnooze(context, reminder.uuId) }
            },
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(
      text = stringResource(R.string.wear_empty_state),
      modifier = Modifier.padding(16.dp),
    )
  }
}
