package com.github.naz013.ui.googletask

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * The canonical Google Task row: a completion checkbox tinted by the task list's color, title/
 * notes, and a trailing due date. Shared by the Google Tasks list, per-list screens, and Tag
 * Details. Pass `onToggle = null` (e.g. in a read-only browsing context) to show the checkbox's
 * state without making it tappable.
 */
@Composable
fun GoogleTaskRow(
  task: GoogleTaskItemState,
  onClick: () -> Unit,
  onToggle: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  val accentColor = task.taskListColor?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp, horizontal = 12.dp),
    ) {
      Icon(
        painter =
          painterResource(
            if (task.isCompleted) R.drawable.ic_fluent_checkbox_checked else R.drawable.ic_fluent_checkbox_unchecked,
          ),
        contentDescription = null,
        tint = accentColor,
        modifier =
          Modifier
            .size(28.dp)
            .let { if (onToggle != null) it.clickable(onClick = onToggle) else it },
      )
      Column(
        modifier =
          Modifier
            .weight(1f)
            .padding(start = 12.dp),
      ) {
        Text(
          text = task.text,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (!task.notes.isNullOrEmpty()) {
          Text(
            text = task.notes ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      if (!task.dueDate.isNullOrEmpty()) {
        Text(
          text = task.dueDate ?: "",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
