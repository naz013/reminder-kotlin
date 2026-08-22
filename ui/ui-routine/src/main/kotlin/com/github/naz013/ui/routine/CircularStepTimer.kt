package com.github.naz013.ui.routine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppTheme

private val DEFAULT_SIZE = 220.dp
private val DEFAULT_STROKE_WIDTH = 10.dp

/**
 * Countdown ring for the focus runner: an M3 determinate [CircularProgressIndicator] that drains
 * as the active step's time elapses, with the remaining time centered inside it. [progress] is the
 * remaining fraction (1f = step just started, 0f = time's up) so the ring visually empties, not
 * fills - the opposite of a typical loading indicator.
 */
@Composable
fun CircularStepTimer(
  progress: Float,
  timeLabel: String,
  modifier: Modifier = Modifier,
  size: Dp = DEFAULT_SIZE,
  strokeWidth: Dp = DEFAULT_STROKE_WIDTH,
  color: Color = MaterialTheme.colorScheme.primary,
  trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator(
      progress = { progress.coerceIn(0f, 1f) },
      modifier = Modifier.fillMaxSize(),
      color = color,
      trackColor = trackColor,
      strokeWidth = strokeWidth,
    )
    Text(
      text = timeLabel,
      style = MaterialTheme.typography.displaySmall,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun CircularStepTimerPreview() {
  AppTheme {
    CircularStepTimer(
      progress = 0.65f,
      timeLabel = "04:32",
    )
  }
}
