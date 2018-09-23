package com.elementary.tasks.core.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.elementary.tasks.core.data.converters.ListIntTypeConverter
import com.elementary.tasks.core.data.converters.ListStringTypeConverter
import com.elementary.tasks.core.data.converters.PlacesTypeConverter
import com.elementary.tasks.core.data.converters.ShopItemsTypeConverter
import com.elementary.tasks.core.interfaces.RecyclerInterface
import com.elementary.tasks.core.utils.TimeUtil
import com.google.gson.annotations.SerializedName
import java.io.Serializable
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
@TypeConverters(
        PlacesTypeConverter::class,
        ShopItemsTypeConverter::class,
        ListStringTypeConverter::class,
        ListIntTypeConverter::class
)
class Reminder : RecyclerInterface, Serializable {

    @SerializedName("summary")
    var summary: String = ""
    @SerializedName("noteId")
    var noteId: String = ""
    @SerializedName("reminderType")
    var reminderType: Int = 0
    @SerializedName("groupUuId")
    var groupUuId: String = ""
    @PrimaryKey
    @SerializedName("uuId")
    var uuId: String = ""
    @SerializedName("eventTime")
    var eventTime: String = ""
    @SerializedName("startTime")
    var startTime: String = ""
    @SerializedName("eventCount")
    var eventCount: Long = 0
    @SerializedName("color")
    var color: Int = 0
    @SerializedName("delay")
    var delay: Int = 0
    @SerializedName("vibrate")
    var vibrate: Boolean = false
    @SerializedName("repeatNotification")
    var repeatNotification: Boolean = false
    @SerializedName("notifyByVoice")
    var notifyByVoice: Boolean = false
    @SerializedName("awake")
    var awake: Boolean = false
    @SerializedName("unlock")
    var unlock: Boolean = false
    @SerializedName("exportToTasks")
    var exportToTasks: Boolean = false
    @SerializedName("exportToCalendar")
    var exportToCalendar: Boolean = false
    @SerializedName("useGlobal")
    var useGlobal: Boolean = false
    @SerializedName("from")
    var from: String = ""
    @SerializedName("to")
    var to: String = ""
    @SerializedName("hours")
    var hours: List<Int> = ArrayList()
    @SerializedName("fileName")
    var fileName: String = ""
    @SerializedName("melodyPath")
    var melodyPath: String = ""
    @SerializedName("volume")
    var volume: Int = -1
    @SerializedName("dayOfMonth")
    var dayOfMonth: Int = 0
    @SerializedName("monthOfYear")
    var monthOfYear: Int = 0
    @SerializedName("repeatInterval")
    var repeatInterval: Long = 0
    @SerializedName("repeatLimit")
    var repeatLimit: Int = -1
    @SerializedName("after")
    var after: Long = 0
    @SerializedName("weekdays")
    var weekdays: List<Int> = ArrayList()
    @SerializedName("type")
    var type: Int = 0
    @SerializedName("target")
    var target: String = ""
    @SerializedName("subject")
    var subject: String = ""
    @SerializedName("attachmentFile")
    var attachmentFile: String = ""
    @SerializedName("attachmentFiles")
    var attachmentFiles: List<String> = ArrayList()
    @SerializedName("auto")
    var auto: Boolean = false
    @SerializedName("places")
    var places: List<Place> = ArrayList()
    @SerializedName("shoppings")
    var shoppings: List<ShopItem> = ArrayList()
    @SerializedName("uniqueId")
    var uniqueId: Int = 0
    @SerializedName("isActive")
    var isActive: Boolean = false
    @SerializedName("isRemoved")
    var isRemoved: Boolean = false
    @SerializedName("isNotificationShown")
    var isNotificationShown: Boolean = false
    @SerializedName("isLocked")
    var isLocked: Boolean = false
    @SerializedName("hasReminder")
    var hasReminder: Boolean = false
    @SerializedName("duration")
    var duration: Long = 0
    @SerializedName("remindBefore")
    var remindBefore: Long = 0
    @SerializedName("windowType")
    var windowType = 0
    @SerializedName("priority")
    var priority = 2
    @ColumnInfo(name = "groupTitle")
    @Transient
    var groupTitle: String = ""
    @ColumnInfo(name = "groupColor")
    @Transient
    var groupColor: Int = 0

    val dateTime: Long
        get() = TimeUtil.getDateTimeFromGmt(eventTime)

    val startDateTime: Long
        get() = TimeUtil.getDateTimeFromGmt(startTime)

    override val viewType: Int
        get() = if (isSame(type, BY_DATE_SHOP)) {
            SHOPPING
        } else {
            REMINDER
        }

    val keys: IntArray
        get() {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(eventTime)
            val y = calendar.get(Calendar.YEAR)
            val m = calendar.get(Calendar.MONTH)
            val d = calendar.get(Calendar.DAY_OF_MONTH)
            val h = calendar.get(Calendar.HOUR_OF_DAY)
            val min = calendar.get(Calendar.MINUTE)
            return intArrayOf(y, m, d, h, min)
        }

    constructor() {
        this.uuId = UUID.randomUUID().toString()
        this.eventTime = TimeUtil.gmtDateTime
        this.uniqueId = Random().nextInt(Integer.MAX_VALUE)
        this.isActive = true
        this.isRemoved = false
        this.useGlobal = true
        this.hasReminder = false
    }

