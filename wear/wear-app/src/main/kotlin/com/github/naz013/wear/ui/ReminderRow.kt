package com.github.naz013.wear.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Text
import com.github.naz013.wear.R
import com.github.naz013.wearsync.WearReminderSummary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun ReminderRow(
  reminder: WearReminderSummary,
  onComplete: () -> Unit,
  onSnooze: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(onClick = onComplete, modifier = modifier.fillMaxWidth()) {
    Column {
      Text(reminder.title, maxLines = 2)
      formatDueTime(reminder.eventDateTimeMillis)?.let { Text(it) }
      Row(modifier = Modifier.padding(top = 4.dp)) {
        Button(onClick = onComplete) { Text(stringResource(R.string.wear_action_done)) }
        if (reminder.canSnooze) {
          CompactButton(onClick = onSnooze, modifier = Modifier.padding(start = 4.dp)) {
            Text(stringResource(R.string.wear_action_snooze))
          }
        }
      }
    }
  }
}

private fun formatDueTime(eventDateTimeMillis: Long?): String? {
  if (eventDateTimeMillis == null) return null
  val dateTime = Instant.ofEpochMilli(eventDateTimeMillis).atZone(ZoneId.systemDefault())
  return dateTime.format(DateTimeFormatter.ofPattern("EEE, HH:mm"))
}
