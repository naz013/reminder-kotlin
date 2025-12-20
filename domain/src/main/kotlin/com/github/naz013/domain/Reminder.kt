package com.github.naz013.domain

import com.github.naz013.domain.reminder.BuilderSchemeItem
import com.github.naz013.domain.reminder.ReminderType
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.sync.SyncState
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Random
import java.util.UUID

data class Reminder(
  @SerializedName("summary")
  var summary: String = "",
  @SerializedName("noteId")
  var noteId: String = "",
  @SerializedName("reminderType")
  @Deprecated("Types are not used anymore")
  var reminderType: Int = 0,
  @SerializedName("eventState")
  var eventState: Int = 1,
  @SerializedName("groupUuId")
  @Deprecated("We will not use groups as mandatory field in new versions")
  var groupUuId: String = "",
  @SerializedName("uuId")
  var uuId: String = UUID.randomUUID().toString(),
  @SerializedName("eventTime")
  var eventTime: String = "",
  @SerializedName("startTime")
  var startTime: String = "",
  @SerializedName("eventCount")
  var eventCount: Long = 0,
  @SerializedName("color")
  var color: Int = 0,
  @SerializedName("delay")
  var delay: Int = 0,
  @SerializedName("vibrate")
  var vibrate: Boolean = false,
  @SerializedName("repeatNotification")
  var repeatNotification: Boolean = false,
  @SerializedName("notifyByVoice")
  @Deprecated("Not supported in newer OS versions")
  var notifyByVoice: Boolean = false,
  @SerializedName("awake")
  @Deprecated("Not supported in newer OS versions")
  var awake: Boolean = false,
  @SerializedName("unlock")
  @Deprecated("Not supported in newer OS versions")
  var unlock: Boolean = false,
  @SerializedName("exportToTasks")
  var exportToTasks: Boolean = false,
  @SerializedName("exportToCalendar")
  var exportToCalendar: Boolean = false,
  @SerializedName("useGlobal")
  var useGlobal: Boolean = true,
  @SerializedName("from")
  var from: String = "",
  @SerializedName("to")
  var to: String = "",
  @SerializedName("hours")
  var hours: List<Int> = ArrayList(),
  @SerializedName("fileName")
  @Deprecated("Not supported in newer OS versions")
  var fileName: String = "",
  @SerializedName("melodyPath")
  @Deprecated("Not supported in newer OS versions")
  var melodyPath: String = "",
  @SerializedName("volume")
  var volume: Int = -1,
  @SerializedName("dayOfMonth")
  var dayOfMonth: Int = -1,
  @SerializedName("monthOfYear")
  var monthOfYear: Int = -1,
  @SerializedName("repeatInterval")
  var repeatInterval: Long = 0,
  @SerializedName("repeatLimit")
  var repeatLimit: Int = -1,
  @SerializedName("after")
  var after: Long = 0, // Countdown time in millis
  @SerializedName("weekdays")
  var weekdays: List<Int> = ArrayList(), // 0 - Sunday, 1 - Monday ..., If == 1 then selected
  @SerializedName("type")
  @Deprecated("Types are not used anymore")
  var type: Int = 0,
  @SerializedName("target")
  var target: String = "",
  @SerializedName("subject")
  var subject: String = "",
  @SerializedName("attachmentFile")
  @Deprecated("Use #attachmentFiles property")
  var attachmentFile: String = "",
  @SerializedName("attachmentFiles")
  var attachmentFiles: List<String> = ArrayList(),
  @SerializedName("auto")
  @Deprecated("Not supported in newer OS versions")
  var auto: Boolean = false,
  @SerializedName("places")
  var places: List<Place> = ArrayList(),
  @SerializedName("shoppings")
  var shoppings: List<ShopItem> = ArrayList(),
  @SerializedName("uniqueId")
  var uniqueId: Int = Random().nextInt(Integer.MAX_VALUE), // Used for notifications and AlarmManager
  @SerializedName("isActive")
  var isActive: Boolean = true,
  @SerializedName("isRemoved")
  var isRemoved: Boolean = false,
  @SerializedName("isNotificationShown")
  var isNotificationShown: Boolean = false, // Used for Location based reminders
  @SerializedName("isLocked")
  var isLocked: Boolean = false, // Used for Location based reminders
  // Used for Delayed events, such as Location
  @SerializedName("hasReminder")
  @Deprecated("Reminder presence is determined by date/time fields now")
  var hasReminder: Boolean = false,
  // Used, when add event to calendar
  @SerializedName("duration")
  var duration: Long = 0,
  @SerializedName("calendarId")
  var calendarId: Long = 0,
  @SerializedName("remindBefore")
  var remindBefore: Long = 0,
  @SerializedName("windowType")
  @Deprecated("Not supported in newer OS versions")
  var windowType: Int = 0,
  @SerializedName("priority")
  var priority: Int = 2,
  @SerializedName("updatedAt")
  var updatedAt: String? = null,
  @SerializedName("taskListId")
  var taskListId: String? = null,
  @SerializedName("recurDataObject")
  var recurDataObject: String? = null,
  // Used, when add event to calendar
  @SerializedName("allDay")
  var allDay: Boolean = false,
  @SerializedName("description")
  var description: String? = null,
  @SerializedName("builderScheme")
  var builderScheme: List<BuilderSchemeItem>? = null,
  @SerializedName("version")
  var jsonSchemaVersion: String? = DEFAULT_VERSION,
  @SerializedName("versionId")
  var version: Long = 0L,
  @Transient
  val syncState: SyncState = SyncState.Synced,
  @Transient
  @Deprecated("We will not use groups as mandatory field in new versions")
  var groupTitle: String? = "",
  @Transient
  @Deprecated("We will not use groups as mandatory field in new versions")
  var groupColor: Int = 0
) : Serializable {

  constructor(
    item: Reminder,
    fullCopy: Boolean,
    updatedAt: String?
  ) : this() {
    this.summary = item.summary
    this.reminderType = item.reminderType
    this.groupUuId = item.groupUuId
    this.eventCount = 0
    this.color = item.color
    this.delay = 0
    this.vibrate = item.vibrate
    this.repeatNotification = item.repeatNotification
    this.exportToTasks = item.exportToTasks
    this.exportToCalendar = item.exportToCalendar
    this.useGlobal = item.useGlobal
    this.from = item.from
    this.to = item.to
    this.hours = item.hours
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
    this.isActive = item.isActive
    this.isRemoved = item.isRemoved
    this.isNotificationShown = item.isNotificationShown
    this.isLocked = item.isLocked
    this.places = item.places
    this.shoppings = item.shoppings.toList()
    this.duration = item.duration
    this.monthOfYear = item.monthOfYear
    this.remindBefore = item.remindBefore
    this.priority = item.priority
    this.hasReminder = item.hasReminder
    this.groupTitle = item.groupTitle
    this.groupColor = item.groupColor
    this.calendarId = item.calendarId
    this.taskListId = item.taskListId
    this.allDay = item.allDay
    if (fullCopy) {
      this.uuId = item.uuId
      this.uniqueId = item.uniqueId
      this.updatedAt = item.updatedAt
    } else {
      this.uuId = UUID.randomUUID().toString()
      this.uniqueId = Random().nextInt(Integer.MAX_VALUE)
      this.updatedAt = updatedAt
    }
  }

  fun isLimited(): Boolean = repeatLimit > 0

  fun isLimitExceed(): Boolean = !isLimited() || (repeatLimit - eventCount - 1) < 0

  fun copy(updatedAt: String?): Reminder {
    val reminder = Reminder(this, false, updatedAt)
    reminder.uuId = UUID.randomUUID().toString()
    reminder.uniqueId = Random().nextInt(Integer.MAX_VALUE)
    reminder.isActive = true
    reminder.isRemoved = false
    return reminder
  }

  @Deprecated("Types are not used anymore")
  fun readType(): ReminderType {
    return ReminderType(type)
  }

  object Action {
    const val NONE = 0
    const val CALL = 1
    const val SMS = 2
    const val APP = 3
    const val LINK = 4
    const val SHOP = 5
    const val EMAIL = 6
  }

  object Version {
    const val V2 = "v2.0"
    const val V3 = "v3.0"
  }

  companion object {
    const val DEFAULT_VERSION = Version.V2

    const val BY_DATE = 10
    const val BY_DATE_APP = 13
    const val BY_DATE_LINK = 14
    const val BY_DATE_SHOP = 15
    const val BY_DATE_EMAIL = 16
    const val BY_TIME = 20
    const val BY_WEEK = 30
    const val BY_LOCATION = 40
    const val BY_LOCATION_CALL = 41
    const val BY_LOCATION_SMS = 42
    const val BY_MONTH = 60
    const val BY_OUT = 70
    const val BY_OUT_CALL = 71
    const val BY_OUT_SMS = 72

    @Deprecated("Removed after 9.3.1")
    const val BY_PLACES = 80

    @Deprecated("Removed after 9.3.1")
    const val BY_PLACES_CALL = 81

    @Deprecated("Removed after 9.3.1")
    const val BY_PLACES_SMS = 82
    const val BY_DAY_OF_YEAR = 90
    const val BY_RECUR = 100

    fun gpsTypes(): IntArray {
      return intArrayOf(
        BY_LOCATION,
        BY_LOCATION_CALL,
        BY_LOCATION_SMS,
        BY_OUT,
        BY_OUT_CALL,
        BY_OUT_SMS,
        BY_PLACES,
        BY_PLACES_CALL,
        BY_PLACES_SMS
      )
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
      return isBase(type, BY_LOCATION) || isBase(type, BY_OUT)
    }
  }
}
