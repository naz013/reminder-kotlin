package com.github.naz013.appwidgets.compose

import android.content.Context
import android.os.Build
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.glance.GlanceTheme
import androidx.glance.unit.ColorProvider
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.compose.primaryContainerLight
import kotlin.math.max
import kotlin.math.min

internal class ComposeResourceProvider(
  private val context: Context,
) {

  /**
   * A representative color for the dynamic-color swatch in the widget config color pickers.
   * Purely cosmetic (the picker is plain Compose, not Glance, so it can't read GlanceTheme) -
   * actual widget rendering resolves the real per-widget dynamic color via GlanceTheme.
   */
  fun getDynamicPreviewColor(): Color {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      dynamicLightColorScheme(context).primaryContainer
    } else {
      primaryContainerLight
    }
  }

  @Composable
  fun getDynamicContrastColor(): ColorProvider {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      GlanceTheme.colors.onPrimaryContainer
    } else {
      val foregroundColor = bestForegroundColor(primaryContainerLight)
      androidx.glance.color.ColorProvider(day = foregroundColor, night = foregroundColor)
    }
  }

  fun getBackgroundColors(): List<Color> {
    return (0..13).map { getComposeColor(it) } + getDynamicPreviewColor()
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

  @Composable
  fun getColors(code: Int): ColorGroup {
    val background = if (code == DYNAMIC_COLOR_INDEX) {
      getDynamicPreviewColor()
    } else {
      getComposeColor(code = code)
    }
    Logger.d(TAG, "Get WIDGET colors for index = $code")
    return ColorGroup(
      background = background,
      foreground = if (code == WidgetUtils.DYNAMIC_COLOR_INDEX) {
        getDynamicContrastColor()
      } else {
        val fallback = bestForegroundColor(background)
        androidx.glance.color.ColorProvider(day = fallback, night = fallback)
      },
    )
  }

  fun bestForegroundColor(
    background: Color,
    candidates: List<Color> = listOf(Color.Black, Color.White),
    minimumRatio: Float = 4.5f  // 3f for large text/UI components, 7f for AAA
  ): Color =
    candidates.maxBy { contrastRatio(background, it) }
      .let { best -> if (contrastRatio(background, best) >= minimumRatio) best else best }

  private fun contrastRatio(a: Color, b: Color): Float {
    val l1 = a.luminance() + 0.05f
    val l2 = b.luminance() + 0.05f
    return max(l1, l2) / min(l1, l2)
  }

  data class ColorGroup(
    val background: Color,
    val foreground: ColorProvider,
  )

  companion object {
    private const val TAG = "WidgetUtils"
    const val DYNAMIC_COLOR_INDEX = 14
  }
}
