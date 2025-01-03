package com.github.naz013.ui.common.theme

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.github.naz013.common.ContextProvider

class ColorProvider(
  private val contextProvider: ContextProvider
) {

  @ColorInt
  fun getColor(@ColorRes color: Int): Int {
    return ContextCompat.getColor(getContext(), color)
  }

  @ColorInt
  fun getHintTextColor(): Int {
    return ThemeProvider.getHintTextColor(getContext())
  }

  @ColorInt
  fun getTitleTextColor(): Int {
    return ThemeProvider.getTitleTextColor(getContext())
  }

  @ColorInt
  fun getColorOnSurface(): Int {
    return ThemeProvider.getThemeOnSurfaceColor(getContext())
  }

  @ColorInt
  fun getColorOnSecondary(): Int {
    return ThemeProvider.getThemeOnSecondaryColor(getContext())
  }

  @ColorInt
  fun getColorOnSecondaryContainer(): Int {
    return ThemeProvider.getThemeOnSecondaryContainerColor(getContext())
  }

  @ColorInt
  fun getColorOnBackground(): Int {
    return ThemeProvider.getThemeOnBackgroundColor(getContext())
  }

  private fun getContext(): Context = contextProvider.themedContext
}
