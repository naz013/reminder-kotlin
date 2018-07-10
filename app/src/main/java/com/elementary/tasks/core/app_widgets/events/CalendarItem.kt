package com.elementary.tasks.core.app_widgets.events

import android.os.Parcel
import android.os.Parcelable

/**
 * Copyright 2017 Nazar Suhovich
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

class CalendarItem : Parcelable {

    var type: Type? = null
    var name: String? = null
    var number: String? = null
    var time: String? = null
    var dayDate: String? = null
    var id: String? = null
    var date: Long = 0
    var viewType: Int = 0
    val item: Any

    enum class Type {
        BIRTHDAY, REMINDER
    }

    constructor(type: Type, name: String, number: String, id: String, time: String,
                dayDate: String, date: Long, viewType: Int, item: Any) {
        this.type = type
        this.time = time
        this.viewType = viewType
        this.dayDate = dayDate
        this.name = name
        this.id = id
        this.number = number
        this.date = date
        this.item = item
    }

    constructor(`in`: Parcel) : super() {
        readFromParcel(`in`)
    }

    fun readFromParcel(`in`: Parcel) {
        name = `in`.readString()
        number = `in`.readString()
        id = `in`.readString()
        date = `in`.readLong()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(number)
        dest.writeString(id)
        dest.writeLong(date)
    }

    companion object {

        val CREATOR: Parcelable.Creator<CalendarItem> = object : Parcelable.Creator<CalendarItem> {
            override fun createFromParcel(`in`: Parcel): CalendarItem {
                return CalendarItem(`in`)
            }

            override fun newArray(size: Int): Array<CalendarItem> {

                return arrayOfNulls(size)
            }

        }
    }
}