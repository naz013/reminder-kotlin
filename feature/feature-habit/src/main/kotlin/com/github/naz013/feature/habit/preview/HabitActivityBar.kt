package com.github.naz013.feature.habit.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val CELL_SIZE = 12.dp
private val CELL_GAP = 3.dp
private val MONTH_ROW_HEIGHT = 16.dp
private val CELL_SHAPE = RoundedCornerShape(2.dp)
private const val DAYS_IN_WEEK = 7
private const val LEVEL_COUNT = 5
private const val MIN_ALPHA = 0.25f

/** One Monday-start column of the bar; [levels] hold 0..4 per day and may be shorter for the current week. */
internal data class UiHabitActivityWeek(
  val monthLabel: String?,
  val levels: List<Int>
)

/**
 * GitHub-contributions-style grid: weeks are columns, weekdays are rows. Shows as many of the newest
 * weeks as fit the available width.
 */
@Composable
internal fun HabitActivityBar(
  modifier: Modifier = Modifier,
  weeks: List<UiHabitActivityWeek>,
  activeColor: Color,
  summary: String,
  lessLabel: String,
  moreLabel: String,
  emptyColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
  Column(modifier = modifier.fillMaxWidth().semantics { contentDescription = summary }) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
      val visibleWeeks = weeks.takeLast(fittingColumns(maxWidth))
      Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
        visibleWeeks.forEach { week ->
          WeekColumn(week = week, activeColor = activeColor, emptyColor = emptyColor)
        }
      }
    }
    Legend(
      modifier = Modifier.padding(top = 8.dp).align(Alignment.End),
      activeColor = activeColor,
      emptyColor = emptyColor,
      lessLabel = lessLabel,
      moreLabel = moreLabel,
    )
  }
}

private fun fittingColumns(width: Dp): Int {
  return ((width + CELL_GAP) / (CELL_SIZE + CELL_GAP)).toInt().coerceAtLeast(1)
}

@Composable
private fun WeekColumn(
  week: UiHabitActivityWeek,
  activeColor: Color,
  emptyColor: Color,
) {
  Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
    Column(modifier = Modifier.width(CELL_SIZE).height(MONTH_ROW_HEIGHT - CELL_GAP)) {
      week.monthLabel?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          softWrap = false,
          modifier = Modifier.wrapContentWidth(align = Alignment.Start, unbounded = true),
        )
      }
    }
    repeat(DAYS_IN_WEEK) { row ->
      val level = week.levels.getOrNull(row)
      if (level == null) {
        Spacer(modifier = Modifier.size(CELL_SIZE))
      } else {
        Cell(color = levelColor(level, activeColor, emptyColor))
      }
    }
  }
}

@Composable
private fun Cell(color: Color) {
  Spacer(modifier = Modifier.size(CELL_SIZE).background(color, CELL_SHAPE))
}

@Composable
private fun Legend(
  modifier: Modifier,
  activeColor: Color,
  emptyColor: Color,
  lessLabel: String,
  moreLabel: String,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
  ) {
    Text(
      text = lessLabel,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(end = 4.dp),
    )
    repeat(LEVEL_COUNT) { Cell(color = levelColor(it, activeColor, emptyColor)) }
    Text(
      text = moreLabel,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 4.dp),
    )
  }
}

private fun levelColor(level: Int, activeColor: Color, emptyColor: Color): Color {
  if (level <= 0) return emptyColor
  val steps = LEVEL_COUNT - 1
  val alpha = MIN_ALPHA + (1f - MIN_ALPHA) * (level.coerceAtMost(steps) - 1) / (steps - 1)
  return activeColor.copy(alpha = alpha)
}
