package com.github.naz013.ui.common.compose.foundation.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.github.naz013.ui.common.R

@Composable
fun SeekValueDialog(
  title: String,
  value: Int,
  valueText: String,
  valueRange: ClosedFloatingPointRange<Float>,
  onValueChange: (Int) -> Unit,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  description: String? = null,
  valueTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
  steps: Int = 0,
  hapticFeedbackEnabled: Boolean = false,
  confirmText: String = stringResource(R.string.ok),
) {
  val hapticFeedback = LocalHapticFeedback.current

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column {
        if (description != null) {
          Text(text = description, style = MaterialTheme.typography.titleSmall)
        }
        Text(text = valueText, style = valueTextStyle)
        Slider(
          value = value.toFloat(),
          onValueChange = { newValue ->
            if (newValue.toInt() != value && hapticFeedbackEnabled) {
              hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            onValueChange(newValue.toInt())
          },
          valueRange = valueRange,
          steps = steps,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
  )
}
