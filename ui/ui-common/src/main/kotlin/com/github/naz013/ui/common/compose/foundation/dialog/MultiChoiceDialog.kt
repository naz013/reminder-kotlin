package com.github.naz013.ui.common.compose.foundation.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R

/** Same shape as [SingleChoiceDialog] but for a set of independently toggleable options - unlike
 *  single-choice, a change here needs an explicit confirm since there's no single "the" selection
 *  to auto-dismiss on. */
@Composable
fun MultiChoiceDialog(
  title: String,
  options: List<String>,
  selectedIndices: Set<Int>,
  onOptionToggled: (Int) -> Unit,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column {
        options.forEachIndexed { index, option ->
          val checked = index in selectedIndices
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
              Modifier
                .fillMaxWidth()
                .toggleable(value = checked, onValueChange = { onOptionToggled(index) }, role = Role.Checkbox)
                .padding(vertical = 8.dp),
          ) {
            Checkbox(checked = checked, onCheckedChange = null)
            Text(
              text = option,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.padding(start = 8.dp),
            )
          }
        }
      }
    },
    confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.ok)) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
  )
}
