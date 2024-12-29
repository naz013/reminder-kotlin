package com.github.naz013.repository.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.BuilderSchemeItem
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.repository.converters.BuilderSchemeItemsTypeConverter
import com.github.naz013.repository.converters.ListIntTypeConverter
import com.github.naz013.repository.converters.ListStringTypeConverter
import com.github.naz013.repository.converters.PlacesTypeConverter
import com.github.naz013.repository.converters.ShopItemsTypeConverter
import com.google.gson.annotations.SerializedName
import java.util.Random
import java.util.UUID

@Entity(tableName = "Reminder")
@TypeConverters(
  PlacesTypeConverter::class,
  ShopItemsTypeConverter::class,
  ListStringTypeConverter::class,
  ListIntTypeConverter::class,
  BuilderSchemeItemsTypeConverter::class
)
@Keep
internal data class ReminderEntity(
  @SerializedName("summary")
  val summary: String = "",
  @SerializedName("noteId")
  val noteId: String = "",
  @SerializedName("reminderType")
  val reminderType: Int = 0,
  @SerializedName("eventState")
  val eventState: Int = 1,
  @SerializedName("groupUuId")
  val groupUuId: String = "",
  @SerializedName("uuId")
  @PrimaryKey
  val uuId: String = UUID.randomUUID().toString(),
  @SerializedName("eventTime")
  val eventTime: String = "",
  @SerializedName("startTime")
  val startTime: String = "",
  @SerializedName("eventCount")
  val eventCount: Long = 0,
  @SerializedName("color")
  val color: Int = 0,
  @SerializedName("delay")
  val delay: Int = 0,
  @SerializedName("vibrate")
  val vibrate: Boolean = false,
  @SerializedName("repeatNotification")
  val repeatNotification: Boolean = false,
  @SerializedName("notifyByVoice")
  @Deprecated("Not supported in newer OS versions")
  val notifyByVoice: Boolean = false,
  @SerializedName("awake")
  @Deprecated("Not supported in newer OS versions")
  val awake: Boolean = false,
  @SerializedName("unlock")
  @Deprecated("Not supported in newer OS versions")
  val unlock: Boolean = false,
  @SerializedName("exportToTasks")
  val exportToTasks: Boolean = false,
  @SerializedName("exportToCalendar")
  val exportToCalendar: Boolean = false,
  @SerializedName("useGlobal")
  val useGlobal: Boolean = true,
  @SerializedName("from")
  val from: String = "",
  @SerializedName("to")
  val to: String = "",
  @SerializedName("hours")
  val hours: List<Int> = ArrayList(),
  @SerializedName("fileName")
  @Deprecated("Not supported in newer OS versions")
  val fileName: String = "",
  @SerializedName("melodyPath")
  @Deprecated("Not supported in newer OS versions")
  val melodyPath: String = "",
  @SerializedName("volume")
  val volume: Int = -1,
  @SerializedName("dayOfMonth")
  val dayOfMonth: Int = 0,
  @SerializedName("monthOfYear")
  val monthOfYear: Int = 0,
  @SerializedName("repeatInterval")
  val repeatInterval: Long = 0,
  @SerializedName("repeatLimit")
  val repeatLimit: Int = -1,
  @SerializedName("after")
  val after: Long = 0,
  @SerializedName("weekdays")
  val weekdays: List<Int> = ArrayList(),
  @SerializedName("type")
  val type: Int = 0,
  @SerializedName("target")
  val target: String = "",
  @SerializedName("subject")
  val subject: String = "",
  @SerializedName("attachmentFile")
  @Deprecated("Use #attachmentFiles property")
  val attachmentFile: String = "",
  @SerializedName("attachmentFiles")
  val attachmentFiles: List<String> = ArrayList(),
  @SerializedName("auto")
  @Deprecated("Not supported in newer OS versions")
  val auto: Boolean = false,
  @SerializedName("places")
  val places: List<PlaceEntity> = ArrayList(),
  @SerializedName("shoppings")
  val shoppings: List<ShopItem> = ArrayList(),
  @SerializedName("uniqueId")
  val uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
  @SerializedName("isActive")
  val isActive: Boolean = true,
  @SerializedName("isRemoved")
  val isRemoved: Boolean = false,
  @SerializedName("isNotificationShown")
  val isNotificationShown: Boolean = false,
  @SerializedName("isLocked")
  val isLocked: Boolean = false,
  // Used for Delayed events, such as Location
  @SerializedName("hasReminder")
  val hasReminder: Boolean = false,
  // Used, when add event to calendar
  @SerializedName("duration")
  val duration: Long = 0,
  @SerializedName("calendarId")
  val calendarId: Long = 0,
  @SerializedName("remindBefore")
  val remindBefore: Long = 0,
  @SerializedName("windowType")
  @Deprecated("Not supported in newer OS versions")
  val windowType: Int = 0,
  @SerializedName("priority")
  val priority: Int = 2,
  @SerializedName("updatedAt")
  val updatedAt: String? = null,
  @SerializedName("taskListId")
  val taskListId: String? = null,
  @SerializedName("recurDataObject")
  val recurDataObject: String? = null,
  // Used, when add event to calendar
  @SerializedName("allDay")
  val allDay: Boolean = false,
  @SerializedName("description")
  val description: String? = null,
  @SerializedName("builderScheme")
  val builderScheme: List<BuilderSchemeItem>? = null,
  @SerializedName("version")
  val version: String? = Reminder.DEFAULT_VERSION,

  @ColumnInfo(name = "groupTitle")
  @Transient
  val groupTitle: String? = "",
  @ColumnInfo(name = "groupColor")
  @Transient
  val groupColor: Int = 0
) {

  constructor(reminder: Reminder) : this(
    summary = reminder.summary,
    noteId = reminder.noteId,
    reminderType = reminder.reminderType,
    eventState = reminder.eventState,
    groupUuId = reminder.groupUuId,
    uuId = reminder.uuId,
    eventTime = reminder.eventTime,
    startTime = reminder.startTime,
    eventCount = reminder.eventCount,
    color = reminder.color,
    delay = reminder.delay,
    vibrate = reminder.vibrate,
    repeatNotification = reminder.repeatNotification,
    notifyByVoice = reminder.notifyByVoice,
    awake = reminder.awake,
    unlock = reminder.unlock,
    exportToTasks = reminder.exportToTasks,
    exportToCalendar = reminder.exportToCalendar,
    useGlobal = reminder.useGlobal,
    from = reminder.from,
    to = reminder.to,
    hours = reminder.hours,
    fileName = reminder.fileName,
    melodyPath = reminder.melodyPath,
    volume = reminder.volume,
    dayOfMonth = reminder.dayOfMonth,
    monthOfYear = reminder.monthOfYear,
    repeatInterval = reminder.repeatInterval,
    repeatLimit = reminder.repeatLimit,
    after = reminder.after,
    weekdays = reminder.weekdays,
    type = reminder.type,
    target = reminder.target,
    subject = reminder.subject,
    attachmentFile = reminder.attachmentFile,
    attachmentFiles = reminder.attachmentFiles,
    auto = reminder.auto,
    places = reminder.places.map { PlaceEntity(it) },
    shoppings = reminder.shoppings,
    uniqueId = reminder.uniqueId,
    isActive = reminder.isActive,
    isRemoved = reminder.isRemoved,
    isNotificationShown = reminder.isNotificationShown,
    isLocked = reminder.isLocked,
    hasReminder = reminder.hasReminder,
    duration = reminder.duration,
    calendarId = reminder.calendarId,
    remindBefore = reminder.remindBefore,
    windowType = reminder.windowType,
    priority = reminder.priority,
    updatedAt = reminder.updatedAt,
    taskListId = reminder.taskListId,
    recurDataObject = reminder.recurDataObject,
    allDay = reminder.allDay,
    description = reminder.description,
    builderScheme = reminder.builderScheme,
    version = reminder.version
  )

  fun toDomain(): Reminder {
    return Reminder(
      summary = summary,
      noteId = noteId,
      reminderType = reminderType,
      eventState = eventState,
      groupUuId = groupUuId,
      uuId = uuId,
      eventTime = eventTime,
      startTime = startTime,
      eventCount = eventCount,
      color = color,
      delay = delay,
      vibrate = vibrate,
      repeatNotification = repeatNotification,
      notifyByVoice = notifyByVoice,
      awake = awake,
      unlock = unlock,
      exportToTasks = exportToTasks,
      exportToCalendar = exportToCalendar,
      useGlobal = useGlobal,
      from = from,
      to = to,
      hours = hours,
      fileName = fileName,
      melodyPath = melodyPath,
      volume = volume,
      dayOfMonth = dayOfMonth,
      monthOfYear = monthOfYear,
      repeatInterval = repeatInterval,
      repeatLimit = repeatLimit,
      after = after,
      weekdays = weekdays,
      type = type,
      target = target,
      subject = subject,
      attachmentFile = attachmentFile,
      attachmentFiles = attachmentFiles,
      auto = auto,
      places = places.map { it.toDomain() },
      shoppings = shoppings,
      uniqueId = uniqueId,
      isActive = isActive,
      isRemoved = isRemoved,
      isNotificationShown = isNotificationShown,
      isLocked = isLocked,
      hasReminder = hasReminder,
      duration = duration,
      calendarId = calendarId,
      remindBefore = remindBefore,
      windowType = windowType,
      priority = priority,
      updatedAt = updatedAt,
      taskListId = taskListId,
      recurDataObject = recurDataObject,
      allDay = allDay,
      description = description,
      builderScheme = builderScheme,
      version = version,
      groupTitle = groupTitle,
      groupColor = groupColor
    )
  }
}
