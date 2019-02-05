package com.elementary.tasks.core.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Singleton
class ThemeUtil @Inject constructor(private val context: Context, private val prefs: Prefs) {

    val isDark: Boolean
        get() {
            val appTheme = prefs.appTheme
            val isDark = appTheme in THEME_DARK_1..THEME_PURE_BLACK
            if (appTheme == THEME_AUTO) {
                return isNight
            }
            return isDark
        }

    private fun isRetroDark(theme: Int): Boolean {
        return theme != THEME_RETRO_YELLOW && theme != THEME_RETRO_ORANGE && theme != THEME_RETRO_GREEN
    }

    private val isNight: Boolean
        get() {
            val calendar = Calendar.getInstance()
            val mTime = System.currentTimeMillis()
            calendar.timeInMillis = mTime
            calendar.set(Calendar.HOUR_OF_DAY, 8)
            calendar.set(Calendar.MINUTE, 0)
            val min = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 19)
            val max = calendar.timeInMillis
            return mTime !in min..max
        }

    val styleWithAccent: Int
        @StyleRes
        get() = when (prefs.appTheme) {
            THEME_AUTO -> {
                if (isDark) {
                    addAccent("Dark1")
                } else {
                    addAccent("Light1")
                }
            }
            THEME_PURE_WHITE -> addAccent("PureWhite")
            THEME_LIGHT_1 -> addAccent("Light1")
            THEME_LIGHT_2 -> addAccent("Light2")
            THEME_LIGHT_3 -> addAccent("Light3")
            THEME_LIGHT_4 -> addAccent("Light4")
            THEME_DARK_1 -> addAccent("Dark1")
            THEME_DARK_2 -> addAccent("Dark2")
            THEME_DARK_3 -> addAccent("Dark3")
            THEME_DARK_4 -> addAccent("Dark4")
            THEME_PURE_BLACK -> addAccent("PureBlack")
            else -> if (Module.isPro) {
                when (prefs.appTheme) {
                    THEME_RETRO_YELLOW -> addAccent("Retro.Yellow")
                    THEME_RETRO_ORANGE -> addAccent("Retro.Orange")
                    THEME_RETRO_GREEN -> addAccent("Retro.Green")
                    THEME_RETRO_RED -> addAccent("Retro.Red")
                    THEME_RETRO_BROWN -> addAccent("Retro.Brown")
                    else -> addAccent("Light1")
                }
            } else {
                addAccent("Light1")
            }
        }

    private val styleDialogWithAccent: Int
        @StyleRes
        get() = when (prefs.appTheme) {
            THEME_AUTO -> {
                if (isDark) {
                    addAccent("Dark1.Dialog")
                } else {
                    addAccent("Light1.Dialog")
                }
            }
            THEME_PURE_WHITE -> addAccent("PureWhite.Dialog")
            THEME_LIGHT_1 -> addAccent("Light1.Dialog")
            THEME_LIGHT_2 -> addAccent("Light2.Dialog")
            THEME_LIGHT_3 -> addAccent("Light3.Dialog")
            THEME_LIGHT_4 -> addAccent("Light4.Dialog")
            THEME_DARK_1 -> addAccent("Dark1.Dialog")
            THEME_DARK_2 -> addAccent("Dark2.Dialog")
            THEME_DARK_3 -> addAccent("Dark3.Dialog")
            THEME_DARK_4 -> addAccent("Dark4.Dialog")
            THEME_PURE_BLACK -> addAccent("PureBlack.Dialog")
            else -> if (Module.isPro) {
                when (prefs.appTheme) {
                    THEME_RETRO_YELLOW -> addAccent("Retro.Yellow.Dialog")
                    THEME_RETRO_ORANGE -> addAccent("Retro.Orange.Dialog")
                    THEME_RETRO_GREEN -> addAccent("Retro.Green.Dialog")
                    THEME_RETRO_RED -> addAccent("Retro.Red.Dialog")
                    THEME_RETRO_BROWN -> addAccent("Retro.Brown.Dialog")
                    else -> addAccent("Light1.Dialog")
                }
            } else {
                addAccent("Light1.Dialog")
            }
        }

    @StyleRes
    private fun addAccent(base: String): Int {
        val color = getStyleColorName(prefs.appThemeColor)
        return context.resources.getIdentifier("$base.$color", "style", context.packageName)
    }

    val dialogStyle: Int
        @StyleRes
        get() = styleDialogWithAccent

    val styleName: Int
        @StringRes
        get() {
            val style = prefs.mapStyle
            when (style) {
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
            val style = prefs.mapStyle
            when (style) {
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
            val style = prefs.mapStyle
            when (style) {
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

    @ColorInt
    fun getColor(@ColorRes color: Int): Int {
        return ContextCompat.getColor(context, color)
    }

    @ColorInt
    fun colorBirthdayCalendar(): Int {
        return getNoteLightColor(prefs.birthdayColor)
    }

    @ColorInt
    fun colorReminderCalendar(): Int {
        return getNoteLightColor(prefs.reminderColor)
    }

    @ColorInt
    fun colorCurrentCalendar(): Int {
        return getNoteLightColor(prefs.todayColor)
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
    fun getCategoryColor(code: Int): Int {
        return getNoteLightColor(code)
    }

    fun isAlmostTransparent(opacity: Int): Boolean {
        return opacity < 25
    }

    @ColorInt
    fun getNoteLightColor(code: Int = prefs.appThemeColor): Int {
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
            Color.LIVING_CORAL -> color = R.color.secondaryLivingCoral
            else -> color = R.color.blueAccent
        }
        return getColor(color)
    }

    @ColorInt
    fun getSecondaryColor(code: Int = prefs.appThemeColor): Int {
        val color: Int = when (code) {
            Color.RED -> R.color.secondaryRed
            Color.PURPLE -> R.color.secondaryPurple
            Color.LIGHT_GREEN -> R.color.secondaryGreenLight
            Color.GREEN -> R.color.secondaryGreen
            Color.LIGHT_BLUE -> R.color.secondaryBlueLight
            Color.BLUE -> R.color.secondaryBlue
            Color.YELLOW -> R.color.secondaryYellow
            Color.ORANGE -> R.color.secondaryOrange
            Color.CYAN -> R.color.secondaryCyan
            Color.PINK -> R.color.secondaryPink
            Color.TEAL -> R.color.secondaryTeal
            Color.AMBER -> R.color.secondaryAmber
            Color.DEEP_PURPLE -> R.color.secondaryPurpleDeep
            Color.DEEP_ORANGE -> R.color.secondaryOrangeDeep
            Color.LIME -> R.color.secondaryLime
            Color.INDIGO -> R.color.secondaryIndigo
            Color.LIVING_CORAL -> R.color.secondaryLivingCoral
            Color.WHITE -> R.color.pureWhite
            Color.BLACK -> R.color.pureBlack
            else -> R.color.secondaryBlue
        }
        return getColor(color)
    }

    @ColorInt
    fun getOnSecondaryColor(code: Int = prefs.appThemeColor): Int {
        val color: Int = when (code) {
            Color.BLACK -> R.color.pureWhite
            else -> R.color.pureBlack
        }
        return getColor(color)
    }

    @ColorInt
    fun getNoteLightColor(code: Int, opacity: Int, palette: Int = prefs.notePalette): Int {
        return adjustAlpha(getNoteColor(code, palette), opacity)
    }

    fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * android.graphics.Color.red(color) + 0.587
                * android.graphics.Color.green(color) + 0.114
                * android.graphics.Color.blue(color)) / 255
        Timber.d("isColorDark: $darkness")
        return darkness >= 0.5
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
        const val WHITE = 17
        const val BLACK = 18
    }

    @ColorInt
    fun colorsForSlider(): IntArray {
        return intArrayOf(
                getColor(R.color.redAccent),
                getColor(R.color.pinkAccent),
                getColor(R.color.purpleAccent),
                getColor(R.color.purpleDeepAccent),
                getColor(R.color.indigoAccent),
                getColor(R.color.blueAccent),
                getColor(R.color.blueLightAccent),
                getColor(R.color.cyanAccent),
                getColor(R.color.tealAccent),
                getColor(R.color.greenAccent),
                getColor(R.color.greenLightAccent),
                getColor(R.color.limeAccent),
                getColor(R.color.yellowAccent),
                getColor(R.color.amberAccent),
                getColor(R.color.orangeAccent),
                getColor(R.color.orangeDeepAccent)
        )
    }

    @ColorInt
    fun accentColorsForSlider(): IntArray {
        return intArrayOf(
                getColor(R.color.secondaryRed),
                getColor(R.color.secondaryPink),
                getColor(R.color.secondaryPurple),
                getColor(R.color.secondaryPurpleDeep),
                getColor(R.color.secondaryIndigo),
                getColor(R.color.secondaryBlue),
                getColor(R.color.secondaryBlueLight),
                getColor(R.color.secondaryCyan),
                getColor(R.color.secondaryTeal),
                getColor(R.color.secondaryGreen),
                getColor(R.color.secondaryGreenLight),
                getColor(R.color.secondaryLime),
                getColor(R.color.secondaryYellow),
                getColor(R.color.secondaryAmber),
                getColor(R.color.secondaryOrange),
                getColor(R.color.secondaryOrangeDeep),
                getColor(R.color.secondaryLivingCoral),
                getColor(R.color.pureWhite),
                getColor(R.color.pureBlack)
        )
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
    fun getNoteColor(code: Int = prefs.appThemeColor, palette: Int = prefs.notePalette): Int {
        return obtainPalette(palette)[code]
    }

    @ColorInt
    fun themeColorsForSlider(): IntArray {
        if (Module.isPro) {
            return intArrayOf(
                    getColor(if (isNight) R.color.darkPrimary1 else R.color.lightPrimary1),
                    getColor(R.color.pureWhite),
                    getColor(R.color.lightPrimary1),
                    getColor(R.color.lightPrimary2),
                    getColor(R.color.lightPrimary3),
                    getColor(R.color.lightPrimary4),
                    getColor(R.color.darkPrimary1),
                    getColor(R.color.darkPrimary2),
                    getColor(R.color.darkPrimary3),
                    getColor(R.color.darkPrimary4),
                    getColor(R.color.pureBlack),
                    getColor(R.color.retroYellow),
                    getColor(R.color.retroOrange),
                    getColor(R.color.retroGreen),
                    getColor(R.color.retroRed),
                    getColor(R.color.retroBrown)
            )
        } else {
            return intArrayOf(
                    getColor(if (isNight) R.color.darkPrimary1 else R.color.lightPrimary1),
                    getColor(R.color.pureWhite),
                    getColor(R.color.lightPrimary1),
                    getColor(R.color.lightPrimary2),
                    getColor(R.color.lightPrimary3),
                    getColor(R.color.lightPrimary4),
                    getColor(R.color.darkPrimary1),
                    getColor(R.color.darkPrimary2),
                    getColor(R.color.darkPrimary3),
                    getColor(R.color.darkPrimary4),
                    getColor(R.color.pureBlack)
            )
        }
    }

    private fun getStyleColorName(code: Int): String {
        return when (code) {
            Color.RED -> "Red"
            Color.PINK -> "Pink"
            Color.PURPLE -> "Purple"
            Color.DEEP_PURPLE -> "PurpleDeep"
            Color.INDIGO -> "Indigo"
            Color.BLUE -> "Blue"
            Color.LIGHT_BLUE -> "LightBlue"
            Color.CYAN -> "Cyan"
            Color.TEAL -> "Teal"
            Color.GREEN -> "Green"
            Color.LIGHT_GREEN -> "LightGreen"
            Color.LIME -> "Lime"
            Color.YELLOW -> "Yellow"
            Color.AMBER -> "Amber"
            Color.ORANGE -> "Orange"
            Color.DEEP_ORANGE -> "OrangeDeep"
            Color.LIVING_CORAL -> "LivingCoral"
            Color.WHITE -> "White"
            Color.BLACK -> "Black"
            else -> "Red"
        }
    }

    fun accentNames(): List<String> {
        return listOf(
                context.getString(R.string.red),
                context.getString(R.string.pink),
                context.getString(R.string.purple),
                context.getString(R.string.dark_purple),
                context.getString(R.string.indigo),
                context.getString(R.string.blue),
                context.getString(R.string.blue_light),
                context.getString(R.string.cyan),
                context.getString(R.string.teal),
                context.getString(R.string.green),
                context.getString(R.string.green_light),
                context.getString(R.string.lime),
                context.getString(R.string.yellow),
                context.getString(R.string.amber),
                context.getString(R.string.orange),
                context.getString(R.string.dark_orange),
                context.getString(R.string.living_coral),
                context.getString(R.string.white),
                context.getString(R.string.black))
    }

    @DrawableRes
    fun getReminderIllustration(type: Int): Int {
        return when {
            Reminder.isKind(type, Reminder.Kind.CALL) -> R.drawable.ic_phone_call
            Reminder.isKind(type, Reminder.Kind.SMS) -> R.drawable.ic_chat
            Reminder.isBase(type, Reminder.BY_LOCATION) -> R.drawable.ic_location_illustration
            Reminder.isBase(type, Reminder.BY_OUT) -> R.drawable.ic_radar
            Reminder.isBase(type, Reminder.BY_PLACES) -> R.drawable.ic_placeholder
            Reminder.isSame(type, Reminder.BY_DATE_LINK) -> R.drawable.ic_browser
            Reminder.isSame(type, Reminder.BY_DATE_APP) -> R.drawable.ic_gamepad
            Reminder.isSame(type, Reminder.BY_DATE_EMAIL) -> R.drawable.ic_email_illustration
            Reminder.isSame(type, Reminder.BY_DATE_SHOP) -> R.drawable.ic_shopping_cart
            Reminder.isBase(type, Reminder.BY_DATE) -> R.drawable.ic_calendar_illustration
            Reminder.isBase(type, Reminder.BY_WEEK) -> R.drawable.ic_alarm_clock
            Reminder.isBase(type, Reminder.BY_MONTH) -> R.drawable.ic_seventeen
            Reminder.isBase(type, Reminder.BY_SKYPE) -> R.drawable.ic_skype_illustration
            Reminder.isBase(type, Reminder.BY_MONTH) -> R.drawable.ic_seventeen
            Reminder.isBase(type, Reminder.BY_TIME) -> R.drawable.ic_stopwatch
            Reminder.isBase(type, Reminder.BY_DAY_OF_YEAR) -> R.drawable.ic_balloons
            else -> R.drawable.ic_bell_illustration
        }
    }

    data class Marker(@ColorRes val fillColor: Int, @ColorRes val strokeColor: Int)

    companion object {
        const val NOTE_COLORS = 20

        const val THEME_AUTO = 0
        const val THEME_PURE_WHITE = 1
        const val THEME_LIGHT_1 = 2
        const val THEME_LIGHT_2 = 3
        const val THEME_LIGHT_3 = 4
        const val THEME_LIGHT_4 = 5
        const val THEME_DARK_1 = 6
        const val THEME_DARK_2 = 7
        const val THEME_DARK_3 = 8
        const val THEME_DARK_4 = 9
        const val THEME_PURE_BLACK = 10
        const val THEME_RETRO_YELLOW = 11
        const val THEME_RETRO_ORANGE = 12
        const val THEME_RETRO_GREEN = 13
        const val THEME_RETRO_RED = 14
        const val THEME_RETRO_BROWN = 15

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
        fun adjustAlpha(@ColorInt color: Int, @IntRange(from = 0, to = 100) factor: Int): Int {
            val alpha = 255f * (factor.toFloat() / 100f)
            val red = android.graphics.Color.red(color)
            val green = android.graphics.Color.green(color)
            val blue = android.graphics.Color.blue(color)
            return android.graphics.Color.argb(alpha.toInt(), red, green, blue)
        }
    }
}
