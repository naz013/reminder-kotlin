package com.github.naz013.ui.common.compose.foundation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.preview.AppScreenSizePreviews

/**
 * Placeholder shown in the detail pane of a two-pane list-detail layout (see
 * `androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy`) before anything has
 * been selected from the list pane. Pass this to a list entry's
 * `ListDetailSceneStrategy.listPane(detailPlaceholder = { DetailPanePlaceholder(...) })` when a
 * feature adopts two-pane - keeps the empty state visually consistent across features instead of
 * each one inventing its own.
 */
@Composable
fun DetailPanePlaceholder(
  text: String,
  modifier: Modifier = Modifier,
  icon: Painter? = null,
) {
  Box(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      if (icon != null) {
        Icon(
          painter = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        )
        Spacer(modifier = Modifier.height(12.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      )
    }
  }
}

@AppScreenSizePreviews
@Composable
private fun DetailPanePlaceholderPreview() {
  AppTheme {
    DetailPanePlaceholder(
      text = "Select an item to see details",
      icon = AppIcons.Fluent.Calendar,
    )
  }
}
