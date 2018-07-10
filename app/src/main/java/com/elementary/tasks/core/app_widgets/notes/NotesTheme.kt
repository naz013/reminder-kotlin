package com.elementary.tasks.core.app_widgets.notes

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

class NotesTheme : Parcelable {

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
    var title: String? = null
        private set
    @get:ColorRes
    var windowColor: Int = 0
        private set
    @get:ColorInt
    var windowTextColor: Int = 0
        private set

    val CREATOR: Parcelable.Creator<NotesTheme> = object : Parcelable.Creator<NotesTheme> {
        override fun createFromParcel(`in`: Parcel): NotesTheme {
            return NotesTheme(`in`)
        }

        override fun newArray(size: Int): Array<NotesTheme> {
            return arrayOfNulls(size)
        }
    }

    private constructor() {}

    constructor(@ColorRes headerColor: Int, @ColorRes backgroundColor: Int, @ColorInt titleColor: Int,
                @DrawableRes plusIcon: Int, @DrawableRes settingsIcon: Int,
                title: String, @ColorRes windowColor: Int, @ColorInt windowTextColor: Int) {
        this.headerColor = headerColor
        this.backgroundColor = backgroundColor
        this.titleColor = titleColor
        this.plusIcon = plusIcon
        this.settingsIcon = settingsIcon
        this.title = title
        this.windowColor = windowColor
        this.windowTextColor = windowTextColor
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
        windowColor = `in`.readInt()
        windowTextColor = `in`.readInt()
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
        dest.writeInt(windowColor)
        dest.writeInt(windowTextColor)
    }

    companion object {

        private fun getResColor(ctx: Context, res: Int): Int {
            return ctx.resources.getColor(res)
        }

        fun getThemes(context: Context): List<NotesTheme> {
            val list = ArrayList<NotesTheme>()
            list.clear()
            list.add(NotesTheme(R.color.indigoPrimary, R.color.whitePrimary,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, context.getString(R.string.indigo), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(NotesTheme(R.color.tealPrimaryDark, R.color.whitePrimary,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, context.getString(R.string.teal), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(NotesTheme(R.color.limePrimaryDark, R.color.whitePrimary,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, context.getString(R.string.lime), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(NotesTheme(R.color.bluePrimaryDark, R.color.whitePrimary,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, context.getString(R.string.blue), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(NotesTheme(R.color.material_grey, R.color.material_divider,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, context.getString(R.string.grey), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(NotesTheme(R.color.greenPrimaryDark, R.color.whitePrimary,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, context.getString(R.string.green), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(NotesTheme(R.color.blackPrimary, R.color.blackPrimary,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, context.getString(R.string.dark), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(NotesTheme(R.color.whitePrimary, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                    R.drawable.ic_settings, context.getString(R.string.white), R.color.material_grey,
                    getResColor(context, R.color.whitePrimary)))

            list.add(NotesTheme(R.color.orangePrimaryDark, R.color.whitePrimary,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, context.getString(R.string.orange), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(NotesTheme(R.color.redPrimaryDark, R.color.whitePrimary,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, context.getString(R.string.red), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(NotesTheme(R.color.material_grey_dialog, R.color.orangeAccent,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, "Simple Orange", R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(NotesTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, "Transparent Light", R.color.material_grey,
                    getResColor(context, R.color.whitePrimary)))

            list.add(NotesTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                    getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                    R.drawable.ic_settings, "Transparent Dark", R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(NotesTheme(R.color.pinkAccent, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, "Simple Pink", R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))
            return list
        }
    }
}
