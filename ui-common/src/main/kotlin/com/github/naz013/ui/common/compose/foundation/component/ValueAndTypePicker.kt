package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppTheme

/**
 * A number entry ([NumberStepperField]) paired with a unit/type wheel ([WheelPicker]) in the same
 * row, e.g. "5 [minutes/hours/days]". This is the Compose replacement for the legacy
 * `ValueAndTypePickerView` composite `LinearLayout`, used by before-time, repeat-interval and
 * countdown-style builder items.
 *
 * @param value Current numeric value.
 * @param onValueChange Invoked with the clamped new numeric value.
 * @param typeItems Unit labels for the wheel (e.g. "Minutes", "Hours", "Days").
 * @param selectedTypeIndex Index of the currently selected unit in [typeItems].
 * @param onTypeIndexChange Invoked once the wheel settles on a new unit index.
 * @param enabled When false, both the stepper and the wheel are disabled and dimmed - e.g. for an
 * "all day" toggle that makes the duration irrelevant.
 * @param hapticFeedbackEnabled When true, both the stepper's +/- buttons and the wheel play a
 * tick whenever they actually change the value (see [NumberStepperField] and [WheelPicker]).
 */
@Composable
fun ValueAndTypePicker(
  value: Long,
  onValueChange: (Long) -> Unit,
  typeItems: List<String>,
  selectedTypeIndex: Int,
  onTypeIndexChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
  minValue: Long = 0,
  maxValue: Long = 999,
  enabled: Boolean = true,
  hapticFeedbackEnabled: Boolean = true,
) {
  Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    NumberStepperField(
      value = value,
      onValueChange = onValueChange,
      minValue = minValue,
      maxValue = maxValue,
      enabled = enabled,
      hapticFeedbackEnabled = hapticFeedbackEnabled,
      modifier = Modifier.weight(1f),
    )
    Spacer(modifier = Modifier.width(8.dp))
    WheelPicker(
      items = typeItems,
      selectedIndex = selectedTypeIndex,
      onSelectedIndexChange = onTypeIndexChange,
      enabled = enabled,
      hapticFeedbackEnabled = hapticFeedbackEnabled,
      modifier = Modifier.weight(1f),
    )
  }
}

@Preview(showBackground = true, name = "Value and type picker")
@Composable
private fun PreviewValueAndTypePicker() {
  AppTheme {
    val types = remember { listOf("Minutes", "Hours", "Days") }
    ValueAndTypePicker(
      value = 15,
      onValueChange = {},
      typeItems = types,
      selectedTypeIndex = 0,
      onTypeIndexChange = {},
    )
  }
}
