package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppTheme

/**
 * A numeric stepper: minus/plus buttons around a directly-editable text field, clamped to
 * [minValue]..[maxValue]. This is the Compose replacement for the legacy `NumberValuePickerView`
 * custom `LinearLayout`.
 *
 * @param value Current value.
 * @param onValueChange Invoked with the clamped new value, from either button or direct typing.
 * @param step Amount the minus/plus buttons add or subtract per tap.
 */
@Composable
fun NumberStepperField(
  value: Long,
  onValueChange: (Long) -> Unit,
  modifier: Modifier = Modifier,
  minValue: Long = 0,
  maxValue: Long = 999,
  step: Long = 1,
  enabled: Boolean = true,
) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    IconButton(
      onClick = { onValueChange((value - step).coerceIn(minValue, maxValue)) },
      enabled = enabled && value > minValue,
    ) {
      Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
    }

    OutlinedTextField(
      value = value.toString(),
      onValueChange = { text ->
        val parsed = text.filter { it.isDigit() }.toLongOrNull()
        if (parsed != null) {
          onValueChange(parsed.coerceIn(minValue, maxValue))
        } else if (text.isEmpty()) {
          onValueChange(minValue)
        }
      },
      modifier = Modifier.width(96.dp),
      enabled = enabled,
      singleLine = true,
      textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )

    IconButton(
      onClick = { onValueChange((value + step).coerceIn(minValue, maxValue)) },
      enabled = enabled && value < maxValue,
    ) {
      Icon(imageVector = Icons.Filled.Add, contentDescription = null)
    }
  }
}

@Preview(showBackground = true, name = "Number stepper field")
@Composable
private fun PreviewNumberStepperField() {
  AppTheme {
    NumberStepperField(
      value = 5,
      onValueChange = {},
    )
  }
}
