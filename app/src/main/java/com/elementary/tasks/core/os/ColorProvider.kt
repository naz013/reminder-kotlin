package com.elementary.tasks.core.os

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.elementary.tasks.core.utils.ThemeProvider

class ColorProvider(
  private val context: Context
) {

  @ColorInt
  fun getColor(@ColorRes color: Int): Int {
    return ContextCompat.getColor(context, color)
  }

  @ColorInt
  fun getHintTextColor(): Int {
    return ThemeProvider.getHintTextColor(context)
  }

  @ColorInt
  fun getTitleTextColor(): Int {
    return ThemeProvider.getTitleTextColor(context)
  }

  @ColorInt
  fun getColorOnSurface(): Int {
    return ThemeProvider.getThemeOnSurfaceColor(context)
  }

  @ColorInt
  fun getColorOnSecondary(): Int {
    return ThemeProvider.getThemeOnSecondaryColor(context)
  }

  @ColorInt
  fun getColorOnSecondaryContainer(): Int {
    return ThemeProvider.getThemeOnSecondaryContainerColor(context)
  }

  @ColorInt
  fun getColorOnBackground(): Int {
    return ThemeProvider.getThemeOnBackgroundColor(context)
  }
}
