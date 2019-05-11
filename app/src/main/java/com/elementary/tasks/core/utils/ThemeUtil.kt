package com.elementary.tasks.core.utils

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import timber.log.Timber

class ThemeUtil(private val context: Context, private val prefs: Prefs) {

    val isDark: Boolean
        get() {
            return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
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

    @ColorInt
    fun getNoteLightColor(code: Int = Color.RED): Int {
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
    fun getNoteLightColor(code: Int, opacity: Int, palette: Int = prefs.notePalette): Int {
        return adjustAlpha(getNoteColor(code, palette), opacity)
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

        fun isAlmostTransparent(opacity: Int): Boolean {
            return opacity < 25
        }

        fun isColorDark(color: Int): Boolean {
            val darkness = 1 - (0.299 * android.graphics.Color.red(color) + 0.587
                    * android.graphics.Color.green(color) + 0.114
                    * android.graphics.Color.blue(color)) / 255
            Timber.d("isColorDark: $darkness")
            return darkness >= 0.5
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

        @ColorInt
        fun colorsForSlider(context: Context): IntArray {
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

        fun isDarkMode(context: Context): Boolean {
            return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }
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
        fun adjustAlpha(@ColorInt color: Int, @IntRange(from = 0, to = 100) factor: Int): Int {
            val alpha = 255f * (factor.toFloat() / 100f)
            val red = android.graphics.Color.red(color)
            val green = android.graphics.Color.green(color)
            val blue = android.graphics.Color.blue(color)
            return android.graphics.Color.argb(alpha.toInt(), red, green, blue)
        }
    }
}
