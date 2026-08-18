package com.github.naz013.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.component.CloudBubble

/**
 * The calendar view-mode switcher for the top app bar: a single icon button that opens a
 * [CloudBubble] (the same speech-bubble used by the Home "+" and note font pickers) listing all
 * [CalendarViewMode]s with the active one highlighted. Shared by the month, day and timeline
 * screens so the switcher looks and behaves identically in every mode.
 */
@Composable
internal fun CalendarModeToggleButton(
  currentMode: CalendarViewMode,
  onModeSelected: (CalendarViewMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  Box(modifier = modifier) {
    MenuIconButton(
      icon = painterResource(R.drawable.ic_fluent_calendar),
      contentDescription = stringResource(R.string.calendar_switch_view_mode),
      onClick = { expanded = true },
    )
    if (expanded) {
      val containerColor = MaterialTheme.colorScheme.surfaceContainer
      val contentColor = MaterialTheme.colorScheme.onSurface
      CloudBubble(
        onDismissRequest = { expanded = false },
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = Modifier.width(200.dp),
      ) {
        Column {
          CalendarViewMode.entries.forEach { mode ->
            CalendarModeRow(
              icon = mode.icon(),
              label = stringResource(mode.labelRes()),
              selected = mode == currentMode,
              contentColor = contentColor,
              onClick = {
                expanded = false
                onModeSelected(mode)
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CalendarModeRow(
  icon: Painter,
  label: String,
  selected: Boolean,
  contentColor: Color,
  onClick: () -> Unit,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.small)
        .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 12.dp),
  ) {
    val rowContentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else contentColor
    Icon(
      painter = icon,
      contentDescription = null,
      tint = rowContentColor,
      modifier = Modifier.size(20.dp),
    )
    Text(
      text = label,
      color = rowContentColor,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
      modifier = Modifier.weight(1f),
    )
    if (selected) {
      Icon(
        painter = painterResource(R.drawable.ic_fluent_checkmark),
        contentDescription = null,
        tint = rowContentColor,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

private fun CalendarViewMode.labelRes(): Int =
  when (this) {
    CalendarViewMode.MONTH -> R.string.calendar_view_month
    CalendarViewMode.DAY -> R.string.calendar_view_day
    CalendarViewMode.THREE_DAY -> R.string.calendar_view_3_days
    CalendarViewMode.SEVEN_DAY -> R.string.calendar_view_7_days
  }

@Composable
private fun CalendarViewMode.icon(): Painter =
  when (this) {
    CalendarViewMode.MONTH -> AppIcons.Fluent.CalendarMonth
    CalendarViewMode.DAY -> AppIcons.Fluent.CalendarDay
    CalendarViewMode.THREE_DAY -> AppIcons.Fluent.Calendar3Day
    CalendarViewMode.SEVEN_DAY -> AppIcons.Fluent.CalendarWorkWeek
  }
