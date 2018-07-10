package com.elementary.tasks.core.data.models

import android.text.TextUtils
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.elementary.tasks.core.interfaces.RecyclerInterface
import com.elementary.tasks.core.utils.SuperUtil
import com.google.gson.annotations.SerializedName
import java.util.*

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
@Entity
class Birthday : RecyclerInterface {

    @SerializedName("name")
    var name: String = ""
    @SerializedName("date")
    var date: String = ""
    @SerializedName("number")
    var number: String = ""
    @SerializedName("key")
    var key: String = ""
    @SerializedName("showedYear")
    var showedYear: Int = 0
    @SerializedName("contactId")
    var contactId: Int = 0
    @SerializedName("day")
    var day: Int = 0
    @SerializedName("month")
    var month: Int = 0
    @SerializedName("uniqueId")
    @PrimaryKey(autoGenerate = true)
    val uniqueId: Int = 0
    @SerializedName("dayMonth")
    var dayMonth: String = ""
    @SerializedName("uuId")
    var uuId: String = ""

    override val viewType: Int
        get() = 2

    constructor()

    @Ignore
    constructor(name: String, date: String, number: String, showedYear: Int, contactId: Int, day: Int, month: Int) {
        this.name = name
        this.date = date
        this.number = number
        val secKey = if (TextUtils.isEmpty(number)) "0" else number.substring(1)
        this.key = "$name|$secKey"
        this.showedYear = showedYear
        this.contactId = contactId
        this.day = day
        this.month = month
        this.dayMonth = day.toString() + "|" + month
        this.uuId = UUID.randomUUID().toString()
    }

    fun getDateTime(time: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val year = calendar.get(Calendar.YEAR)
        calendar.timeInMillis = time
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        return calendar.timeInMillis
    }

    override fun equals(obj: Any?): Boolean {
        return this.key == (obj as Birthday).key
    }

    override fun toString(): String {
        return SuperUtil.getObjectPrint(this, Birthday::class.java)
    }

    override fun hashCode(): Int {
        return UUID.fromString(uuId).hashCode()
    }
}
