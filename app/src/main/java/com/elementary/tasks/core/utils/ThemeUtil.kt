package com.elementary.tasks.core.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder

import java.util.Calendar

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

class ThemeUtil {

    private lateinit var holder: ContextHolder

    private val context: Context
        get() = holder.context

    val isDark: Boolean
        get() {
            val prefs = Prefs.getInstance(context)
            val appTheme = prefs.appTheme
            val isDark = appTheme == THEME_DARK || appTheme == THEME_AMOLED
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
        get() {
            val id: Int
            val loadedColor = Prefs.getInstance(context).appThemeColor
            if (isDark) {
                if (Prefs.getInstance(context).appTheme == THEME_AMOLED) {
                    when (loadedColor) {
                        Color.RED -> id = R.style.HomeBlack_Red
                        Color.PURPLE -> id = R.style.HomeBlack_Purple
                        Color.LIGHT_GREEN -> id = R.style.HomeBlack_LightGreen
                        Color.GREEN -> id = R.style.HomeBlack_Green
                        Color.LIGHT_BLUE -> id = R.style.HomeBlack_LightBlue
                        Color.BLUE -> id = R.style.HomeBlack_Blue
                        Color.YELLOW -> id = R.style.HomeBlack_Yellow
                        Color.ORANGE -> id = R.style.HomeBlack_Orange
                        Color.CYAN -> id = R.style.HomeBlack_Cyan
                        Color.PINK -> id = R.style.HomeBlack_Pink
                        Color.TEAL -> id = R.style.HomeBlack_Teal
                        Color.AMBER -> id = R.style.HomeBlack_Amber
                        else -> id = if (Module.isPro) {
                            when (loadedColor) {
                                Color.DEEP_PURPLE -> R.style.HomeBlack_DeepPurple
                                Color.DEEP_ORANGE -> R.style.HomeBlack_DeepOrange
                                Color.LIME -> R.style.HomeBlack_Lime
                                Color.INDIGO -> R.style.HomeBlack_Indigo
                                else -> R.style.HomeBlack_Blue
                            }
                        } else {
                            R.style.HomeBlack_Blue
                        }
                    }
                } else {
                    when (loadedColor) {
                        Color.RED -> id = R.style.HomeDark_Red
                        Color.PURPLE -> id = R.style.HomeDark_Purple
                        Color.LIGHT_GREEN -> id = R.style.HomeDark_LightGreen
                        Color.GREEN -> id = R.style.HomeDark_Green
                        Color.LIGHT_BLUE -> id = R.style.HomeDark_LightBlue
                        Color.BLUE -> id = R.style.HomeDark_Blue
                        Color.YELLOW -> id = R.style.HomeDark_Yellow
                        Color.ORANGE -> id = R.style.HomeDark_Orange
                        Color.CYAN -> id = R.style.HomeDark_Cyan
                        Color.PINK -> id = R.style.HomeDark_Pink
                        Color.TEAL -> id = R.style.HomeDark_Teal
                        Color.AMBER -> id = R.style.HomeDark_Amber
                        else -> id = if (Module.isPro) {
                            when (loadedColor) {
                                Color.DEEP_PURPLE -> R.style.HomeDark_DeepPurple
                                Color.DEEP_ORANGE -> R.style.HomeDark_DeepOrange
                                Color.LIME -> R.style.HomeDark_Lime
                                Color.INDIGO -> R.style.HomeDark_Indigo
                                else -> R.style.HomeDark_Blue
                            }
                        } else {
                            R.style.HomeDark_Blue
                        }
                    }
                }
            } else {
                when (loadedColor) {
                    Color.RED -> id = R.style.HomeWhite_Red
                    Color.PURPLE -> id = R.style.HomeWhite_Purple
                    Color.LIGHT_GREEN -> id = R.style.HomeWhite_LightGreen
                    Color.GREEN -> id = R.style.HomeWhite_Green
                    Color.LIGHT_BLUE -> id = R.style.HomeWhite_LightBlue
                    Color.BLUE -> id = R.style.HomeWhite_Blue
                    Color.YELLOW -> id = R.style.HomeWhite_Yellow
                    Color.ORANGE -> id = R.style.HomeWhite_Orange
                    Color.CYAN -> id = R.style.HomeWhite_Cyan
                    Color.PINK -> id = R.style.HomeWhite_Pink
                    Color.TEAL -> id = R.style.HomeWhite_Teal
                    Color.AMBER -> id = R.style.HomeWhite_Amber
                    else -> id = if (Module.isPro) {
                        when (loadedColor) {
                            Color.DEEP_PURPLE -> R.style.HomeWhite_DeepPurple
                            Color.DEEP_ORANGE -> R.style.HomeWhite_DeepOrange
                            Color.LIME -> R.style.HomeWhite_Lime
                            Color.INDIGO -> R.style.HomeWhite_Indigo
                            else -> R.style.HomeWhite_Blue
                        }
                    } else {
                        R.style.HomeWhite_Blue
                    }
                }
            }
            return id
        }

    val indicator: Int
        @DrawableRes
        get() = getIndicator(Prefs.getInstance(context).appThemeColor)

    val spinnerStyle: Int
        @ColorInt
        get() {
            val color: Int = if (isDark) {
                if (Prefs.getInstance(context).appTheme == THEME_AMOLED) {
                    R.color.blackPrimary
                } else {
                    R.color.material_grey
                }
            } else {
                R.color.whitePrimary
            }
            return getColor(color)
        }

    val dialogStyle: Int
        @StyleRes
        get() {
            val id: Int
            val loadedColor = Prefs.getInstance(context).appThemeColor
            if (isDark) {
                if (Prefs.getInstance(context).appTheme == THEME_AMOLED) {
                    when (loadedColor) {
                        Color.RED -> id = R.style.HomeBlackDialog_Red
                        Color.PURPLE -> id = R.style.HomeBlackDialog_Purple
                        Color.LIGHT_GREEN -> id = R.style.HomeBlackDialog_LightGreen
                        Color.GREEN -> id = R.style.HomeBlackDialog_Green
                        Color.LIGHT_BLUE -> id = R.style.HomeBlackDialog_LightBlue
                        Color.BLUE -> id = R.style.HomeBlackDialog_Blue
                        Color.YELLOW -> id = R.style.HomeBlackDialog_Yellow
                        Color.ORANGE -> id = R.style.HomeBlackDialog_Orange
                        Color.CYAN -> id = R.style.HomeBlackDialog_Cyan
                        Color.PINK -> id = R.style.HomeBlackDialog_Pink
                        Color.TEAL -> id = R.style.HomeBlackDialog_Teal
                        Color.AMBER -> id = R.style.HomeBlackDialog_Amber
                        else -> id = if (Module.isPro) {
                            when (loadedColor) {
                                Color.DEEP_PURPLE -> R.style.HomeBlackDialog_DeepPurple
                                Color.DEEP_ORANGE -> R.style.HomeBlackDialog_DeepOrange
                                Color.LIME -> R.style.HomeBlackDialog_Lime
                                Color.INDIGO -> R.style.HomeBlackDialog_Indigo
                                else -> R.style.HomeBlackDialog_Blue
                            }
                        } else {
                            R.style.HomeBlackDialog_Blue
                        }
                    }
                } else {
                    when (loadedColor) {
                        Color.RED -> id = R.style.HomeDarkDialog_Red
                        Color.PURPLE -> id = R.style.HomeDarkDialog_Purple
                        Color.LIGHT_GREEN -> id = R.style.HomeDarkDialog_LightGreen
                        Color.GREEN -> id = R.style.HomeDarkDialog_Green
                        Color.LIGHT_BLUE -> id = R.style.HomeDarkDialog_LightBlue
                        Color.BLUE -> id = R.style.HomeDarkDialog_Blue
                        Color.YELLOW -> id = R.style.HomeDarkDialog_Yellow
                        Color.ORANGE -> id = R.style.HomeDarkDialog_Orange
                        Color.CYAN -> id = R.style.HomeDarkDialog_Cyan
                        Color.PINK -> id = R.style.HomeDarkDialog_Pink
                        Color.TEAL -> id = R.style.HomeDarkDialog_Teal
                        Color.AMBER -> id = R.style.HomeDarkDialog_Amber
                        else -> id = if (Module.isPro) {
                            when (loadedColor) {
                                Color.DEEP_PURPLE -> R.style.HomeDarkDialog_DeepPurple
                                Color.DEEP_ORANGE -> R.style.HomeDarkDialog_DeepOrange
                                Color.LIME -> R.style.HomeDarkDialog_Lime
                                Color.INDIGO -> R.style.HomeDarkDialog_Indigo
                                else -> R.style.HomeDarkDialog_Blue
                            }
                        } else {
                            R.style.HomeDarkDialog_Blue
                        }
                    }
                }
            } else {
                when (loadedColor) {
                    Color.RED -> id = R.style.HomeWhiteDialog_Red
                    Color.PURPLE -> id = R.style.HomeWhiteDialog_Purple
                    Color.LIGHT_GREEN -> id = R.style.HomeWhiteDialog_LightGreen
                    Color.GREEN -> id = R.style.HomeWhiteDialog_Green
                    Color.LIGHT_BLUE -> id = R.style.HomeWhiteDialog_LightBlue
                    Color.BLUE -> id = R.style.HomeWhiteDialog_Blue
                    Color.YELLOW -> id = R.style.HomeWhiteDialog_Yellow
                    Color.ORANGE -> id = R.style.HomeWhiteDialog_Orange
                    Color.CYAN -> id = R.style.HomeWhiteDialog_Cyan
                    Color.PINK -> id = R.style.HomeWhiteDialog_Pink
                    Color.TEAL -> id = R.style.HomeWhiteDialog_Teal
                    Color.AMBER -> id = R.style.HomeWhiteDialog_Amber
                    else -> id = if (Module.isPro) {
                        when (loadedColor) {
                            Color.DEEP_PURPLE -> R.style.HomeWhiteDialog_DeepPurple
                            Color.DEEP_ORANGE -> R.style.HomeWhiteDialog_DeepOrange
                            Color.LIME -> R.style.HomeWhiteDialog_Lime
                            Color.INDIGO -> R.style.HomeWhiteDialog_Indigo
                            else -> R.style.HomeWhiteDialog_Blue
                        }
                    } else {
                        R.style.HomeWhiteDialog_Blue
                    }
                }
            }
            return id
        }

    val backgroundStyle: Int
        @ColorInt
        get() {
            return if (isDark) {
                if (Prefs.getInstance(context).appTheme == THEME_AMOLED) {
                    getColor(R.color.blackPrimary)
                } else {
                    getColor(R.color.material_grey)
                }
            } else {
                getColor(R.color.material_white)
            }
        }

    val cardStyle: Int
        @ColorInt
        get() {
            return if (isDark) {
                if (Prefs.getInstance(context).appTheme == THEME_AMOLED) {
                    getColor(R.color.blackPrimary)
                } else {
                    getColor(R.color.grey_x)
                }
            } else {
                getColor(R.color.whitePrimary)
            }
        }

    val markerStyle: Int
        @DrawableRes
        get() {
            return if (Module.isPro) {
                val loaded = Prefs.getInstance(context).markerStyle
                getMarkerStyle(loaded)
            } else {
                R.drawable.ic_location_pointer_blue
            }
        }

    val rectangle: Int
        @DrawableRes
        get() {
            val code = Prefs.getInstance(context).appThemeColor
            val color: Int
            when (code) {
                Color.RED -> color = R.drawable.rectangle_stroke_red
                Color.PURPLE -> color = R.drawable.rectangle_stroke_purple
                Color.LIGHT_GREEN -> color = R.drawable.rectangle_stroke_light_green
                Color.GREEN -> color = R.drawable.rectangle_stroke_green
                Color.LIGHT_BLUE -> color = R.drawable.rectangle_stroke_light_blue
                Color.BLUE -> color = R.drawable.rectangle_stroke_blue
                Color.YELLOW -> color = R.drawable.rectangle_stroke_yellow
                Color.ORANGE -> color = R.drawable.rectangle_stroke_orange
                Color.CYAN -> color = R.drawable.rectangle_stroke_cyan
                Color.PINK -> color = R.drawable.rectangle_stroke
                Color.TEAL -> color = R.drawable.rectangle_stroke_teal
                Color.AMBER -> color = R.drawable.rectangle_stroke_amber
                else -> color = if (Module.isPro) {
                    when (code) {
                        Color.DEEP_PURPLE -> R.drawable.rectangle_stroke_deep_purple
                        Color.DEEP_ORANGE -> R.drawable.rectangle_stroke_deep_orange
                        Color.LIME -> R.drawable.rectangle_stroke_lime
                        Color.INDIGO -> R.drawable.rectangle_stroke_indigo
                        else -> R.drawable.rectangle_stroke_blue
                    }
                } else {
                    R.drawable.rectangle_stroke_blue
                }
            }
            return color
        }

    val styleName: Int
        @StringRes
        get() {
            val style = Prefs.getInstance(context).mapStyle
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
            val style = Prefs.getInstance(context).mapStyle
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
            val style = Prefs.getInstance(context).mapStyle
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

    private constructor()

    private constructor(context: Context) {
        this.holder = ContextHolder(context)
    }

    @ColorInt
    fun getColor(@ColorRes color: Int): Int {
        return ViewUtils.getColor(context, color)
    }

    @ColorRes
    @JvmOverloads
    fun colorAccent(code: Int = Prefs.getInstance(context).appThemeColor): Int {
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

    @ColorRes
    fun colorBirthdayCalendar(): Int {
        return colorPrimary(Prefs.getInstance(context).birthdayColor)
    }

    @ColorRes
    @JvmOverloads
    fun colorPrimary(code: Int = Prefs.getInstance(context).appThemeColor): Int {
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

    @ColorRes
    fun colorReminderCalendar(): Int {
        return colorPrimary(Prefs.getInstance(context).reminderColor)
    }

    @ColorRes
    fun colorCurrentCalendar(): Int {
        return colorPrimary(Prefs.getInstance(context).todayColor)
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

    private fun getDrawable(@DrawableRes i: Int): Drawable {
        return ViewUtils.getDrawable(context, i)
    }

    fun toggleDrawable(): Drawable {
        val loadedColor = Prefs.getInstance(context).appThemeColor
        val color: Int
        when (loadedColor) {
            Color.RED -> color = R.drawable.toggle_red
            Color.PURPLE -> color = R.drawable.toggle_purple
            Color.LIGHT_GREEN -> color = R.drawable.toggle_green_light
            Color.GREEN -> color = R.drawable.toggle_green
            Color.LIGHT_BLUE -> color = R.drawable.toggle_blue_light
            Color.BLUE -> color = R.drawable.toggle_blue
            Color.YELLOW -> color = R.drawable.toggle_yellow
            Color.ORANGE -> color = R.drawable.toggle_orange
            Color.CYAN -> color = R.drawable.toggle_cyan
            Color.PINK -> color = R.drawable.toggle_pink
            Color.TEAL -> color = R.drawable.toggle_teal
            Color.AMBER -> color = R.drawable.toggle_amber
            else -> color = if (Module.isPro) {
                when (loadedColor) {
                    Color.DEEP_PURPLE -> R.drawable.toggle_deep_purple
                    Color.DEEP_ORANGE -> R.drawable.toggle_deep_orange
                    Color.LIME -> R.drawable.toggle_lime
                    Color.INDIGO -> R.drawable.toggle_indigo
                    else -> R.drawable.toggle_cyan
                }
            } else {
                R.drawable.toggle_cyan
            }
        }
        return getDrawable(color)
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

    @ColorRes
    fun colorPrimaryDark(): Int {
        val loadedColor = Prefs.getInstance(context).appThemeColor
        return colorPrimaryDark(loadedColor)
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

    @ColorInt
    fun getNoteDarkColor(code: Int): Int {
        return getColor(colorPrimaryDark(code))
    }

    fun adjustAlpha(color: Int, @IntRange(from = 0, to = 100) factor: Int): Int {
        val alpha = 255f * (factor.toFloat() / 100f)
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        return android.graphics.Color.argb(alpha.toInt(), red, green, blue)
    }

    @ColorInt
    fun getNoteLightColor(code: Int): Int {
        val color: Int
        when (code) {
            Color.RED -> color = R.color.redAccent
            Color.PURPLE -> color = R.color.purpleAccent
            Color.GREEN -> color = R.color.greenAccent
            Color.LIGHT_GREEN -> color = R.color.greenLightAccent
            Color.BLUE -> color = R.color.blueAccent
            Color.LIGHT_BLUE -> color = R.color.blueLightAccent
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
        val alpha = Prefs.getInstance(holder.context).noteColorOpacity
        return adjustAlpha(getColor(color), alpha)
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

    private interface Color {
        companion object {
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
    }

    class Marker internal constructor(@param:ColorRes @field:ColorRes
                                      @get:ColorRes
                                      val fillColor: Int, @param:ColorRes @field:ColorRes
                                      @get:ColorRes
                                      val strokeColor: Int)

    companion object {

        const val THEME_AUTO = 0
        const val THEME_WHITE = 1
        private const val THEME_DARK = 2
        const val THEME_AMOLED = 3
        const val NUM_OF_MARKERS = 16
        private var instance: ThemeUtil? = null

        fun getInstance(context: Context): ThemeUtil {
            if (instance == null) {
                synchronized(ThemeUtil::class.java) {
                    if (instance == null) {
                        instance = ThemeUtil(context.applicationContext)
                    }
                }
            }
            return instance!!
        }
    }
}
