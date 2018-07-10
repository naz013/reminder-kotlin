package com.elementary.tasks.core.app_widgets.events

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

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

class EventsTheme : Parcelable {

    @get:ColorRes
    var headerColor: Int = 0
        private set
    @get:ColorRes
    var backgroundColor: Int = 0
        private set
    @get:ColorInt
    var titleColor: Int = 0
        private set
    @get:DrawableRes
    var plusIcon: Int = 0
        private set
    @get:DrawableRes
    var settingsIcon: Int = 0
        private set
    @get:DrawableRes
    var voiceIcon: Int = 0
        private set
    @get:ColorInt
    var itemTextColor: Int = 0
        private set
    @get:ColorRes
    var itemBackground: Int = 0
        private set
    var checkboxColor: Int = 0
        private set
    var title: String? = null
        private set
    @get:ColorRes
    var windowColor: Int = 0
        private set
    @get:ColorInt
    var windowTextColor: Int = 0
        private set

    val CREATOR: Parcelable.Creator<EventsTheme> = object : Parcelable.Creator<EventsTheme> {
        override fun createFromParcel(`in`: Parcel): EventsTheme {
            return EventsTheme(`in`)
        }

        override fun newArray(size: Int): Array<EventsTheme> {
            return arrayOfNulls(size)
        }
    }

    private constructor() {}

    constructor(@ColorRes headerColor: Int, @ColorRes backgroundColor: Int, @ColorInt titleColor: Int,
                @DrawableRes plusIcon: Int, @DrawableRes settingsIcon: Int, @DrawableRes voiceIcon: Int,
                @ColorInt itemTextColor: Int, @ColorRes itemBackground: Int, checkboxColor: Int,
                title: String, @ColorRes windowColor: Int, @ColorInt windowTextColor: Int) {
        this.headerColor = headerColor
        this.backgroundColor = backgroundColor
        this.titleColor = titleColor
        this.plusIcon = plusIcon
        this.settingsIcon = settingsIcon
        this.title = title
        this.windowColor = windowColor
        this.windowTextColor = windowTextColor
        this.voiceIcon = voiceIcon
        this.itemTextColor = itemTextColor
        this.itemBackground = itemBackground
        this.checkboxColor = checkboxColor
    }

    constructor(`in`: Parcel) : super() {
        readFromParcel(`in`)
    }

    fun readFromParcel(`in`: Parcel) {
        title = `in`.readString()
        backgroundColor = `in`.readInt()
        headerColor = `in`.readInt()
        titleColor = `in`.readInt()
        plusIcon = `in`.readInt()
        settingsIcon = `in`.readInt()
        voiceIcon = `in`.readInt()
        windowColor = `in`.readInt()
        windowTextColor = `in`.readInt()
        itemBackground = `in`.readInt()
        itemTextColor = `in`.readInt()
        checkboxColor = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeInt(headerColor)
        dest.writeInt(titleColor)
        dest.writeInt(plusIcon)
        dest.writeInt(backgroundColor)
        dest.writeInt(settingsIcon)
        dest.writeInt(voiceIcon)
        dest.writeInt(windowColor)
        dest.writeInt(windowTextColor)
        dest.writeInt(itemBackground)
        dest.writeInt(itemTextColor)
        dest.writeInt(checkboxColor)
    }

    companion object {

        private fun getResColor(ctx: Context, res: Int): Int {
            return ctx.resources.getColor(res)
        }

        fun getThemes(context: Context): List<EventsTheme> {
            val list = ArrayList<EventsTheme>()
            list.clear()
            list.add(EventsTheme(R.color.indigoPrimary, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.indigo), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(EventsTheme(R.color.tealPrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.teal), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(EventsTheme(R.color.limePrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.lime), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(EventsTheme(R.color.bluePrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.blue), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(EventsTheme(R.color.material_grey, R.color.material_divider,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.grey), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(EventsTheme(R.color.greenPrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.green), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(EventsTheme(R.color.blackPrimary, R.color.blackPrimary,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.whitePrimary), R.color.blackPrimary, 1, context.getString(R.string.dark), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(EventsTheme(R.color.whitePrimary, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                    R.drawable.ic_settings, R.drawable.ic_microphone_black,
                    getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.white), R.color.material_grey,
                    getResColor(context, R.color.whitePrimary)))

            list.add(EventsTheme(R.color.orangePrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.orange), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(EventsTheme(R.color.redPrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 0, context.getString(R.string.red), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(EventsTheme(R.color.material_grey_dialog, R.color.orangeAccent,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.blackPrimary), R.color.material_divider, 0, "Simple Orange", R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(EventsTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.whitePrimary), R.color.simple_transparent_header_color, 1, "Transparent Light", R.color.material_grey,
                    getResColor(context, R.color.whitePrimary)))

            list.add(EventsTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                    getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                    R.drawable.ic_settings, R.drawable.ic_microphone_black,
                    getResColor(context, R.color.blackPrimary), R.color.simple_transparent_header_color, 0, "Transparent Dark", R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(EventsTheme(R.color.pinkAccent, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, R.drawable.ic_microphone_white,
                    getResColor(context, R.color.blackPrimary), R.color.whitePrimary, 1, "Simple Pink", R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))
            return list
        }
    }
}
