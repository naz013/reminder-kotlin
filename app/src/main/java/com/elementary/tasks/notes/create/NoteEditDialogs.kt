package com.elementary.tasks.notes.create

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.elementary.tasks.R

@Composable
fun DeleteNoteDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    text = { Text(stringResource(R.string.delete_this_note)) },
    confirmButton = {
      TextButton(onClick = {
        onConfirm()
        onDismiss()
      }) {
        Text(stringResource(R.string.yes))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.no))
      }
    },
  )
}

@Composable
fun SameNoteDialog(
  onDismiss: () -> Unit,
  onKeep: () -> Unit,
  onReplace: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    text = { Text(stringResource(R.string.same_note_message)) },
    confirmButton = {
      TextButton(onClick = {
        onKeep()
        onDismiss()
      }) {
        Text(stringResource(R.string.keep))
      }
    },
    dismissButton = {
      Row {
        TextButton(onClick = {
          onReplace()
          onDismiss()
        }) {
          Text(stringResource(R.string.replace))
        }
        TextButton(onClick = onDismiss) {
          Text(stringResource(R.string.cancel))
        }
      }
    },
  )
}
