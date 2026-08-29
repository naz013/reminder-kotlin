package com.github.naz013.ui.common.theme

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.github.naz013.common.ContextProvider
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.adjustAlpha
import com.github.naz013.ui.common.compose.toColor

class ThemeProvider(
  private val contextProvider: ContextProvider,
  private val themePreferences: ThemePreferences
) : DarkModeState {

  private val context: Context
    get() = contextProvider.themedContext

  override val isDark: Boolean
    get() {
      return when (themePreferences.nightMode) {
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
          when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
          }
        }

        else -> themePreferences.nightMode == AppCompatDelegate.MODE_NIGHT_YES
      }
    }

  val styleName: Int
    @StringRes
    get() {
      when (themePreferences.mapStyle) {
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

  val mapStylePreview: Int
    @DrawableRes
    get() {
      when (themePreferences.mapStyle) {
        0 -> return R.drawable.preview_map_day
        1 -> return R.drawable.preview_map_retro
        2 -> return R.drawable.preview_map_silver
        3 -> return R.drawable.preview_map_night
        4 -> return R.drawable.preview_map_dark
        5 -> return R.drawable.preview_map_aubergine
        6 -> {
          return if (isDark) {
            R.drawable.preview_map_night
          } else {
            R.drawable.preview_map_day
          }
        }
      }
      return R.drawable.preview_map_day
    }

  @ColorInt
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

  @ColorInt
  fun getNoteLightColor(code: Int, opacity: Int): Int {
    return getNoteColor(code).adjustAlpha(opacity)
  }

  object AppColorIndex {
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
    const val WHITE = 17
    const val BLACK = 18
  }

  /** All note colors as one flat, ordered list - `NoteColorEngine.COLORS` (ui-note) is a plain-
   * Kotlin mirror of the same list for Compose call sites that can't reach Android resources;
   * keep the two in sync if either changes. Ends with black/white, appended in code since they
   * aren't part of any of the three hand-picked hex palettes below. */
  @ColorInt
  private fun allNoteColors(): IntArray {
    val list = mutableListOf<Int>()
    for (arrayRes in intArrayOf(R.array.note_palette_one, R.array.note_palette_two, R.array.note_palette_three)) {
      for (hex in context.resources.getStringArray(arrayRes)) {
        list.add(android.graphics.Color.parseColor(hex))
      }
    }
    list.add(android.graphics.Color.BLACK)
    list.add(android.graphics.Color.WHITE)
    return list.toIntArray()
  }

  @ColorInt
  fun getNoteColor(code: Int = AppColorIndex.RED): Int {
    val colors = allNoteColors()
    return colors[code % colors.size]
  }

  @ColorInt
  fun colorBirthdayCalendar(): Int {
    return themedColor(context, themePreferences.birthdayColor)
  }

  @ColorInt
  fun getColor(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(context, colorRes)
  }

  fun colorsForSliderThemed(): List<Color> {
    return listOf(
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
    ).map { it.toColor() }
  }

  /**
   * The 19-entry palette (accent colors plus true white/black at the end) used by the single-note
   * widget's text/overlay color pickers - as opposed to [colorsForSliderThemed], which is only
   * accent hues, this one guarantees a genuine black/white so [AppColorIndex.WHITE]/[AppColorIndex.BLACK]
   * remain valid, high-contrast default indices.
   */
  fun noteWidgetSliderColors(): List<Color> {
    return colorsForNoteWidgetSlider(context).map { it.toColor() }
  }

  fun themedColor(code: Int = AppColorIndex.RED): Color {
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
      AppColorIndex.WHITE -> color = R.color.pureWhite
      AppColorIndex.BLACK -> color = R.color.pureBlack
      else -> color = R.color.blueAccent
    }
    return ContextCompat.getColor(context, color).toColor()
  }

  companion object {
    @ColorInt
    fun getPrimaryColor(context: Context): Int {
      return ContextCompat.getColor(context, R.color.md_theme_primary)
    }

    @ColorInt
    fun getOnPrimaryColor(context: Context): Int {
      return ContextCompat.getColor(context, R.color.md_theme_onPrimary)
    }

    @ColorInt
    fun colorsForNoteWidgetSlider(context: Context): IntArray {
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
        ContextCompat.getColor(context, R.color.orangeDeepAccentOld),
        ContextCompat.getColor(context, R.color.secondaryLivingCoral),
        ContextCompat.getColor(context, R.color.pureWhite),
        ContextCompat.getColor(context, R.color.pureBlack)
      )
    }

    @ColorInt
    fun themedColor(context: Context, code: Int = AppColorIndex.RED): Int {
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
        AppColorIndex.WHITE -> color = R.color.pureWhite
        AppColorIndex.BLACK -> color = R.color.pureBlack
        else -> color = R.color.blueAccent
      }
      return ContextCompat.getColor(context, color)
    }

    @ColorInt
    fun getThemeOnSurfaceColor(context: Context): Int {
      return ContextCompat.getColor(context, R.color.md_theme_onSurface)
    }

    @ColorInt
    fun getThemeOnSecondaryContainerColor(context: Context): Int {
      return ContextCompat.getColor(context, R.color.md_theme_onSecondaryContainer)
    }

    @ColorInt
    fun colorBirthdayCalendar(context: Context, color: Int): Int {
      return themedColor(context, color)
    }

    @ColorInt
    fun colorReminderCalendar(context: Context, color: Int): Int {
      return themedColor(context, color)
    }
  }
}
