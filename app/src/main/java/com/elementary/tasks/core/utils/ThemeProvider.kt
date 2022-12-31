package com.elementary.tasks.core.utils

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs

class ThemeProvider(
  private val context: Context,
  private val prefs: Prefs
) {

  val isDark: Boolean
    get() {
      return when (prefs.nightMode) {
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
          when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
          }
        }
        else -> prefs.nightMode == AppCompatDelegate.MODE_NIGHT_YES
      }
    }

  val styleName: Int
    @StringRes
    get() {
      when (prefs.mapStyle) {
        0 -> return R.string.day
        1 -> return R.string.retro
        2 -> return R.string.silver
        3 -> return R.string.night
        4 -> return R.string.dark
        5 -> return R.string.aubergine
        6 -> return R.string.auto
      }
      return R.string.auto
    }

  val mapStyleJson: Int
    @RawRes
    get() {
      when (prefs.mapStyle) {
        0 -> return R.raw.map_terrain_day
        1 -> return R.raw.map_terrain_retro
        2 -> return R.raw.map_terrain_silver
        3 -> return R.raw.map_terrain_night
        4 -> return R.raw.map_terrain_dark
        5 -> return R.raw.map_terrain_aubergine
        6 -> {
          return if (isDark)
            R.raw.map_terrain_night
          else
            R.raw.map_terrain_day
        }
      }
      return R.raw.map_terrain_day
    }

  val mapStylePreview: Int
    @DrawableRes
    get() {
      when (prefs.mapStyle) {
        0 -> return R.drawable.preview_map_day
        1 -> return R.drawable.preview_map_retro
        2 -> return R.drawable.preview_map_silver
        3 -> return R.drawable.preview_map_night
        4 -> return R.drawable.preview_map_dark
        5 -> return R.drawable.preview_map_aubergine
        6 -> {
          return if (isDark)
            R.drawable.preview_map_night
          else
            R.drawable.preview_map_day
        }
      }
      return R.drawable.preview_map_day
    }

  fun getMarkerRadiusStyle(color: Int): Marker {
    val fillColor: Int
    val strokeColor: Int
    when (color) {
      Color.RED -> {
        fillColor = R.color.secondaryRed12
        strokeColor = R.color.secondaryRed
      }
      Color.PURPLE -> {
        fillColor = R.color.secondaryPurple12
        strokeColor = R.color.secondaryPurple
      }
      Color.LIGHT_GREEN -> {
        fillColor = R.color.secondaryGreenLight12
        strokeColor = R.color.secondaryGreenLight
      }
      Color.GREEN -> {
        fillColor = R.color.secondaryGreen12
        strokeColor = R.color.secondaryGreen
      }
      Color.LIGHT_BLUE -> {
        fillColor = R.color.secondaryBlueLight12
        strokeColor = R.color.secondaryBlueLight
      }
      Color.BLUE -> {
        fillColor = R.color.secondaryBlue12
        strokeColor = R.color.secondaryBlue
      }
      Color.YELLOW -> {
        fillColor = R.color.secondaryYellow12
        strokeColor = R.color.secondaryYellow
      }
      Color.ORANGE -> {
        fillColor = R.color.secondaryOrange12
        strokeColor = R.color.secondaryOrange
      }
      Color.CYAN -> {
        fillColor = R.color.secondaryCyan12
        strokeColor = R.color.secondaryCyan
      }
      Color.PINK -> {
        fillColor = R.color.secondaryPink12
        strokeColor = R.color.secondaryPink
      }
      Color.TEAL -> {
        fillColor = R.color.secondaryTeal12
        strokeColor = R.color.secondaryTeal
      }
      Color.AMBER -> {
        fillColor = R.color.secondaryAmber12
        strokeColor = R.color.secondaryAmber
      }
      Color.DEEP_PURPLE -> {
        fillColor = R.color.secondaryPurpleDeep12
        strokeColor = R.color.secondaryPurpleDeep
      }
      Color.DEEP_ORANGE -> {
        fillColor = R.color.secondaryOrangeDeep12
        strokeColor = R.color.secondaryOrangeDeep
      }
      Color.INDIGO -> {
        fillColor = R.color.secondaryIndigo12
        strokeColor = R.color.secondaryIndigo
      }
      Color.LIME -> {
        fillColor = R.color.secondaryLime12
        strokeColor = R.color.secondaryLime
      }
      else -> {
        fillColor = R.color.secondaryBlue12
        strokeColor = R.color.secondaryBlue
      }
    }
    return Marker(fillColor, strokeColor)
  }

  @ColorInt
  fun getMarkerLightColor(code: Int = Color.RED): Int {
    val color: Int
    when (code) {
      Color.RED -> color = R.color.redAccentOld
      Color.PURPLE -> color = R.color.purpleAccentOld
      Color.LIGHT_GREEN -> color = R.color.greenLightAccentOld
      Color.GREEN -> color = R.color.greenAccentOld
      Color.LIGHT_BLUE -> color = R.color.blueLightAccentOld
      Color.BLUE -> color = R.color.blueAccentOld
      Color.YELLOW -> color = R.color.yellowAccentOld
      Color.ORANGE -> color = R.color.orangeAccentOld
      Color.CYAN -> color = R.color.cyanAccentOld
      Color.PINK -> color = R.color.pinkAccentOld
      Color.TEAL -> color = R.color.tealAccentOld
      Color.AMBER -> color = R.color.amberAccentOld
      Color.DEEP_PURPLE -> color = R.color.purpleDeepAccentOld
      Color.DEEP_ORANGE -> color = R.color.orangeDeepAccentOld
      Color.LIME -> color = R.color.limeAccentOld
      Color.INDIGO -> color = R.color.indigoAccentOld
      Color.LIVING_CORAL -> color = R.color.secondaryLivingCoral
      else -> color = R.color.blueAccentOld
    }
    return ContextCompat.getColor(context, color)
  }

  @ColorInt
  fun getNoteLightColor(code: Int, opacity: Int, palette: Int = prefs.notePalette): Int {
    return getNoteColor(code, palette).adjustAlpha(opacity)
  }

  object Color {
    const val RED = 0
    const val PINK = 1
    const val PURPLE = 2
    const val DEEP_PURPLE = 3
    const val INDIGO = 4
    const val BLUE = 5
    const val LIGHT_BLUE = 6
    const val CYAN = 7
    const val TEAL = 8
    const val GREEN = 9
    const val LIGHT_GREEN = 10
    const val LIME = 11
    const val YELLOW = 12
    const val AMBER = 13
    const val ORANGE = 14
    const val DEEP_ORANGE = 15
    const val LIVING_CORAL = 16
  }

  @ColorInt
  private fun obtainPalette(palette: Int): IntArray {
    val list = mutableListOf<Int>()
    val hexArray = when (palette) {
      0 -> context.resources.getStringArray(R.array.note_palette_one)
      1 -> context.resources.getStringArray(R.array.note_palette_two)
      2 -> context.resources.getStringArray(R.array.note_palette_three)
      else -> context.resources.getStringArray(R.array.note_palette_one)
    }
    for (hex in hexArray) {
      list.add(android.graphics.Color.parseColor(hex))
    }
    return list.toTypedArray().toIntArray()
  }

  @ColorInt
  fun noteColorsForSlider(palette: Int = prefs.notePalette): IntArray = obtainPalette(palette)

  @ColorInt
  fun getNoteColor(code: Int = Color.RED, palette: Int = prefs.notePalette): Int {
    return obtainPalette(palette)[code]
  }

  @ColorRes
  fun pickColorRes(@ColorRes colorForLight: Int, @ColorRes colorForDark: Int) =
    if (isDark) colorForDark else colorForLight

  data class Marker(@ColorRes val fillColor: Int, @ColorRes val strokeColor: Int)

  companion object {
    const val NOTE_COLORS = 20

    @ColorInt
    fun getSecondaryColor(context: Context): Int {
      return ContextCompat.getColor(context, R.color.color_secondary)
    }

    @ColorInt
    fun getOnSecondaryColor(context: Context): Int {
      return ContextCompat.getColor(context, R.color.color_on_secondary)
    }

    @ColorInt
    fun colorsForSlider(context: Context): IntArray {
      return intArrayOf(
        ContextCompat.getColor(context, R.color.redAccentOld),
        ContextCompat.getColor(context, R.color.pinkAccentOld),
        ContextCompat.getColor(context, R.color.purpleAccentOld),
        ContextCompat.getColor(context, R.color.purpleDeepAccentOld),
        ContextCompat.getColor(context, R.color.indigoAccentOld),
        ContextCompat.getColor(context, R.color.blueAccentOld),
        ContextCompat.getColor(context, R.color.blueLightAccentOld),
        ContextCompat.getColor(context, R.color.cyanAccentOld),
        ContextCompat.getColor(context, R.color.tealAccentOld),
        ContextCompat.getColor(context, R.color.greenAccentOld),
        ContextCompat.getColor(context, R.color.greenLightAccentOld),
        ContextCompat.getColor(context, R.color.limeAccentOld),
        ContextCompat.getColor(context, R.color.yellowAccentOld),
        ContextCompat.getColor(context, R.color.amberAccentOld),
        ContextCompat.getColor(context, R.color.orangeAccentOld),
        ContextCompat.getColor(context, R.color.orangeDeepAccentOld)
      )
    }

    @ColorInt
    fun colorsForSliderThemed(context: Context): IntArray {
      return intArrayOf(
        ContextCompat.getColor(context, R.color.redAccent),
        ContextCompat.getColor(context, R.color.pinkAccent),
        ContextCompat.getColor(context, R.color.purpleAccent),
        ContextCompat.getColor(context, R.color.purpleDeepAccent),
        ContextCompat.getColor(context, R.color.indigoAccent),
        ContextCompat.getColor(context, R.color.blueAccent),
        ContextCompat.getColor(context, R.color.blueLightAccent),
        ContextCompat.getColor(context, R.color.cyanAccent),
        ContextCompat.getColor(context, R.color.tealAccent),
        ContextCompat.getColor(context, R.color.greenAccent),
        ContextCompat.getColor(context, R.color.greenLightAccent),
        ContextCompat.getColor(context, R.color.limeAccent),
        ContextCompat.getColor(context, R.color.yellowAccent),
        ContextCompat.getColor(context, R.color.amberAccent),
        ContextCompat.getColor(context, R.color.orangeAccent),
        ContextCompat.getColor(context, R.color.orangeDeepAccent)
      )
    }

    @ColorInt
    fun themedColor(context: Context, code: Int = Color.RED): Int {
      val color: Int
      when (code) {
        Color.RED -> color = R.color.redAccent
        Color.PURPLE -> color = R.color.purpleAccent
        Color.LIGHT_GREEN -> color = R.color.greenLightAccent
        Color.GREEN -> color = R.color.greenAccent
        Color.LIGHT_BLUE -> color = R.color.blueLightAccent
        Color.BLUE -> color = R.color.blueAccent
        Color.YELLOW -> color = R.color.yellowAccent
        Color.ORANGE -> color = R.color.orangeAccent
        Color.CYAN -> color = R.color.cyanAccent
        Color.PINK -> color = R.color.pinkAccent
        Color.TEAL -> color = R.color.tealAccent
        Color.AMBER -> color = R.color.amberAccent
        Color.DEEP_PURPLE -> color = R.color.purpleDeepAccent
        Color.DEEP_ORANGE -> color = R.color.orangeDeepAccent
        Color.LIME -> color = R.color.limeAccent
        Color.INDIGO -> color = R.color.indigoAccent
        else -> color = R.color.blueAccent
      }
      return ContextCompat.getColor(context, color)
    }

    @ColorInt
    fun colorWithAlpha(@ColorInt color: Int, alpha: Int): Int {
      val r = android.graphics.Color.red(color)
      val g = android.graphics.Color.green(color)
      val b = android.graphics.Color.blue(color)
      return android.graphics.Color.argb(alpha, r, g, b)
    }

    fun getThemeSecondaryColor(context: Context): Int {
      val colorAttr: Int = if (Module.isNougat1) {
        android.R.attr.colorSecondary
      } else {
        context.resources.getIdentifier("colorSecondary", "attr", context.packageName)
      }
      val outValue = TypedValue()
      context.theme.resolveAttribute(colorAttr, outValue, true)
      return outValue.data
    }

    @ColorInt
    fun colorBirthdayCalendar(context: Context, prefs: Prefs): Int {
      return themedColor(context, prefs.birthdayColor)
    }

    @ColorInt
    fun colorReminderCalendar(context: Context, prefs: Prefs): Int {
      return themedColor(context, prefs.reminderColor)
    }

    @ColorInt
    fun colorCurrentCalendar(context: Context, prefs: Prefs): Int {
      return themedColor(context, prefs.todayColor)
    }
  }
}
