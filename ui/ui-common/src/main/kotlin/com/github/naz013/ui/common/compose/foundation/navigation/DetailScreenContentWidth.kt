package com.github.naz013.ui.common.compose.foundation.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.foundation.isDesktopScreen
import com.github.naz013.ui.common.compose.foundation.isTabletScreen

private val DETAIL_SCREEN_MAX_WIDTH = 640.dp

/**
 * Caps a reminder/birthday-style detail screen's content to a comfortable reading width on
 * Medium+ width (tablet/desktop) instead of letting it stretch edge-to-edge across a wide
 * two-pane detail pane. No-op on Compact width, where the screen already renders full phone-width.
 * The parent must center its child itself (e.g. `Box(contentAlignment = Alignment.TopCenter)`) -
 * this only bounds the width, matching how `fillMaxWidth()` alone never centers either.
 */
@Composable
fun Modifier.detailScreenContentWidth(): Modifier =
  if (isTabletScreen() || isDesktopScreen()) {
    widthIn(max = DETAIL_SCREEN_MAX_WIDTH).fillMaxWidth()
  } else {
    fillMaxWidth()
  }
