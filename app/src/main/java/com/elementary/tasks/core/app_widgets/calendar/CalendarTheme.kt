package com.elementary.tasks.core.app_widgets.calendar

import android.content.Context
import android.os.Parcel
import android.os.Parcelable

import com.elementary.tasks.R

import java.util.ArrayList

/**
 * Copyright 2015 Nazar Suhovich
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

class CalendarTheme : Parcelable {
    var itemTextColor: Int = 0
    var widgetBgColor: Int = 0
        private set
    var headerColor: Int = 0
        private set
    var borderColor: Int = 0
        private set
    var titleColor: Int = 0
        private set
    var rowColor: Int = 0
        private set
    var leftArrow: Int = 0
        private set
    var rightArrow: Int = 0
        private set
    var iconPlus: Int = 0
        private set
    var iconVoice: Int = 0
        private set
    var iconSettings: Int = 0
        private set
    val currentMark: Int
    val birthdayMark: Int
    val reminderMark: Int
    var title: String? = null
    var windowColor: Int = 0
        private set
    var windowTextColor: Int = 0
        private set

    val CREATOR: Parcelable.Creator<CalendarTheme> = object : Parcelable.Creator<CalendarTheme> {
        override fun createFromParcel(`in`: Parcel): CalendarTheme {
            return CalendarTheme(`in`)
        }

        override fun newArray(size: Int): Array<CalendarTheme> {
            return arrayOfNulls(size)
        }
    }

    private constructor() {}

    constructor(itemTextColor: Int, widgetBgColor: Int, headerColor: Int, borderColor: Int,
                titleColor: Int, rowColor: Int, leftArrow: Int, rightArrow: Int, iconPlus: Int, iconVoice: Int,
                iconSettings: Int, title: String, currentMark: Int, birthdayMark: Int, reminderMark: Int,
                windowColor: Int, windowTextColor: Int) {
        this.itemTextColor = itemTextColor
        this.widgetBgColor = widgetBgColor
        this.headerColor = headerColor
        this.borderColor = borderColor
        this.titleColor = titleColor
        this.leftArrow = leftArrow
        this.rightArrow = rightArrow
        this.iconPlus = iconPlus
        this.iconVoice = iconVoice
        this.iconSettings = iconSettings
        this.rowColor = rowColor
        this.title = title
        this.currentMark = currentMark
        this.birthdayMark = birthdayMark
        this.reminderMark = reminderMark
        this.windowColor = windowColor
        this.windowTextColor = windowTextColor
    }

    constructor(`in`: Parcel) : super() {
        readFromParcel(`in`)
    }

    fun readFromParcel(`in`: Parcel) {
        title = `in`.readString()
        itemTextColor = `in`.readInt()
        widgetBgColor = `in`.readInt()
        rowColor = `in`.readInt()
        borderColor = `in`.readInt()
        headerColor = `in`.readInt()
        titleColor = `in`.readInt()
        leftArrow = `in`.readInt()
        rightArrow = `in`.readInt()
        iconPlus = `in`.readInt()
        iconSettings = `in`.readInt()
        iconVoice = `in`.readInt()
        windowColor = `in`.readInt()
        windowTextColor = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeInt(itemTextColor)
        dest.writeInt(widgetBgColor)
        dest.writeInt(rowColor)
        dest.writeInt(borderColor)
        dest.writeInt(headerColor)
        dest.writeInt(titleColor)
        dest.writeInt(leftArrow)
        dest.writeInt(rightArrow)
        dest.writeInt(iconPlus)
        dest.writeInt(iconVoice)
        dest.writeInt(iconSettings)
        dest.writeInt(windowColor)
        dest.writeInt(windowTextColor)
    }

    companion object {

        private fun getResColor(ctx: Context, res: Int): Int {
            return ctx.resources.getColor(res)
        }

        fun getThemes(context: Context): List<CalendarTheme> {
            val list = ArrayList<CalendarTheme>()
            list.clear()
            list.add(CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                    R.color.tealPrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, context.getString(R.string.teal), 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                    R.color.indigoPrimary, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, context.getString(R.string.indigo), 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                    R.color.limePrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, context.getString(R.string.lime), 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                    R.color.bluePrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, context.getString(R.string.blue), 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.whitePrimary), R.color.material_divider,
                    R.color.material_grey, R.color.material_divider,
                    getResColor(context, R.color.whitePrimary), R.color.material_divider,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, context.getString(R.string.grey), 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                    R.color.greenPrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, context.getString(R.string.green), 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.whitePrimary), R.color.blackPrimary,
                    R.color.blackPrimary, R.color.blackPrimary,
                    getResColor(context, R.color.whitePrimary), R.color.blackPrimary,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, context.getString(R.string.dark), 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                    R.color.whitePrimary, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                    R.drawable.simple_left_arrow_black, R.drawable.simple_right_arrow_black,
                    R.drawable.simple_plus_button_black, R.drawable.simple_voice_button_black,
                    R.drawable.simple_settings_button_black, context.getString(R.string.white), 0, 0, 0, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                    R.color.orangePrimaryDark, R.color.whitePrimary,
                    getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, context.getString(R.string.orange), 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.whitePrimary,
                    R.color.redPrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, context.getString(R.string.red), 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.orangeAccent,
                    R.color.material_grey_dialog, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.color.whitePrimary,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, "Simple Black", 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.whitePrimary), R.color.simple_transparent_widget_color,
                    R.color.simple_transparent_header_color, R.color.simple_transparent_border_color,
                    getResColor(context, R.color.whitePrimary), R.color.simple_transparent_row_color,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, "Transparent Light", 0, 0, 0, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.simple_transparent_widget_color,
                    R.color.simple_transparent_header_color, R.color.simple_transparent_border_color,
                    getResColor(context, R.color.blackPrimary), R.color.simple_transparent_row_color,
                    R.drawable.simple_left_arrow_black, R.drawable.simple_right_arrow_black,
                    R.drawable.simple_plus_button_black, R.drawable.simple_voice_button_black,
                    R.drawable.simple_settings_button_black, "Transparent Dark", 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(CalendarTheme(getResColor(context, R.color.blackPrimary), R.color.orangeAccent,
                    R.color.cyanPrimary, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.color.orangeAccent,
                    R.drawable.simple_left_arrow, R.drawable.simple_right_arrow,
                    R.drawable.simple_plus_button, R.drawable.simple_voice_button,
                    R.drawable.simple_settings_button, "Simple Brown", 0, 0, 0, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))
            return list
        }
    }
}
