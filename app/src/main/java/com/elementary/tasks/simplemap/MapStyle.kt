package com.elementary.tasks.simplemap

import android.content.Context
import androidx.annotation.RawRes
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.toColor
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

  fun getMarkerLightColor(code: Int = AppColorIndex.RED): Int {
    val color: Int
    when (code) {
      AppColorIndex.RED -> color = R.color.redAccentOld
      AppColorIndex.PURPLE -> color = R.color.purpleAccentOld
      AppColorIndex.LIGHT_GREEN -> color = R.color.greenLightAccentOld
      AppColorIndex.GREEN -> color = R.color.greenAccentOld
      AppColorIndex.LIGHT_BLUE -> color = R.color.blueLightAccentOld
      AppColorIndex.BLUE -> color = R.color.blueAccentOld
      AppColorIndex.YELLOW -> color = R.color.yellowAccentOld
      AppColorIndex.ORANGE -> color = R.color.orangeAccentOld
      AppColorIndex.CYAN -> color = R.color.cyanAccentOld
      AppColorIndex.PINK -> color = R.color.pinkAccentOld
      AppColorIndex.TEAL -> color = R.color.tealAccentOld
      AppColorIndex.AMBER -> color = R.color.amberAccentOld
      AppColorIndex.DEEP_PURPLE -> color = R.color.purpleDeepAccentOld
      AppColorIndex.DEEP_ORANGE -> color = R.color.orangeDeepAccentOld
      AppColorIndex.LIME -> color = R.color.limeAccentOld
      AppColorIndex.INDIGO -> color = R.color.indigoAccentOld
      AppColorIndex.LIVING_CORAL -> color = R.color.secondaryLivingCoral
      else -> color = R.color.blueAccentOld
    }
    return ContextCompat.getColor(context, color)
  }

  fun getMarkerRadiusStyle(color: Int): Marker {
    val fillColor: Int
    val strokeColor: Int
    when (color) {
      AppColorIndex.RED -> {
        fillColor = R.color.secondaryRed12
        strokeColor = R.color.secondaryRed
      }

      AppColorIndex.PURPLE -> {
        fillColor = R.color.secondaryPurple12
        strokeColor = R.color.secondaryPurple
      }

      AppColorIndex.LIGHT_GREEN -> {
        fillColor = R.color.secondaryGreenLight12
        strokeColor = R.color.secondaryGreenLight
      }

      AppColorIndex.GREEN -> {
        fillColor = R.color.secondaryGreen12
        strokeColor = R.color.secondaryGreen
      }

      AppColorIndex.LIGHT_BLUE -> {
        fillColor = R.color.secondaryBlueLight12
        strokeColor = R.color.secondaryBlueLight
      }

      AppColorIndex.BLUE -> {
        fillColor = R.color.secondaryBlue12
        strokeColor = R.color.secondaryBlue
      }

      AppColorIndex.YELLOW -> {
        fillColor = R.color.secondaryYellow12
        strokeColor = R.color.secondaryYellow
      }

      AppColorIndex.ORANGE -> {
        fillColor = R.color.secondaryOrange12
        strokeColor = R.color.secondaryOrange
      }

      AppColorIndex.CYAN -> {
        fillColor = R.color.secondaryCyan12
        strokeColor = R.color.secondaryCyan
      }

      AppColorIndex.PINK -> {
        fillColor = R.color.secondaryPink12
        strokeColor = R.color.secondaryPink
      }

      AppColorIndex.TEAL -> {
        fillColor = R.color.secondaryTeal12
        strokeColor = R.color.secondaryTeal
      }

      AppColorIndex.AMBER -> {
        fillColor = R.color.secondaryAmber12
        strokeColor = R.color.secondaryAmber
      }

      AppColorIndex.DEEP_PURPLE -> {
        fillColor = R.color.secondaryPurpleDeep12
        strokeColor = R.color.secondaryPurpleDeep
      }

      AppColorIndex.DEEP_ORANGE -> {
        fillColor = R.color.secondaryOrangeDeep12
        strokeColor = R.color.secondaryOrangeDeep
      }

      AppColorIndex.INDIGO -> {
        fillColor = R.color.secondaryIndigo12
        strokeColor = R.color.secondaryIndigo
      }

      AppColorIndex.LIME -> {
        fillColor = R.color.secondaryLime12
        strokeColor = R.color.secondaryLime
      }

      else -> {
        fillColor = R.color.secondaryBlue12
        strokeColor = R.color.secondaryBlue
      }
    }
    return Marker(fillColor.toColor(), strokeColor.toColor())
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
