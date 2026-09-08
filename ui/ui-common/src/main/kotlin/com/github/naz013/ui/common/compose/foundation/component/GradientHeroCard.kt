package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppShapes
import com.github.naz013.ui.common.compose.withAlpha

private const val CardFillAlpha = 0.85f
private const val ContentPadding = 20

/**
 * Translucent content card used on top of [AnimatedGradientBackground] — shared by the app's
 * gradient hero screens (cloud services, what's new, pro version, ...).
 */
@Composable
fun GradientHeroCard(
  modifier: Modifier = Modifier,
  verticalArrangement: Arrangement.Vertical = Arrangement.Top,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    shape = AppShapes.largeIncreased,
    color = MaterialTheme.colorScheme.surface.withAlpha(CardFillAlpha),
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(ContentPadding.dp),
      verticalArrangement = verticalArrangement,
      content = content,
    )
  }
}
