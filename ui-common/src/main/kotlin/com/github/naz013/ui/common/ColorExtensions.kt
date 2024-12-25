package com.github.naz013.ui.common

import androidx.annotation.ColorInt
import androidx.annotation.IntRange

@ColorInt
fun Int.adjustAlpha(@IntRange(from = 0, to = 100) factor: Int): Int {
  val alpha = 255f * (factor.toFloat() / 100f)
  val red = android.graphics.Color.red(this)
  val green = android.graphics.Color.green(this)
  val blue = android.graphics.Color.blue(this)
  return android.graphics.Color.argb(alpha.toInt(), red, green, blue)
}

// Check of opacity of Color
fun Int.isAlmostTransparent(): Boolean {
  return this < 25
}

// Check if Color is Dark
fun Int.isColorDark(): Boolean {
  val darkness = 1 - (
    0.299 * android.graphics.Color.red(this) +
      0.587 * android.graphics.Color.green(this) +
      0.114 * android.graphics.Color.blue(this)
    ) / 255
  return darkness >= 0.5
}
