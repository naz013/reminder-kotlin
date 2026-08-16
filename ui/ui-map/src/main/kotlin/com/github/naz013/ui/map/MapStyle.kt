package com.github.naz013.ui.map

import android.content.Context
import androidx.annotation.RawRes
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.toColor
import com.github.naz013.ui.common.compose.withAlpha
import com.github.naz013.ui.common.theme.DarkModeState
import com.github.naz013.ui.common.theme.ThemeProvider.AppColorIndex
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions

class MapStyle(
  private val context: Context,
  private val darkModeState: DarkModeState
) {

  fun getMapStyleOptions(
    mapStyle: Int,
    mapType: Int
  ): MapStyleOptions? {
    if (mapType != GoogleMap.MAP_TYPE_NORMAL) return null
    return try {
      MapStyleOptions.loadRawResourceStyle(context, getMapStyleJson(mapStyle))
    } catch (_: Exception) {
      null
    }
  }

  @RawRes
  fun getMapStyleJson(mapStyle: Int): Int {
    return when (mapStyle) {
      0 -> R.raw.map_terrain_day
      1 -> R.raw.map_terrain_retro
      2 -> R.raw.map_terrain_silver
      3 -> R.raw.map_terrain_night
      4 -> R.raw.map_terrain_dark
      5 -> R.raw.map_terrain_aubergine
      else -> {
        if (darkModeState.isDark) {
          R.raw.map_terrain_night
        } else {
          R.raw.map_terrain_day
        }
      }
    }
  }

  fun getMarkerColor(code: Int = AppColorIndex.RED): Color {
    val color: Int
    when (code) {
      AppColorIndex.RED -> color = R.color.redAccent
      AppColorIndex.PURPLE -> color = R.color.purpleAccent
      AppColorIndex.LIGHT_GREEN -> color = R.color.greenLightAccent
      AppColorIndex.GREEN -> color = R.color.greenAccent
      AppColorIndex.LIGHT_BLUE -> color = R.color.blueLightAccent
      AppColorIndex.BLUE -> color = R.color.blueAccent
      AppColorIndex.YELLOW -> color = R.color.yellowAccent
      AppColorIndex.ORANGE -> color = R.color.orangeAccent
      AppColorIndex.CYAN -> color = R.color.cyanAccent
      AppColorIndex.PINK -> color = R.color.pinkAccent
      AppColorIndex.TEAL -> color = R.color.tealAccent
      AppColorIndex.AMBER -> color = R.color.amberAccent
      AppColorIndex.DEEP_PURPLE -> color = R.color.purpleDeepAccent
      AppColorIndex.DEEP_ORANGE -> color = R.color.orangeDeepAccent
      AppColorIndex.LIME -> color = R.color.limeAccent
      AppColorIndex.INDIGO -> color = R.color.indigoAccent
      AppColorIndex.LIVING_CORAL -> color = R.color.secondaryLivingCoral
      else -> color = R.color.blueAccent
    }
    return ContextCompat.getColor(context, color).toColor()
  }

  fun getMarkerRadiusStyle(color: Int): Marker {
    val strokeColor = getMarkerColor(color)
    return Marker(strokeColor.withAlpha(0.12f), strokeColor)
  }

  fun colorsForSlider(): List<Color> {
    return listOf(
      ContextCompat.getColor(context, R.color.redAccent).toColor(),
      ContextCompat.getColor(context, R.color.pinkAccent).toColor(),
      ContextCompat.getColor(context, R.color.purpleAccent).toColor(),
      ContextCompat.getColor(context, R.color.purpleDeepAccent).toColor(),
      ContextCompat.getColor(context, R.color.indigoAccent).toColor(),
      ContextCompat.getColor(context, R.color.blueAccent).toColor(),
      ContextCompat.getColor(context, R.color.blueLightAccent).toColor(),
      ContextCompat.getColor(context, R.color.cyanAccent).toColor(),
      ContextCompat.getColor(context, R.color.tealAccent).toColor(),
      ContextCompat.getColor(context, R.color.greenAccent).toColor(),
      ContextCompat.getColor(context, R.color.greenLightAccent).toColor(),
      ContextCompat.getColor(context, R.color.limeAccent).toColor(),
      ContextCompat.getColor(context, R.color.yellowAccent).toColor(),
      ContextCompat.getColor(context, R.color.amberAccent).toColor(),
      ContextCompat.getColor(context, R.color.orangeAccent).toColor(),
      ContextCompat.getColor(context, R.color.orangeDeepAccent).toColor()
    )
  }

  data class Marker(val fillColor: Color, val strokeColor: Color)
}