    constructor(item: Reminder, fullCopy: Boolean) {
        this.summary = item.summary
        this.reminderType = item.reminderType
        this.groupUuId = item.groupUuId
        this.eventCount = 0
        this.color = item.color
        this.delay = 0
        this.vibrate = item.vibrate
        this.repeatNotification = item.repeatNotification
        this.notifyByVoice = item.notifyByVoice
        this.awake = item.awake
        this.unlock = item.unlock
        this.exportToTasks = item.exportToTasks
        this.exportToCalendar = item.exportToCalendar
        this.useGlobal = item.useGlobal
        this.from = item.from
        this.to = item.to
        this.hours = item.hours
        this.fileName = item.fileName
        this.melodyPath = item.melodyPath
        this.volume = item.volume
        this.dayOfMonth = item.dayOfMonth
        this.repeatInterval = item.repeatInterval
        this.repeatLimit = item.repeatLimit
        this.after = item.after
        this.weekdays = item.weekdays
        this.type = item.type
        this.target = item.target
        this.subject = item.subject
        this.attachmentFile = item.attachmentFile
        this.attachmentFiles = item.attachmentFiles
        this.auto = item.auto
        this.isActive = item.isActive
        this.isRemoved = item.isRemoved
        this.isNotificationShown = item.isNotificationShown
        this.isLocked = item.isLocked
        this.places = item.places
        this.shoppings = item.shoppings
        this.duration = item.duration
        this.monthOfYear = item.monthOfYear
        this.remindBefore = item.remindBefore
        this.windowType = item.windowType
        this.priority = item.priority
        this.hasReminder = item.hasReminder
        this.groupTitle = item.groupTitle
        this.groupColor = item.groupColor
        if (fullCopy) {
            this.uuId = item.uuId
            this.uniqueId = item.uniqueId
        } else {
            this.uuId = UUID.randomUUID().toString()
            this.uniqueId = Random().nextInt(Integer.MAX_VALUE)
        }
    }

    fun copy(): Reminder {
        val reminder = Reminder(this, false)
        reminder.uuId = UUID.randomUUID().toString()
        reminder.uniqueId = Random().nextInt(Integer.MAX_VALUE)
        reminder.isActive = true
        reminder.isRemoved = false
        return reminder
    }

    override fun toString(): String {
        return "Reminder(summary='$summary', noteId='$noteId', reminderType=$reminderType, " +
                "groupUuId='$groupUuId', groupUuId='$uuId', eventTime='$eventTime', startTime='$startTime', " +
                "eventCount=$eventCount, groupColor=$color, delay=$delay, vibrate=$vibrate, " +
                "repeatNotification=$repeatNotification, notifyByVoice=$notifyByVoice, awake=$awake, " +
                "unlock=$unlock, exportToTasks=$exportToTasks, exportToCalendar=$exportToCalendar, " +
                "useGlobal=$useGlobal, from='$from', to='$to', hours=$hours, fileName='$fileName', " +
                "melodyPath='$melodyPath', volume=$volume, dayOfMonth=$dayOfMonth, monthOfYear=$monthOfYear, " +
                "repeatInterval=$repeatInterval, repeatLimit=$repeatLimit, after=$after, weekdays=$weekdays, " +
                "type=$type, target='$target', subject='$subject', attachmentFile='$attachmentFile', " +
                "attachmentFiles=$attachmentFiles, auto=$auto, places=$places, shoppings=$shoppings, " +
                "uniqueId=$uniqueId, isActive=$isActive, isRemoved=$isRemoved, " +
                "isNotificationShown=$isNotificationShown, isLocked=$isLocked, duration=$duration, " +
                "groupTitle=$groupTitle, groupColor=$groupColor, " +
                "remindBefore=$remindBefore, windowType=$windowType, priority=$priority"
    }


    object Kind {
        const val SMS = 2
        const val CALL = 1
    }

    companion object {

        const val REMINDER = 0
        const val SHOPPING = 1

        const val BY_DATE = 10
        const val BY_DATE_CALL = 11
        const val BY_DATE_SMS = 12
        const val BY_DATE_APP = 13
        const val BY_DATE_LINK = 14
        const val BY_DATE_SHOP = 15
        const val BY_DATE_EMAIL = 16
        const val BY_TIME = 20
        const val BY_TIME_CALL = 21
        const val BY_TIME_SMS = 22
        const val BY_WEEK = 30
        const val BY_WEEK_CALL = 31
        const val BY_WEEK_SMS = 32
        const val BY_LOCATION = 40
        const val BY_LOCATION_CALL = 41
        const val BY_LOCATION_SMS = 42
        const val BY_SKYPE = 50
        const val BY_SKYPE_CALL = 51
        const val BY_SKYPE_VIDEO = 52
        const val BY_MONTH = 60
        const val BY_MONTH_CALL = 61
        const val BY_MONTH_SMS = 62
        const val BY_OUT = 70
        const val BY_OUT_CALL = 71
        const val BY_OUT_SMS = 72
        const val BY_PLACES = 80
        const val BY_PLACES_CALL = 81
        const val BY_PLACES_SMS = 82
        const val BY_DAY_OF_YEAR = 90
        const val BY_DAY_OF_YEAR_CALL = 91
        const val BY_DAY_OF_YEAR_SMS = 92

        fun gpsTypes(): IntArray {
            return intArrayOf(BY_LOCATION, BY_LOCATION_CALL, BY_LOCATION_SMS, BY_OUT, BY_OUT_CALL, BY_OUT_SMS, BY_PLACES, BY_PLACES_CALL, BY_PLACES_SMS)
        }

        fun isBase(type: Int, base: Int): Boolean {
            val res = type - base
            return res in 0..9
        }

        fun isKind(type: Int, kind: Int): Boolean {
            return type % BY_DATE == kind
        }

        fun isSame(type: Int, base: Int): Boolean {
            return type == base
        }

        fun isGpsType(type: Int): Boolean {
            return isBase(type, BY_LOCATION) || isBase(type, BY_OUT) || isBase(type, BY_PLACES)
        }
    }
}
