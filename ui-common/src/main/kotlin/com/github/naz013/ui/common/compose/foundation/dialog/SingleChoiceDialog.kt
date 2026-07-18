package com.github.naz013.ui.common.compose.foundation.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R

@Composable
fun SingleChoiceDialog(
  title: String,
  options: List<String>,
  selectedIndex: Int,
  onOptionSelected: (Int) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column(modifier = Modifier.selectableGroup()) {
        options.forEachIndexed { index, option ->
          val selected = index == selectedIndex
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
              Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = { onOptionSelected(index) }, role = Role.RadioButton)
                .padding(vertical = 8.dp),
          ) {
            RadioButton(selected = selected, onClick = null)
            Text(
              text = option,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.padding(start = 8.dp),
            )
          }
        }
      }
    },
    confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
  )
}
