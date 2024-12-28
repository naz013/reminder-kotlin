package com.github.naz013.appwidgets.compose

import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders

private val DarkColorScheme = darkColorScheme(
  primary = PrimaryDark,
  secondary = SecondaryDark,
  tertiary = TertiaryDark
)

private val LightColorScheme = lightColorScheme(
  primary = PrimaryLight,
  secondary = SecondaryLight,
  tertiary = TertiaryLight

  /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
internal fun GlanceAppWidgetTheme(
  content: @Composable () -> Unit
) {
  val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    GlanceTheme.colors
  } else {
    ColorProviders(
      light = LightColorScheme,
      dark = DarkColorScheme
    )
  }

  GlanceTheme(
    colors = colors,
    content = content
  )
}
