package com.elementary.tasks.core.utils

import android.content.Context
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
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
            val isDark = appTheme > THEME_LIGHT_4
            if (appTheme == THEME_AUTO) {
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
            return isDark
        }

    val style: Int
        @StyleRes
        get() = if (Module.isPro) {
                when (prefs.appTheme) {
                    THEME_AUTO -> {
                        if (isDark) {
                            R.style.Dark1
                        } else {
                            R.style.Light1
                        }
                    }
                    THEME_PURE_BLACK -> R.style.PureBlack
                    THEME_PURE_WHITE -> R.style.PureWhite
                    THEME_LIGHT_1 -> R.style.Light1
                    THEME_LIGHT_2 -> R.style.Light2
                    THEME_LIGHT_3 -> R.style.Light3
                    THEME_LIGHT_4 -> R.style.Light4
                    THEME_DARK_1 -> R.style.Dark1
                    THEME_DARK_2 -> R.style.Dark2
                    THEME_DARK_3 -> R.style.Dark3
                    THEME_DARK_4 -> R.style.Dark4
                    else -> {
                        R.style.Light1
                    }
                }
            } else {
            when (prefs.appTheme) {
                THEME_AUTO -> {
                    if (isDark) {
                        R.style.Dark1
                    } else {
                        R.style.Light1
                    }
                }
                THEME_LIGHT_1 -> R.style.Light1
                THEME_DARK_1 -> R.style.Dark1
                else -> {
                    R.style.Light1
                }
            }
            }

    val dialogStyle: Int
        @StyleRes
        get() = if (Module.isPro) {
            when (prefs.appTheme) {
                THEME_AUTO -> {
                    if (isDark) {
                        R.style.Dark1_Dialog
                    } else {
                        R.style.Light1_Dialog
                    }
                }
                THEME_PURE_BLACK -> R.style.PureBlack_Dialog
                THEME_PURE_WHITE -> R.style.PureWhite_Dialog
                THEME_LIGHT_1 -> R.style.Light1_Dialog
                THEME_LIGHT_2 -> R.style.Light2_Dialog
                THEME_LIGHT_3 -> R.style.Light3_Dialog
                THEME_LIGHT_4 -> R.style.Light4_Dialog
                THEME_DARK_1 -> R.style.Dark1_Dialog
                THEME_DARK_2 -> R.style.Dark2_Dialog
                THEME_DARK_3 -> R.style.Dark3_Dialog
                THEME_DARK_4 -> R.style.Dark4_Dialog
                else -> {
                    R.style.Light1_Dialog
                }
            }
        } else {
            when (prefs.appTheme) {
                THEME_AUTO -> {
                    if (isDark) {
                        R.style.Dark1_Dialog
                    } else {
                        R.style.Light1_Dialog
                    }
                }
                THEME_LIGHT_1 -> R.style.Light1_Dialog
                THEME_DARK_1 -> R.style.Dark1_Dialog
                else -> {
                    R.style.Light1_Dialog
                }
            }
        }

    val backgroundStyle: Int
        @ColorInt
        get() {
            return if (isDark) {
                if (prefs.appTheme == THEME_PURE_BLACK) {
                    getColor(R.color.pureBlack)
                } else {
                    getColor(R.color.material_grey)
                }
            } else {
                getColor(R.color.material_white)
            }
        }

    val markerStyle: Int
        @DrawableRes
        get() {
            return if (Module.isPro) {
                val loaded = prefs.markerStyle
                getMarkerStyle(loaded)
            } else {
                R.drawable.ic_location_pointer_blue
            }
        }

    val rectangle: Int
        @DrawableRes
        get() {
            return R.drawable.rectangle_stroke_blue
        }

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

    @ColorRes
    @JvmOverloads
    fun colorAccent(code: Int = prefs.appThemeColor): Int {
        val color: Int
        if (isDark) {
            when (code) {
                Color.RED -> color = R.color.indigoAccent
                Color.PURPLE -> color = R.color.amberAccent
                Color.LIGHT_GREEN -> color = R.color.pinkAccent
                Color.GREEN -> color = R.color.purpleAccent
                Color.LIGHT_BLUE -> color = R.color.yellowAccent
                Color.BLUE -> color = R.color.redAccent
                Color.YELLOW -> color = R.color.redAccent
                Color.ORANGE -> color = R.color.greenAccent
                Color.CYAN -> color = R.color.purpleDeepAccent
                Color.PINK -> color = R.color.blueLightAccent
                Color.TEAL -> color = R.color.pinkAccent
                Color.AMBER -> color = R.color.blueAccent
                else -> color = if (Module.isPro) {
                    when (code) {
                        Color.DEEP_PURPLE -> R.color.greenAccent
                        Color.DEEP_ORANGE -> R.color.purpleAccent
                        Color.LIME -> R.color.redAccent
                        Color.INDIGO -> R.color.pinkAccent
                        else -> R.color.redAccent
                    }
                } else {
                    R.color.redAccent
                }
            }
        } else {
            when (code) {
                Color.RED -> color = R.color.indigoAccent
                Color.PURPLE -> color = R.color.amberAccent
                Color.LIGHT_GREEN -> color = R.color.purpleDeepAccent
                Color.GREEN -> color = R.color.cyanAccent
                Color.LIGHT_BLUE -> color = R.color.pinkAccent
                Color.BLUE -> color = R.color.yellowAccent
                Color.YELLOW -> color = R.color.cyanAccent
                Color.ORANGE -> color = R.color.pinkAccent
                Color.CYAN -> color = R.color.redAccent
                Color.PINK -> color = R.color.cyanAccent
                Color.TEAL -> color = R.color.redAccent
                Color.AMBER -> color = R.color.indigoAccent
                else -> color = if (Module.isPro) {
                    when (code) {
                        Color.DEEP_PURPLE -> R.color.greenLightAccent
                        Color.DEEP_ORANGE -> R.color.purpleDeepAccent
                        Color.LIME -> R.color.purpleAccent
                        Color.INDIGO -> R.color.pinkAccent
                        else -> R.color.yellowAccent
                    }
                } else {
                    R.color.yellowAccent
                }
            }
        }
        return color
    }

    @ColorInt
    fun colorBirthdayCalendar(): Int {
        return getNoteLightColor(prefs.birthdayColor)
    }

    @ColorRes
    @JvmOverloads
    fun colorPrimary(code: Int = prefs.appThemeColor): Int {
        val color: Int
        when (code) {
            Color.RED -> color = R.color.redPrimary
            Color.PURPLE -> color = R.color.purplePrimary
            Color.LIGHT_GREEN -> color = R.color.greenLightPrimary
            Color.GREEN -> color = R.color.greenPrimary
            Color.LIGHT_BLUE -> color = R.color.blueLightPrimary
            Color.BLUE -> color = R.color.bluePrimary
            Color.YELLOW -> color = R.color.yellowPrimary
            Color.ORANGE -> color = R.color.orangePrimary
            Color.CYAN -> color = R.color.cyanPrimary
            Color.PINK -> color = R.color.pinkPrimary
            Color.TEAL -> color = R.color.tealPrimary
            Color.AMBER -> color = R.color.amberPrimary
            else -> color = if (Module.isPro) {
                when (code) {
                    Color.DEEP_PURPLE -> R.color.purpleDeepPrimary
                    Color.DEEP_ORANGE -> R.color.orangeDeepPrimary
                    Color.LIME -> R.color.limePrimary
                    Color.INDIGO -> R.color.indigoPrimary
                    else -> R.color.cyanPrimary
                }
            } else {
                R.color.cyanPrimary
            }
        }
        return color
    }

    @ColorInt
    fun colorReminderCalendar(): Int {
        return getNoteLightColor(prefs.reminderColor)
    }

    @ColorInt
    fun colorCurrentCalendar(): Int {
        return getNoteLightColor(prefs.todayColor)
    }

    @DrawableRes
    fun getIndicator(color: Int): Int {
        val drawable: Int
        when (color) {
            Color.RED -> drawable = R.drawable.drawable_red
            Color.PURPLE -> drawable = R.drawable.drawable_purple
            Color.LIGHT_GREEN -> drawable = R.drawable.drawable_green_light
            Color.GREEN -> drawable = R.drawable.drawable_green
            Color.LIGHT_BLUE -> drawable = R.drawable.drawable_blue_light
            Color.BLUE -> drawable = R.drawable.drawable_blue
            Color.YELLOW -> drawable = R.drawable.drawable_yellow
            Color.ORANGE -> drawable = R.drawable.drawable_orange
            Color.CYAN -> drawable = R.drawable.drawable_cyan
            Color.PINK -> drawable = R.drawable.drawable_pink
            Color.TEAL -> drawable = R.drawable.drawable_teal
            Color.AMBER -> drawable = R.drawable.drawable_amber
            else -> drawable = if (Module.isPro) {
                when (color) {
                    Color.DEEP_PURPLE -> R.drawable.drawable_deep_purple
                    Color.DEEP_ORANGE -> R.drawable.drawable_deep_orange
                    Color.LIME -> R.drawable.drawable_lime
                    Color.INDIGO -> R.drawable.drawable_indigo
                    else -> R.drawable.drawable_cyan
                }
            } else {
                R.drawable.drawable_cyan
            }
        }
        return drawable
    }

    @ColorRes
    fun colorPrimaryDark(code: Int): Int {
        val color: Int
        when (code) {
            Color.RED -> color = R.color.redPrimaryDark
            Color.PURPLE -> color = R.color.purplePrimaryDark
            Color.LIGHT_GREEN -> color = R.color.greenLightPrimaryDark
            Color.GREEN -> color = R.color.greenPrimaryDark
            Color.LIGHT_BLUE -> color = R.color.blueLightPrimaryDark
            Color.BLUE -> color = R.color.bluePrimaryDark
            Color.YELLOW -> color = R.color.yellowPrimaryDark
            Color.ORANGE -> color = R.color.orangePrimaryDark
            Color.CYAN -> color = R.color.cyanPrimaryDark
            Color.PINK -> color = R.color.pinkPrimaryDark
            Color.TEAL -> color = R.color.tealPrimaryDark
            Color.AMBER -> color = R.color.amberPrimaryDark
            else -> color = if (Module.isPro) {
                when (code) {
                    Color.DEEP_PURPLE -> R.color.purpleDeepPrimaryDark
                    Color.DEEP_ORANGE -> R.color.orangeDeepPrimaryDark
                    Color.LIME -> R.color.limePrimaryDark
                    Color.INDIGO -> R.color.indigoPrimaryDark
                    else -> R.color.cyanPrimaryDark
                }
            } else {
                R.color.cyanPrimaryDark
            }
        }
        return color
    }

    fun getMarkerRadiusStyle(color: Int): Marker {
        val fillColor: Int
        val strokeColor: Int
        when (color) {
            Color.RED -> {
                fillColor = R.color.red50
                strokeColor = R.color.redPrimaryDark
            }
            Color.PURPLE -> {
                fillColor = R.color.purple50
                strokeColor = R.color.purplePrimaryDark
            }
            Color.LIGHT_GREEN -> {
                fillColor = R.color.greenLight50
                strokeColor = R.color.greenLightPrimaryDark
            }
            Color.GREEN -> {
                fillColor = R.color.green50
                strokeColor = R.color.greenPrimaryDark
            }
            Color.LIGHT_BLUE -> {
                fillColor = R.color.blueLight50
                strokeColor = R.color.blueLightPrimaryDark
            }
            Color.BLUE -> {
                fillColor = R.color.blue50
                strokeColor = R.color.bluePrimaryDark
            }
            Color.YELLOW -> {
                fillColor = R.color.yellow50
                strokeColor = R.color.yellowPrimaryDark
            }
            Color.ORANGE -> {
                fillColor = R.color.orange50
                strokeColor = R.color.orangePrimaryDark
            }
            Color.CYAN -> {
                fillColor = R.color.cyan50
                strokeColor = R.color.cyanPrimaryDark
            }
            Color.PINK -> {
                fillColor = R.color.pink50
                strokeColor = R.color.pinkPrimaryDark
            }
            Color.TEAL -> {
                fillColor = R.color.teal50
                strokeColor = R.color.tealPrimaryDark
            }
            Color.AMBER -> {
                fillColor = R.color.amber50
                strokeColor = R.color.amberPrimaryDark
            }
            Color.DEEP_PURPLE -> {
                fillColor = R.color.purpleDeep50
                strokeColor = R.color.purpleDeepPrimaryDark
            }
            Color.DEEP_ORANGE -> {
                fillColor = R.color.orangeDeep50
                strokeColor = R.color.orangeDeepPrimaryDark
            }
            Color.INDIGO -> {
                fillColor = R.color.indigo50
                strokeColor = R.color.indigoPrimaryDark
            }
            Color.LIME -> {
                fillColor = R.color.lime50
                strokeColor = R.color.limePrimaryDark
            }
            else -> {
                fillColor = R.color.blue50
                strokeColor = R.color.bluePrimaryDark
            }
        }
        return Marker(fillColor, strokeColor)
    }

    @DrawableRes
    fun getMarkerStyle(code: Int): Int {
        val color: Int
        when (code) {
            Color.RED -> color = R.drawable.ic_location_pointer_red
            Color.PURPLE -> color = R.drawable.ic_location_pointer_purple
            Color.LIGHT_GREEN -> color = R.drawable.ic_location_pointer_green_light
            Color.GREEN -> color = R.drawable.ic_location_pointer_green
            Color.LIGHT_BLUE -> color = R.drawable.ic_location_pointer_blue_light
            Color.BLUE -> color = R.drawable.ic_location_pointer_blue
            Color.YELLOW -> color = R.drawable.ic_location_pointer_yellow
            Color.ORANGE -> color = R.drawable.ic_location_pointer_orange
            Color.CYAN -> color = R.drawable.ic_location_pointer_cyan
            Color.PINK -> color = R.drawable.ic_location_pointer_pink
            Color.TEAL -> color = R.drawable.ic_location_pointer_teal
            Color.AMBER -> color = R.drawable.ic_location_pointer_amber
            Color.DEEP_PURPLE -> color = R.drawable.ic_location_pointer_purple_deep
            Color.DEEP_ORANGE -> color = R.drawable.ic_location_pointer_orange_deep
            Color.INDIGO -> color = R.drawable.ic_location_pointer_indigo
            Color.LIME -> color = R.drawable.ic_location_pointer_lime
            else -> color = R.drawable.ic_location_pointer_blue
        }
        return color
    }

    @DrawableRes
    fun getCategoryIndicator(code: Int): Int {
        val color: Int
        when (code) {
            Color.RED -> color = R.drawable.circle_red
            Color.PURPLE -> color = R.drawable.circle_purple
            Color.LIGHT_GREEN -> color = R.drawable.circle_green_light
            Color.GREEN -> color = R.drawable.circle_green
            Color.LIGHT_BLUE -> color = R.drawable.circle_blue_light
            Color.BLUE -> color = R.drawable.circle_blue
            Color.YELLOW -> color = R.drawable.circle_yellow
            Color.ORANGE -> color = R.drawable.circle_orange
            Color.CYAN -> color = R.drawable.circle_cyan
            Color.PINK -> color = R.drawable.circle_pink
            Color.TEAL -> color = R.drawable.circle_teal
            Color.AMBER -> color = R.drawable.circle_amber
            else -> color = if (Module.isPro) {
                when (code) {
                    Color.DEEP_PURPLE -> R.drawable.circle_deep_purple
                    Color.DEEP_ORANGE -> R.drawable.circle_deep_orange
                    Color.LIME -> R.drawable.circle_lime
                    Color.INDIGO -> R.drawable.circle_indigo
                    else -> R.drawable.circle_blue
                }
            } else {
                R.drawable.circle_blue
            }
        }
        return color
    }

    @ColorRes
    fun getCategoryColor(code: Int): Int {
        return colorPrimary(code)
    }

    @ColorInt
    fun getNoteColor(code: Int): Int {
        return getColor(colorPrimary(code))
    }

    private fun adjustAlpha(color: Int, @IntRange(from = 0, to = 100) factor: Int): Int {
        val alpha = 255f * (factor.toFloat() / 100f)
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        return android.graphics.Color.argb(alpha.toInt(), red, green, blue)
    }

    fun isAlmostTransparent(opacity: Int): Boolean {
        return opacity < 25
    }

    @ColorInt
    fun getNoteLightColor(code: Int): Int {
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
            else -> color = if (Module.isPro) {
                when (code) {
                    Color.DEEP_PURPLE -> R.color.purpleDeepAccent
                    Color.DEEP_ORANGE -> R.color.orangeDeepAccent
                    Color.LIME -> R.color.limeAccent
                    Color.INDIGO -> R.color.indigoAccent
                    else -> R.color.blueAccent
                }
            } else {
                R.color.blueAccent
            }
        }
        return getColor(color)
    }

    @ColorInt
    fun getNoteLightColor(code: Int, opacity: Int): Int {
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
            else -> color = if (Module.isPro) {
                when (code) {
                    Color.DEEP_PURPLE -> R.color.purpleDeepAccent
                    Color.DEEP_ORANGE -> R.color.orangeDeepAccent
                    Color.LIME -> R.color.limeAccent
                    Color.INDIGO -> R.color.indigoAccent
                    else -> R.color.blueAccent
                }
            } else {
                R.color.blueAccent
            }
        }
        return adjustAlpha(getColor(color), opacity)
    }

    @ColorInt
    fun colorsForSlider(): IntArray {
        return if (Module.isPro) {
            intArrayOf(
                    getColor(R.color.redAccent),
                    getColor(R.color.purpleAccent),
                    getColor(R.color.greenLightAccent),
                    getColor(R.color.greenAccent),
                    getColor(R.color.blueLightAccent),
                    getColor(R.color.blueAccent),
                    getColor(R.color.yellowAccent),
                    getColor(R.color.orangeAccent),
                    getColor(R.color.cyanAccent),
                    getColor(R.color.pinkAccent),
                    getColor(R.color.tealAccent),
                    getColor(R.color.amberAccent),
                    getColor(R.color.purpleDeepAccent),
                    getColor(R.color.orangeDeepAccent),
                    getColor(R.color.limeAccent),
                    getColor(R.color.indigoAccent)
            )
        } else {
            intArrayOf(
                    getColor(R.color.redAccent),
                    getColor(R.color.purpleAccent),
                    getColor(R.color.greenLightAccent),
                    getColor(R.color.greenAccent),
                    getColor(R.color.blueLightAccent),
                    getColor(R.color.blueAccent),
                    getColor(R.color.yellowAccent),
                    getColor(R.color.orangeAccent),
                    getColor(R.color.cyanAccent),
                    getColor(R.color.pinkAccent),
                    getColor(R.color.tealAccent),
                    getColor(R.color.amberAccent)
            )
        }
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

    private object Color {
        const val RED = 0
        const val PURPLE = 1
        const val LIGHT_GREEN = 2
        const val GREEN = 3
        const val LIGHT_BLUE = 4
        const val BLUE = 5
        const val YELLOW = 6
        const val ORANGE = 7
        const val CYAN = 8
        const val PINK = 9
        const val TEAL = 10
        const val AMBER = 11
        const val DEEP_PURPLE = 12
        const val DEEP_ORANGE = 13
        const val LIME = 14
        const val INDIGO = 15
    }

    class Marker internal constructor(@param:ColorRes @field:ColorRes
                                      @get:ColorRes
                                      val fillColor: Int, @param:ColorRes @field:ColorRes
                                      @get:ColorRes
                                      val strokeColor: Int)

    companion object {
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
        const val NUM_OF_MARKERS = 16

        @DrawableRes
        fun getMarkerStyle(code: Int): Int {
            val color: Int
            when (code) {
                Color.RED -> color = R.drawable.ic_location_pointer_red
                Color.PURPLE -> color = R.drawable.ic_location_pointer_purple
                Color.LIGHT_GREEN -> color = R.drawable.ic_location_pointer_green_light
                Color.GREEN -> color = R.drawable.ic_location_pointer_green
                Color.LIGHT_BLUE -> color = R.drawable.ic_location_pointer_blue_light
                Color.BLUE -> color = R.drawable.ic_location_pointer_blue
                Color.YELLOW -> color = R.drawable.ic_location_pointer_yellow
                Color.ORANGE -> color = R.drawable.ic_location_pointer_orange
                Color.CYAN -> color = R.drawable.ic_location_pointer_cyan
                Color.PINK -> color = R.drawable.ic_location_pointer_pink
                Color.TEAL -> color = R.drawable.ic_location_pointer_teal
                Color.AMBER -> color = R.drawable.ic_location_pointer_amber
                Color.DEEP_PURPLE -> color = R.drawable.ic_location_pointer_purple_deep
                Color.DEEP_ORANGE -> color = R.drawable.ic_location_pointer_orange_deep
                Color.INDIGO -> color = R.drawable.ic_location_pointer_indigo
                Color.LIME -> color = R.drawable.ic_location_pointer_lime
                else -> color = R.drawable.ic_location_pointer_blue
            }
            return color
        }
    }
}
