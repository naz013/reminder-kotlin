package com.github.naz013.ui.common.compose.foundation.navigation

import androidx.compose.ui.graphics.painter.Painter

/**
 * A single top-level app destination shown in [AppNavigationScaffold] - rendered as a bottom
 * navigation bar item on compact width, a navigation rail item on medium/expanded width. [key]
 * is whatever the caller's own destination/NavKey type is - this stays feature/navigation
 * agnostic so it can live in ui-common (see the module dependency rules in
 * docs/architecture.md).
 */
data class AppDestination<T>(
  val key: T,
  val icon: Painter,
  val labelRes: Int,
  val selectedIcon: Painter = icon,
  val badgeCount: Int? = null,
)
