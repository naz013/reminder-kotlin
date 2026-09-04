package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.naz013.domain.GoogleCalendarEvent
import com.google.gson.annotations.SerializedName
import java.util.Random
import java.util.UUID

@Entity(
  tableName = "GoogleCalendarEvent",
  indices = [
    Index(value = ["deviceEventId"]),
    Index(value = ["isDismissed"]),
  ],
)
internal data class GoogleCalendarEventEntity(
  @SerializedName("deviceEventId")
  val deviceEventId: Long,
  @SerializedName("calendarId")
  val calendarId: Long,
  @SerializedName("calendarName")
  val calendarName: String,
  @SerializedName("title")
  val title: String,
  @SerializedName("description")
  val description: String,
  @SerializedName("startDateTime")
  val startDateTime: Long,
  @SerializedName("endDateTime")
  val endDateTime: Long?,
  @SerializedName("allDay")
  val allDay: Boolean,
  @SerializedName("rrule")
  val rrule: String,
  @SerializedName("isDismissed")
  val isDismissed: Boolean = false,
  @SerializedName("uuId")
  @PrimaryKey
  val uuId: String = UUID.randomUUID().toString(),
  @SerializedName("uniqueId")
  val uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
) {

  constructor(event: GoogleCalendarEvent) : this(
    deviceEventId = event.deviceEventId,
    calendarId = event.calendarId,
    calendarName = event.calendarName,
    title = event.title,
    description = event.description,
    startDateTime = event.startDateTime.toEpochMillisUtc(),
    endDateTime = event.endDateTime?.toEpochMillisUtc(),
    allDay = event.allDay,
    rrule = event.rrule,
    isDismissed = event.isDismissed,
    uuId = event.uuId,
    uniqueId = event.uniqueId,
  )

  fun toDomain(): GoogleCalendarEvent = GoogleCalendarEvent(
    deviceEventId = deviceEventId,
    calendarId = calendarId,
    calendarName = calendarName,
    title = title,
    description = description,
    startDateTime = startDateTime.toLocalDateTimeUtc(),
    endDateTime = endDateTime?.toLocalDateTimeUtc(),
    allDay = allDay,
    rrule = rrule,
    isDismissed = isDismissed,
    uuId = uuId,
    uniqueId = uniqueId,
  )
}
