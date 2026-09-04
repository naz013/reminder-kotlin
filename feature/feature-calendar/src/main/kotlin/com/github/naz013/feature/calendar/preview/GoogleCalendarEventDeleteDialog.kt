package com.github.naz013.feature.calendar.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R

@Composable
internal fun GoogleCalendarEventDeleteDialog(
  onDeleteLocalOnly: () -> Unit,
  onDeleteFromDeviceCalendarToo: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.delete_calendar_event_title)) },
    text = {
      Column {
        Text(
          text = stringResource(R.string.delete_calendar_event_message),
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDeleteFromDeviceCalendarToo) {
        Text(stringResource(R.string.delete_from_app_and_calendar))
      }
    },
    dismissButton = {
      Column(modifier = Modifier.fillMaxWidth()) {
        TextButton(onClick = onDeleteLocalOnly) {
          Text(stringResource(R.string.delete_from_app_only))
        }
      }
    },
  )
}
