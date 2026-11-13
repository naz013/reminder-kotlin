package com.github.naz013.appwidgets.routine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.compose.WidgetConfigScaffold
import com.github.naz013.ui.common.compose.AppTheme

@Composable
internal fun RoutineWidgetConfigScreen(
  modifier: Modifier = Modifier,
  state: RoutineWidgetConfigState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onRoutineSelected: (String?) -> Unit
) {
  WidgetConfigScaffold(
    title = stringResource(R.string.routines),
    onBackClick = onBackClick,
    onSaveClick = onSaveClick,
    modifier = modifier,
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
      RoutineOptionRow(
        title = stringResource(R.string.widget_routine_next_scheduled),
        iconRes = null,
        color = null,
        isSelected = state.selectedRoutineId == null,
        onClick = { onRoutineSelected(null) }
      )
      state.routines.forEach { option ->
        RoutineOptionRow(
          title = option.name,
          iconRes = option.iconRes,
          color = Color(option.color),
          isSelected = state.selectedRoutineId == option.id,
          onClick = { onRoutineSelected(option.id) }
        )
      }
    }
    if (state.routines.isEmpty()) {
      Text(
        text = stringResource(R.string.no_routines),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 16.dp)
      )
    }
  }
}

@Composable
private fun RoutineOptionRow(
  modifier: Modifier = Modifier,
  title: String,
  iconRes: Int?,
  color: Color?,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    RadioButton(selected = isSelected, onClick = onClick)
    if (iconRes != null) {
      Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = color ?: MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 4.dp).size(24.dp)
      )
    }
    Text(
      text = title,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.padding(start = 12.dp)
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun RoutineWidgetConfigScreenPreview() {
  AppTheme {
    RoutineWidgetConfigScreen(
      state = RoutineWidgetConfigState(
        routines = listOf(UiRoutineWidgetOption("1", "Morning routine", null, 0xFF86E3CE.toInt()))
      ),
      onBackClick = {},
      onSaveClick = {},
      onRoutineSelected = {}
    )
  }
}
