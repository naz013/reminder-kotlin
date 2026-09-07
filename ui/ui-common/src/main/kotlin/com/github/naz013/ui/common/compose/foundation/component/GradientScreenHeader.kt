package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.TooltipIconButton
import com.github.naz013.ui.common.compose.withAlpha

private const val HeaderHeight = 64
private const val LeadingSpacer = 16
private const val ChipBackgroundAlpha = 0.25f

/**
 * Leading back/close action row for a screen rendered on top of [AnimatedGradientBackground] —
 * shared by the app's gradient hero screens (cloud services, what's new, pro version, ...).
 */
@Composable
fun GradientScreenHeader(
  onBackClick: () -> Unit,
  contentDescription: String,
  modifier: Modifier = Modifier,
  icon: Painter = AppIcons.Builder.ArrowLeft,
) {
  Row(
    modifier = modifier
      .statusBarsPadding()
      .fillMaxWidth()
      .height(HeaderHeight.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = Modifier.width(LeadingSpacer.dp))
    TooltipIconButton(contentDescription = contentDescription) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.background.withAlpha(ChipBackgroundAlpha)),
      ) {
        Icon(
          painter = icon,
          contentDescription = contentDescription,
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}
