package com.github.naz013.appwidgets

import android.content.Context
import android.os.Build
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.compose.primaryContainerLight

internal object WidgetUtils {

  private const val TAG = "WidgetUtils"

  /**
   * Sentinel palette index (one past the fixed 0-13 palette) meaning "use the launcher's dynamic
   * Material You color" instead of one of the fixed colors. Handled entirely at the Glance
   * rendering layer (see `roundedBackground`/`paletteContrastColor`) - the legacy color/contrast
   * lookups below never need to special-case it since their `else` branches already handle any
   * out-of-range index safely.
   */
  const val DYNAMIC_COLOR_INDEX = 14

  /**
   * A representative color for the dynamic-color swatch in the widget config color pickers.
   * Purely cosmetic (the picker is plain Compose, not Glance, so it can't read GlanceTheme) -
   * actual widget rendering resolves the real per-widget dynamic color via GlanceTheme.
   */
  fun getDynamicPreviewColor(context: Context): Color {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      dynamicLightColorScheme(context).primary
    } else {
      primaryContainerLight
    }
  }

  fun getComposeColor(code: Int): Color {
    return when (code) {
      0 -> Color.Transparent
      1 -> Color.hsl(hue = 180f, saturation = 1f, lightness = 1f, alpha = 0.25f)
      2 -> Color.hsl(hue = 180f, saturation = 1f, lightness = 1f, alpha = 0.50f)
      3 -> Color.hsl(hue = 180f, saturation = 1f, lightness = 1f, alpha = 0.75f)
      4 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 1f)
      5 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.97f)
      6 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.94f)
      7 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.90f)
      8 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.75f)
      9 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.60f)
      10 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.45f)
      11 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.30f)
      12 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0.15f)
      13 -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0f)
      else -> Color.hsl(hue = 0f, saturation = 0f, lightness = 0f)
    }
  }

  fun getContrastColor(code: Int): Color {
    return getContrastColor(getComposeColor(code))
  }

  fun getContrastColor(color: Color): Color {
    val luminance = color.luminance()
    Logger.d(TAG, "Get contrast color for luminance: $luminance")
    return Color.hsl(
      hue = 0f,
      saturation = 0f,
      lightness = if (luminance > 0.5f) 0.1f else 0.9f
    )
  }
}
