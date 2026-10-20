package com.github.naz013.feature.reminder.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.IconPicker
import com.github.naz013.ui.routine.RoutineColorPicker
import com.github.naz013.ui.routine.RoutineIconSet

/**
 * Shown in [com.github.naz013.feature.reminder.build.BuildReminderScreen] to pick the optional
 * icon and tag color a reminder gets in lists and the calendar. A null [colorIndex] means no color.
 */
@Composable
internal fun AppearanceRow(
  modifier: Modifier = Modifier,
  iconIndex: Int?,
  colorIndex: Int?,
  colors: List<Color>,
  hapticFeedbackEnabled: Boolean,
  onIconSelected: (Int?) -> Unit,
  onColorSelected: (Int) -> Unit,
  onColorCleared: () -> Unit,
) {
  Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconPicker(
        icons = RoutineIconSet.ALL,
        selectedIndex = iconIndex,
        onIconSelected = onIconSelected,
      )
      Spacer(modifier = Modifier.width(16.dp))
      Text(
        text = stringResource(R.string.reminder_icon_and_color),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.weight(1f),
      )
      if (colorIndex != null) {
        TextButton(onClick = onColorCleared) {
          Text(text = stringResource(R.string.remove_color))
        }
      }
    }
    RoutineColorPicker(
      colors = colors,
      selectedIndex = colorIndex ?: 0,
      onColorSelected = onColorSelected,
      hapticFeedbackEnabled = hapticFeedbackEnabled,
      modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    )
  }
}
