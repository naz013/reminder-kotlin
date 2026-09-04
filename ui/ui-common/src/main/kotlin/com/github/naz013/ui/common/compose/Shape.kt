package com.github.naz013.ui.common.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Named corner-radius shapes shared across screens, consolidating values that were previously
 * hardcoded ad hoc per call site (header nav tiles, split button, search bar).
 */
object AppShapes {
  val tile: Shape = RoundedCornerShape(12.dp)
  val card: Shape = RoundedCornerShape(16.dp)
  val pill: Shape = RoundedCornerShape(28.dp)
}
