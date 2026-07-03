package com.elementary.tasks.notes.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.elementary.tasks.notes.preview.reminders.UiNoteAttachedReminder

private val REMINDER_CARD_WIDTH = 300.dp

@Composable
fun PreviewNoteReminderRow(
  reminders: List<UiNoteAttachedReminder>,
  onEditClick: (String) -> Unit,
  onDetachClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  if (reminders.isEmpty()) return
  LazyRow(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(end = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(0.dp)
  ) {
    items(reminders, key = { it.id }) { reminder ->
      PreviewNoteReminderCard(
        reminder = reminder,
        onEditClick = { onEditClick(reminder.id) },
        onDetachClick = { onDetachClick(reminder.id) },
        modifier = Modifier
          .width(REMINDER_CARD_WIDTH)
          .padding(start = 16.dp)
      )
    }
  }
}

@Composable
private fun PreviewNoteReminderCard(
  reminder: UiNoteAttachedReminder,
  onEditClick: () -> Unit,
  onDetachClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = MaterialTheme.shapes.extraSmall,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Row {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_alert),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface
        )
        Column {
          Text(
            text = stringResource(R.string.reminder),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = reminder.summary,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
          reminder.dateTime?.let {
            Text(
              text = it,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentWidth(Alignment.End)
          .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        TextButton(onClick = onEditClick) {
          Text(text = stringResource(R.string.edit))
        }
        OutlinedButton(onClick = onDetachClick) {
          Text(text = stringResource(R.string.detach))
        }
      }
    }
  }
}
