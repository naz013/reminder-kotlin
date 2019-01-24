package com.elementary.tasks.core.data.models

import androidx.room.*
import com.elementary.tasks.core.data.converters.ListIntTypeConverter
import com.elementary.tasks.core.data.converters.ListStringTypeConverter
import com.elementary.tasks.core.data.converters.PlacesTypeConverter
import com.elementary.tasks.core.data.converters.ShopItemsTypeConverter
import com.elementary.tasks.core.interfaces.RecyclerInterface
import com.elementary.tasks.core.utils.TimeUtil
import java.io.Serializable
import java.util.*

/**
 * Copyright 2018 Nazar Suhovich
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
data class Reminder(
        var summary: String = "",
        var noteId: String = "",
        var reminderType: Int = 0,
        var groupUuId: String = "",
        @PrimaryKey
        var uuId: String = UUID.randomUUID().toString(),
        var eventTime: String = "",
        var startTime: String = "",
        var eventCount: Long = 0,
        var color: Int = 0,
        var delay: Int = 0,
        var vibrate: Boolean = false,
        var repeatNotification: Boolean = false,
        var notifyByVoice: Boolean = false,
        var awake: Boolean = false,
        var unlock: Boolean = false,
        var exportToTasks: Boolean = false,
        var exportToCalendar: Boolean = false,
        var useGlobal: Boolean = true,
        var from: String = "",
        var to: String = "",
        var hours: List<Int> = ArrayList(),
        var fileName: String = "",
        var melodyPath: String = "",
        var volume: Int = -1,
        var dayOfMonth: Int = 0,
        var monthOfYear: Int = 0,
        var repeatInterval: Long = 0,
        var repeatLimit: Int = 0,
        var after: Long = 0,
        var weekdays: List<Int> = ArrayList(),
        var type: Int = 0,
        var target: String = "",
        var subject: String = "",
        var attachmentFile: String = "",
        var attachmentFiles: List<String> = ArrayList(),
        var auto: Boolean = false,
        var places: List<Place> = ArrayList(),
        var shoppings: List<ShopItem> = ArrayList(),
        var uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
        var isActive: Boolean = true,
        var isRemoved: Boolean = false,
        var isNotificationShown: Boolean = false,
        var isLocked: Boolean = false,
        var hasReminder: Boolean = false,
        var duration: Long = 0,
        var remindBefore: Long = 0,
        var windowType: Int = 0,
        var priority: Int = 2,
        @ColumnInfo(name = "groupTitle")
        @Transient
        var groupTitle: String? = "",
        @ColumnInfo(name = "groupColor")
        @Transient
        var groupColor: Int = 0
) : RecyclerInterface, Serializable {

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

    @Ignore
    constructor(item: Reminder, fullCopy: Boolean) : this() {
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

    fun isLimited(): Boolean = repeatLimit > 0

    fun isLimitExceed(): Boolean = repeatLimit - eventCount - 1 > 0

    fun copy(): Reminder {
        val reminder = Reminder(this, false)
        reminder.uuId = UUID.randomUUID().toString()
        reminder.uniqueId = Random().nextInt(Integer.MAX_VALUE)
        reminder.isActive = true
        reminder.isRemoved = false
        return reminder
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
