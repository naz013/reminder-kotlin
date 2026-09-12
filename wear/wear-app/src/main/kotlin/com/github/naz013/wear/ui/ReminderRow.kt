package com.github.naz013.wear.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
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
  TitleCard(
    onClick = onComplete,
    title = { Text(reminder.title, maxLines = 2) },
    modifier = modifier.fillMaxWidth(),
    colors = reminder.groupColor?.let { CardDefaults.cardColors(backgroundColor = Color(it)) }
      ?: CardDefaults.cardColors(),
  ) {
    Column {
      formatDueTime(reminder.eventDateTimeMillis)?.let {
        Text(it, style = MaterialTheme.typography.caption2)
      }
      Row(modifier = Modifier.padding(top = 4.dp)) {
        CompactChip(
          onClick = onComplete,
          label = { Text(stringResource(R.string.wear_action_done)) },
          colors = ChipDefaults.primaryChipColors(),
        )
        if (reminder.canSnooze) {
          CompactChip(
            onClick = onSnooze,
            label = { Text(stringResource(R.string.wear_action_snooze)) },
            colors = ChipDefaults.secondaryChipColors(),
            modifier = Modifier.padding(start = 4.dp),
          )
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
