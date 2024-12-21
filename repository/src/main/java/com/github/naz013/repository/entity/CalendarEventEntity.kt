package com.github.naz013.repository.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.naz013.domain.CalendarEvent
import com.google.gson.annotations.SerializedName
import java.util.UUID

@Entity(tableName = "CalendarEvent")
internal data class CalendarEventEntity(
  @SerializedName("reminderId")
  val reminderId: String,
  @SerializedName("event")
  val event: String,
  @SerializedName("eventId")
  val eventId: Long,
  @SerializedName("allDay")
  val allDay: Boolean,
  @SerializedName("uuId")
  @PrimaryKey
  val uuId: String = UUID.randomUUID().toString()
) {

  constructor(calendarEvent: CalendarEvent) : this(
    reminderId = calendarEvent.reminderId,
    event = calendarEvent.event,
    eventId = calendarEvent.eventId,
    allDay = calendarEvent.allDay,
    uuId = calendarEvent.uuId
  )

  fun toDomain(): CalendarEvent {
    return CalendarEvent(
      reminderId = reminderId,
      event = event,
      eventId = eventId,
      allDay = allDay,
      uuId = uuId
    )
  }
}
