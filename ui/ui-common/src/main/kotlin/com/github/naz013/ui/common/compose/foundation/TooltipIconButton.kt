package com.github.naz013.ui.common.compose.foundation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
  modifier: Modifier = Modifier,
  contentDescription: String?,
  content: @Composable () -> Unit,
) {
  if (contentDescription.isNullOrBlank()) {
    content()
    return
  }
  TooltipBox(
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
    tooltip = { PlainTooltip { Text(contentDescription) } },
    state = rememberTooltipState(),
    modifier = modifier,
    content = content,
  )
}
