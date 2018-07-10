package com.elementary.tasks.core.app_widgets.tasks

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

class TasksTheme : Parcelable {

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
    @get:ColorInt
    var itemTextColor: Int = 0
        private set
    var title: String? = null
        private set
    @get:ColorRes
    var windowColor: Int = 0
        private set
    @get:ColorInt
    var windowTextColor: Int = 0
        private set

    val CREATOR: Parcelable.Creator<TasksTheme> = object : Parcelable.Creator<TasksTheme> {
        override fun createFromParcel(`in`: Parcel): TasksTheme {
            return TasksTheme(`in`)
        }

        override fun newArray(size: Int): Array<TasksTheme> {
            return arrayOfNulls(size)
        }
    }

    private constructor() {}

    constructor(@ColorRes headerColor: Int, @ColorRes backgroundColor: Int, @ColorInt titleColor: Int,
                @DrawableRes plusIcon: Int, @DrawableRes settingsIcon: Int, @ColorInt itemTextColor: Int,
                title: String, @ColorRes windowColor: Int, @ColorInt windowTextColor: Int) {
        this.headerColor = headerColor
        this.backgroundColor = backgroundColor
        this.titleColor = titleColor
        this.plusIcon = plusIcon
        this.settingsIcon = settingsIcon
        this.title = title
        this.windowColor = windowColor
        this.windowTextColor = windowTextColor
        this.itemTextColor = itemTextColor
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
        itemTextColor = `in`.readInt()
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
        dest.writeInt(itemTextColor)
    }

    companion object {

        private fun getResColor(ctx: Context, res: Int): Int {
            return ctx.resources.getColor(res)
        }

        fun getThemes(context: Context): List<TasksTheme> {
            val list = ArrayList<TasksTheme>()
            list.clear()
            list.add(TasksTheme(R.color.indigoPrimary, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.indigo), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(TasksTheme(R.color.tealPrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.teal), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(TasksTheme(R.color.limePrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.lime), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(TasksTheme(R.color.bluePrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.blue), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(TasksTheme(R.color.material_grey, R.color.material_divider,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.blackPrimary), context.getString(R.string.grey), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(TasksTheme(R.color.greenPrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.green), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(TasksTheme(R.color.blackPrimary, R.color.blackPrimary,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.dark), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(TasksTheme(R.color.whitePrimary, R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                    R.drawable.ic_settings, getResColor(context, R.color.blackPrimary), context.getString(R.string.white), R.color.material_grey,
                    getResColor(context, R.color.whitePrimary)))

            list.add(TasksTheme(R.color.orangePrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.orange), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(TasksTheme(R.color.redPrimaryDark, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), context.getString(R.string.red), R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(TasksTheme(R.color.material_grey_dialog, R.color.orangeAccent,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.blackPrimary), "Simple Orange", R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(TasksTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), "Transparent Light", R.color.material_grey,
                    getResColor(context, R.color.whitePrimary)))

            list.add(TasksTheme(R.color.simple_transparent_header_color, R.color.simple_transparent_header_color,
                    getResColor(context, R.color.blackPrimary), R.drawable.ic_add_black_24dp,
                    R.drawable.ic_settings, getResColor(context, R.color.blackPrimary), "Transparent Dark", R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))

            list.add(TasksTheme(R.color.pinkAccent, R.color.material_grey,
                    getResColor(context, R.color.whitePrimary), R.drawable.ic_add_white_24dp,
                    R.drawable.ic_settings_white, getResColor(context, R.color.whitePrimary), "Simple Pink", R.color.whitePrimary,
                    getResColor(context, R.color.blackPrimary)))
            return list
        }
    }
}
